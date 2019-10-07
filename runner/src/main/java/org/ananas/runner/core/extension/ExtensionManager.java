package org.ananas.runner.core.extension;

import java.io.IOException;
import java.util.Map;

public interface ExtensionManager {
  void load();

  void load(String repo);

  Map<String, StepMetadata> getAllStepMetadata();

  void loadExtension(String path) throws IOException;

  boolean hasStepMetadata(String metadataId);

  StepMetadata getStepMetadata(String metadataId);

  void reset();
}
