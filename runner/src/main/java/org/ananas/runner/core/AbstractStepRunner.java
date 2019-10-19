package org.ananas.runner.core;

import java.io.Serializable;
import org.ananas.runner.core.common.DataReader;
import org.ananas.runner.core.common.DirectRunnerDataReader;
import org.ananas.runner.core.errors.ErrorHandler;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.model.StepType;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public abstract class AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = -2738595602320369583L;
  protected static int DEFAULT_LIMIT = 100;

  protected String stepId;
  protected transient PCollection<Row> output;
  protected transient ErrorHandler errors;
  protected transient StepType type;
  protected transient String outputMessage;
  protected transient ExtensionManager extensionManager;
  private transient DataReader reader;

  public String getMessage() {
    return this.outputMessage;
  }

  public void setOutputMessage(String outputMessage) {
    this.outputMessage = outputMessage;
  }

  public String getStepId() {
    return this.stepId;
  }

  public PCollection<Row> getOutput() {
    return this.output;
  }

  public void setOutput(PCollection<Row> pCollection) {
    this.output = pCollection;
  }

  public Schema getSchema() {
    return ((SchemaCoder) getOutput().getCoder()).getSchema();
  }

  public SchemaCoder getSchemaCoder() {
    return (SchemaCoder) getOutput().getCoder();
  }

  public void setSchemaCoder(SchemaCoder coder) {
    this.getOutput().setCoder(coder);
  }

  public StepType getType() {
    return this.type;
  }

  public DataReader getReader() {
    return this.reader;
  }

  public abstract void build();

  public void setReader() {
    if (this.output != null) {
      this.reader = DirectRunnerDataReader.of(this.stepId);
      this.output.apply(this.reader.mapElements());
    }
  }

  protected AbstractStepRunner(StepType type) {
    this.type = type;
    this.errors = new ErrorHandler();
  }

  @Override
  public void setExtensionManager(ExtensionManager extensionManager) {
    this.extensionManager = extensionManager;
  }
}
