package org.ananas.scheduler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.TriggerOptionsFactory;
import org.junit.Test;
import org.quartz.SchedulerException;

public class TestDefaultScheduler {
  @Test
  public void runDefaultScheduler() throws SchedulerException, IOException {
    DefaultScheduler scheduler = DefaultScheduler.of();
    scheduler.start();

    ScheduleOptions options = new ScheduleOptions();
    options.id = "test-trigger";
    options.dag = new DagRequest();
    options.trigger = TriggerOptionsFactory.repeat(UUID.randomUUID().toString(), System.currentTimeMillis(), 3);

    scheduler.schedule(options);

    ScheduleOptions op = scheduler.getTriggerById(options.id);

    assertEquals("test-trigger", op.id);

    List<ScheduleOptions> schedules = scheduler.getAllTriggers();

    assertEquals(1, schedules.size());
    assertEquals("test-trigger", schedules.get(0).id);

    try {
      Thread.sleep(10000);
      // get failed job
      scheduler.getJobsByTriggerId(options.id);
      scheduler.unschedule(options.id);
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    scheduler.stop();
  }
}
