package org.ananas.runner.model.steps.commons.jobs;

import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.core.PipelineContext;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.build.Builder;
import org.apache.beam.sdk.PipelineResult;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class LocalJobManager implements JobManager, JobRepository {

	private ReentrantLock lock;
	private ConcurrentMap<String, Job> jobs;

	private static LocalJobManager singleton;

	private LocalJobManager() {
		this.lock = new ReentrantLock();
		this.jobs = new ConcurrentHashMap<>();
	}

	public static LocalJobManager Of() {
		if (singleton == null) {
			singleton = new LocalJobManager();
		}
		return singleton;
	}

	@Override
	public void cancelJob(String id) throws IOException {
		Job r = this.jobs.get(id);
		if (r != null) {
			this.lock.lock();
			try {
				r.cancel();
			} finally {
				this.lock.unlock();
			}
		}
	}

	@Override
	public Job getJob(String id) {
		return this.jobs.get(id);
	}


	@Override
	public Set<Job> getJobs() {
		Set<Job> jobs = new HashSet<>();
		for (String id : this.jobs.keySet()) {
			Job j = getJob(id);
			if (j != null) {
				jobs.add(getJob(id));
			}
		}
		return jobs;
	}

	@Override
	public String run(String jobId,
					  Builder builder,
					  String projectId,
					  String token) {
		this.lock.lock();
		try {
			CompletableFuture<MutablePair<PipelineResult, Exception>> pipelineFuture
					= CompletableFuture.supplyAsync(() -> {
						try {
							MutablePair<Map<String, StepRunner>, Stack<PipelineContext>> build = builder.build();
							Iterator<PipelineContext> it = build.getRight().iterator();
							PipelineResult lastIntermediateResult = null;
							while (it.hasNext()) {
								if (lastIntermediateResult != null &&
										lastIntermediateResult.getState() != PipelineResult.State.DONE) {
									return MutablePair.of(lastIntermediateResult, (Exception) null); //We abort
								}
								PipelineContext next = it.next();
								if (it.hasNext()) {
									//Important : we wait for a pipelines to finish before we trigger the next one.
									next.waitUntilFinish();
								} else {
									lastIntermediateResult = next.run();
								}
							}
							return MutablePair.of(lastIntermediateResult, (Exception) null);
						} catch (Exception e) {
							System.out.println(e.getMessage());
							return MutablePair.of((PipelineResult) null, e);
						}
					}
			);

			pipelineFuture.thenApply(pipelineResult -> {
				this.jobs.put(jobId,
						Job.of(jobId, pipelineResult.getLeft(), pipelineResult.getRight(), projectId, builder.getGoals(), token));
				return pipelineResult != null;
			});

		} finally {
			this.lock.unlock();
		}
		return jobId;
	}

	@Override
	public void removeJob(String jobId) {
		this.lock.lock();
		try {
			this.jobs.remove(jobId);
		} finally {
			this.lock.unlock();
		}
	}
}
