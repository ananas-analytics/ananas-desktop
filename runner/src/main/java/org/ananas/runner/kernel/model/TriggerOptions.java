package org.ananas.runner.kernel.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class TriggerOptions implements Serializable {
  private static final long serialVersionUID = 6595071906502645423L;

  public static final String ONCE = "once";
  public static final String REPEAT = "repeat";
  public static final String HOURLY = "hourly";
  public static final String DAILY = "daily";
  public static final String WEEKLY = "weekly";
  public static final String MONTHLY = "monthly";

  public String id;
  public String type;
  public int interval;
  public int hour;
  public int minute;
  public int dayOfWeek;
  public int dayOfMonth;

  public long startTimestamp;
}
