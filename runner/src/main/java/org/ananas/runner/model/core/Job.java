package org.ananas.runner.model.core;

import lombok.Data;
import org.apache.beam.sdk.PipelineResult;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;

@Data
public class Job {

	public String id;
	public String projectId;
	private PipelineResult pipelineResult;
	private Exception e;
	public PipelineResult.State lastUpdate;
	public String token;

	public void cancel() throws IOException {
		this.pipelineResult.cancel();

	}

	public MutablePair<PipelineResult.State, Exception> getState() {
		if (this.pipelineResult == null) {
			return MutablePair.of(PipelineResult.State.FAILED, this.e);
		}
		return MutablePair.of(this.pipelineResult.getState(), this.e);
	}

	public static Job of(String id, PipelineResult r, Exception e, String projectId, String token) {
		Job s = new Job();
		s.id = id;
		s.pipelineResult = r;
		s.projectId = projectId;
		s.token = token;
		s.e = e;
		s.lastUpdate = PipelineResult.State.UNKNOWN;
		return s;
	}

}