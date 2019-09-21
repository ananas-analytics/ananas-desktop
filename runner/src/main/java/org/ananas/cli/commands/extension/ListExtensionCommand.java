package org.ananas.cli.commands.extension;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.extension.StepMetadata;
import picocli.CommandLine;

@CommandLine.Command(name = "list", description = "List installed extensions")
public class ListExtensionCommand implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-r", "--repo"},
      description = "Extension repository location, by default, <ANANAS_WORKSPACE>/extensions")
  private File repo;

  @Override
  public Integer call() throws Exception {
    if (repo != null) {
      ExtensionManager.getInstance().load(repo.getAbsolutePath());
    } else {
      ExtensionManager.getInstance().load();
    }

    Map<String, StepMetadata> result = ExtensionManager.getInstance().getAllStepMetadata();
    System.out.println(result);
    return 0;
  }
}
