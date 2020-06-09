package org.ananas.runner.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.schemas.Schema;

public class DataSourceLoop {
  /* loop fields */
  public static String NAME = "name";
  public static String TYPE = "type"; // LoopType
  public static String VALUES = "values"; // only available for enum type
  public static String BEGIN = "begin";
  public static String END = "end";
  public static String STEP = "step";

  private List<Loop> loops = new ArrayList<>();

  public void addLoop(Map<String, Object> metadata) {
    try {
      LoopType type = LoopType.from((String) metadata.getOrDefault(TYPE, "number"));
      if (type.equals(LoopType.Number)) {
        addNumberLoop(metadata);
      } else if (type.equals(LoopType.Enum)) {

      } else if (type.equals(LoopType.Date)) {

      }
    } catch (Exception e) {
      // just ignore the loop
    }
  }

  public int size() {
    return loops.size();
  }

  public Loop get(int n) {
    return loops.get(n);
  }

  public List<Loop> getAll() {
    return this.loops;
  }

  private void addNumberLoop(Map<String, Object> metadata) {
    Loop loop =
        new NumberLoop(
            (String) metadata.get(NAME),
            (Integer) metadata.get(BEGIN),
            (Integer) metadata.get(END),
            (Integer) metadata.get(STEP));
    loops.add(loop);
  }

  public abstract static class Loop {
    public String name;
    public LoopType type;
    public String value;

    public abstract void reset();

    public abstract String next();

    public abstract boolean hasNext();

    public abstract Schema.Field toBeamField();

    public abstract Object toRowValue();

    public String getValue() {
      return value;
    }
  }

  static class NumberLoop extends Loop {
    public int begin;
    public int end;
    public int step;

    public NumberLoop(String name, int begin, int end, int step) {
      this.name = name;
      this.type = LoopType.Number;
      this.begin = begin;
      this.end = end;
      this.step = step;
      this.value = Integer.toString(begin);
    }

    @Override
    public void reset() {
      this.value = Integer.toString(begin);
    }

    @Override
    public boolean hasNext() {
      return Integer.parseInt(value) < end;
    }

    @Override
    public String next() {
      int current = Integer.parseInt(value);
      value = Integer.toString(current + step);
      return value;
    }

    @Override
    public Schema.Field toBeamField() {
      return Schema.Field.of(this.name, Schema.FieldType.INT32);
    }

    @Override
    public Object toRowValue() {
      return Integer.valueOf(this.value);
    }
  }

  /*
    static class EnumLoop {
      public List<String> values;
      public int current;
    }

    static class DateLoop {
      public int begin;
      public int end;
      public String timezone;
      public String step;
    }
  */

  public enum LoopType {
    Number,
    Enum,
    Date;

    public static LoopType from(String v) {
      for (LoopType t : LoopType.values()) {
        if (t.name().toLowerCase().equals(v.toLowerCase())) {
          return t;
        }
      }
      throw new RuntimeException("cannot find loop type for value " + v);
    }
  }
}
