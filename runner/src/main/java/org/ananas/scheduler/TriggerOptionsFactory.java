package org.ananas.scheduler;


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TriggerOptionsFactory {
  public static TriggerOptions repeatInSeconds(int interval) {
    TriggerOptions options = new TriggerOptions();
    options.type = TriggerOptions.SECONDS;
    options.interval = interval;
    return options;
  }

  public static TriggerOptions hourly() {
    TriggerOptions options = new TriggerOptions();
    options.type = TriggerOptions.HOURLY;
    return options;
  }

  public static TriggerOptions daily(int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.type = TriggerOptions.DAILY;
    options.hour = hour;
    options.minute = minute;
    return options;
  }

  public static TriggerOptions weekly(int dayOfWeek, int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.type = TriggerOptions.WEEKLY;
    options.dayOfWeek = dayOfWeek;
    options.hour = hour;
    options.minute = minute;
    return options;
  }

  public static TriggerOptions monthly(int dayOfMonth, int hour, int minute) {
    TriggerOptions options = new TriggerOptions();
    options.type = TriggerOptions.MONTHLY;
    options.dayOfMonth = dayOfMonth;
    options.hour = hour;
    options.minute = minute;
    return options;
  }


  public static void startNow(TriggerOptions options) {
    Date date = new Date();

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    options.startYear = calendar.get(Calendar.YEAR);
    options.startMonth = calendar.get(Calendar.MONTH) + 1;
    options.startDay = calendar.get(Calendar.DAY_OF_MONTH);
    options.startHour = calendar.get(Calendar.HOUR_OF_DAY);
    options.startMinute = (calendar.get(Calendar.MINUTE)) % 60;
  }
}
