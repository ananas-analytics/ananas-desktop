package org.ananas.runner.kernel;

import java.io.Serializable;
import org.ananas.runner.kernel.common.DataReader;
import org.ananas.runner.kernel.common.DirectRunnerDataReader;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.kernel.model.StepType;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.schemas.SchemaCoder;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;

public abstract class AbstractStepRunner implements StepRunner, Serializable {

  private static final long serialVersionUID = -2738595602320369583L;
  protected static int DEFAULT_LIMIT = new Integer(100);

  protected transient PCollection<Row> output;
  protected String stepId;
  private transient DataReader reader;
  protected transient ErrorHandler errors;
  protected transient StepType type;

  public String getMessage() {
    return this.outputMessage;
  }

  public void setOutputMessage(String outputMessage) {
    this.outputMessage = outputMessage;
  }

  protected transient String outputMessage;

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

  public void build() {
    // do nothing
  }

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
}
