package org.ananas.scheduler;

import java.io.IOException;
import org.quartz.SchedulerException;

public interface Scheduler {

  public void start() throws SchedulerException;

  public void stop() throws SchedulerException;

  public void schedule(ScheduleOptions option);

  public void unschedule(String id);

  public ScheduleOptions getTriggerById(String group) throws SchedulerException, IOException;
}
