package org.ananas.scheduler;

import java.util.Date;

public class Job {
  public String id; // job id
  public String triggerId; // trigger id, could be null
  // duplicate the triggerType here to avoid join with trigger table, as it might be managed by
  // other library (ex. quartz)
  public String triggerType;
  public String state; // job state
  public String message; // message attached to the job
  public byte[] dag; // dag data
  public byte[] trigger; // trigger data
  public Date createAt;
  public Date updateAt;
}
