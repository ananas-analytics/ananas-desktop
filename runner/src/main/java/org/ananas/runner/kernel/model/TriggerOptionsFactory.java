package org.ananas.runner.kernel.model;

import java.util.UUID;

public class TriggerOptionsFactory {
  public static TriggerOptions runOnceNow() {
    return once(UUID.randomUUID().toString(), System.currentTimeMillis());
  }

  public static TriggerOptions once(String id, long start) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.ONCE;
    options.startTimestamp = start;
    return options;
  }

  public static TriggerOptions repeat(String id, long start, int intervalInSecond) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.REPEAT;
    options.interval = intervalInSecond;
    options.startTimestamp = start;
    return options;
  }

  public static TriggerOptions hourly(String id, long start, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.HOURLY;
    options.minute = minute;
    options.startTimestamp = start;
    return options;
  }

  public static TriggerOptions daily(String id, long start, int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.DAILY;
    options.hour = hour;
    options.minute = minute;
    options.startTimestamp = start;
    return options;
  }

  public static TriggerOptions weekly(String id, long start, int dayOfWeek, int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.WEEKLY;
    options.dayOfWeek = dayOfWeek;
    options.hour = hour;
    options.minute = minute;
    options.startTimestamp = start;
    return options;
  }


  public static TriggerOptions monthly(String id, long start, int dayOfMonth, int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.id = id;
    options.type = TriggerOptions.MONTHLY;
    options.dayOfMonth = dayOfMonth;
    options.hour = hour;
    options.minute = minute;
    options.startTimestamp = start;
    return options;
  }


}
