package org.ananas.runner.model.steps.concat;

import com.google.common.base.Preconditions;
import org.ananas.runner.model.steps.commons.AbstractStepRunner;
import org.ananas.runner.model.steps.commons.StepRunner;
import org.ananas.runner.model.steps.commons.StepType;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class ConcatConnector extends AbstractStepRunner implements StepRunner, Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(ConcatConnector.class);
	private static final long serialVersionUID = 8007583692748290306L;

	/**
	 * Connector concatenating two upstream pipelines
	 *
	 * @param id        The Concat Connector Pipeline id
	 * @param leftStep  The Left Upstream Pipeline id
	 * @param rightStep The Right Upstream Pipeline id
	 */
	public ConcatConnector(String id,
						   StepRunner leftStep,
						   StepRunner rightStep
	) {
		super(StepType.Connector);
		Preconditions.checkNotNull(leftStep);
		Preconditions.checkNotNull(rightStep);

		//hack for flink issue with Schema coder equals
		leftStep.getSchema().setUUID(null);
		rightStep.getSchema().setUUID(null);

		if (!leftStep.getSchema().equals(rightStep.getSchema())) {
			throw new RuntimeException("Both steps should have same columns (name and type). ");
		}

		if (leftStep.getSchemaCoder() != null) {
			try {
				leftStep.setSchemaCoder(rightStep.getSchemaCoder());
			} catch (Exception e) {
				rightStep.setSchemaCoder(leftStep.getSchemaCoder());
			}

		}

		this.stepId = id;

		PCollectionList<Row> a = PCollectionList.of(leftStep.getOutput()).and(rightStep.getOutput());

		this.output = a.apply(Flatten.pCollections());
		this.output.setCoder(SchemaCoder.of(leftStep.getSchema()));
	}

}


