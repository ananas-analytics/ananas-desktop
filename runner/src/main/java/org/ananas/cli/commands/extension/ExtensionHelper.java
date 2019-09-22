package org.ananas.cli.commands.extension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.ananas.runner.core.extension.ExtensionManager;

public class ExtensionHelper {
  public static int loadExtensions(File repo, List<File> extensions) {
    if (repo != null) {
      ExtensionManager.getInstance().load(repo.getAbsolutePath());
    } else {
      ExtensionManager.getInstance().load();
    }

    AtomicInteger ret = new AtomicInteger();
    if (extensions != null) {
      extensions.forEach(
          ext -> {
            try {
              ExtensionManager.getInstance().loadStepExtension(ext.getAbsolutePath());
            } catch (IOException e) {
              System.err.println("Failed to load extension: " + ext.getAbsolutePath());
              ret.set(1);
            }
          });
    }
    return ret.get();
  }
}
