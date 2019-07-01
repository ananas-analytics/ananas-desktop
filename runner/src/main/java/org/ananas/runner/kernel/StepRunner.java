package org.ananas.runner.kernel;

import org.ananas.runner.kernel.common.DataReader;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

/** A Step Runner runs a step and delivers output data and schemas. */
public interface StepRunner {

  /**
   * Gets immutable collections of {@link Row}
   *
   * @return an instance of {@link PCollection<Row>}
   */
  PCollection<Row> getOutput();

  String getMessage();

  /**
   * Get output schemas
   *
   * @return an instance of {@link Schema}
   */
  Schema getSchema();

  /**
   * Get output schemas coder
   *
   * @return an instance of {@link SchemaCoder}
   */
  SchemaCoder getSchemaCoder();

  /** Set output schemas coder */
  void setSchemaCoder(SchemaCoder coder);

  /**
   * Get Step ID
   *
   * @return string value of Step ID
   */
  String getStepId();

  /**
   * Get a data Reader
   *
   * @return a DataReader
   */
  DataReader getReader();

  void setReader();

  StepType getType();

  /** build the step runner */
  void build();
}
