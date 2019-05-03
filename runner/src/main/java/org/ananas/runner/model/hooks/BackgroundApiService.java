package org.ananas.runner.model.hooks;

import org.ananas.runner.model.core.Job;
import org.ananas.runner.model.steps.commons.run.BeamRunner;
import org.ananas.runner.model.steps.commons.run.Runner;
import org.apache.beam.sdk.PipelineResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class BackgroundApiService implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(BackgroundApiService.class);

	private static int POLLING_INTERVAL_IN_MILLIS = 1000 * 2;

	private boolean doStop = false;

	public synchronized void doStop() {
		this.doStop = true;
		LOG.debug("Aborted. Graceful shutdown. ");
	}

	private synchronized boolean keepRunning() {
		return this.doStop == false;
	}

	@Override
	public void run() {
		LOG.debug("Starting Background service");
		while (keepRunning()) {
			try {
				Thread.sleep(POLLING_INTERVAL_IN_MILLIS);
			} catch (InterruptedException e) {
				LOG.warn("Can't sleep !!!");
			}
			pollJobs();

		}
	}

	/**
	 * Cancel all running jobs
	 */
	public synchronized void cancelRunningJobs() {
		Runner runner = new BeamRunner();
		Set<Job> jobs = runner.getJobs();
		for (Job job : jobs) {
			if (job.getState().getLeft() == PipelineResult.State.RUNNING) {
				boolean cancelled = false;
				try {
					runner.cancel(job.id);
					cancelled = true;
				} catch (IOException e) {
				}
				if (cancelled) {
					runner.updateJobState(job.id);
					// NOTE: keep all jobs in memory for now
					// runner.removeJob(job.id);
				}
			}
		}
		LOG.debug("Graceful shutdown - Cancelled all jobs.");
	}

	private synchronized void pollJobs() {
		Runner runner = new BeamRunner();
		Set<Job> jobs = runner.getJobs();
		for (Job job : jobs) {
			runner.updateJobState(job.id);
			if (job.getState().getLeft().isTerminal()) {
				// NOTE: keep all jobs in memory for now
				// runner.removeJob(job.id);
				// LOG.debug("removed job - " + job.id);
			}
		}
	}

}
