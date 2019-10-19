package org.ananas.runner.core.extension;

import java.util.Map;
import org.ananas.runner.core.model.Extension;

public interface ExtensionManager {
  void resolve(Map<String, Extension> manifest);

  Map<String, StepMetadata> getAllStepMetadata();

  boolean hasStepMetadata(String metadataId);

  StepMetadata getStepMetadata(String metadataId);

  void reset();
}
