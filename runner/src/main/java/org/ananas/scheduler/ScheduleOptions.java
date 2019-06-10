package org.ananas.scheduler;

import java.io.Serializable;
import lombok.Data;
import org.ananas.runner.kernel.model.DagRequest;

@Data
public class ScheduleOptions implements Serializable {

  private static final long serialVersionUID = 6092906099009125985L;

  public String id; // schedule id
  public DagRequest dag;
  public TriggerOptions trigger;

  @Override
  public String toString() {
    return "ScheduleOptions{" + "id='" + id + '\'' + ", dag=" + dag + ", trigger=" + trigger + '}';
  }
}
