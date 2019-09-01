package org.ananas.runner.steprunner.subprocess;

import com.google.common.collect.Lists;
import java.util.List;

/** Parameters to the sub-process, has tuple of ordinal position and the value. */
public class SubProcessCommandLineArgs {

  // Parameters to pass to the sub-process
  private List<Command> parameters = Lists.newArrayList();

  public void addCommand(Integer position, String value) {
    parameters.add(new Command(position, value));
  }

  public void putCommand(Command command) {
    parameters.add(command);
  }

  public List<Command> getParameters() {
    return parameters;
  }

  /** Class used to store the SubProcces parameters. */
  public static class Command {

    // The ordinal position of the command to pass to the sub-process
    int ordinalPosition;
    String value;

    @SuppressWarnings("unused")
    private Command() {}

    public Command(int ordinalPosition, String value) {
      this.ordinalPosition = ordinalPosition;
      this.value = value;
    }

    public int getKey() {
      return ordinalPosition;
    }

    public void setKey(int key) {
      this.ordinalPosition = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
