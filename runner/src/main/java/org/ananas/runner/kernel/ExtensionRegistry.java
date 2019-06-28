package org.ananas.runner.kernel;

import org.ananas.runner.kernel.build.StepBuilder;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.paginate.PaginatorFactory;

public class ExtensionRegistry {

  public static void registerConnector(
      String metaId,
      Class<? extends StepRunner> stepRunnerClass,
      Class<? extends AutoDetectedSchemaPaginator> paginatorClass) {
    StepBuilder.register(metaId, stepRunnerClass);
    PaginatorFactory.register(metaId, paginatorClass);
  }

  public static void registerTransformer(
      String metaId, Class<? extends StepRunner> stepRunnerClass) {
    StepBuilder.register(metaId, stepRunnerClass);
  }

  public static void registerLoader(
      String metaId,
      Class<? extends StepRunner> stepRunnerClass,
      Class<? extends AutoDetectedSchemaPaginator> paginatorClass) {
    StepBuilder.register(metaId, stepRunnerClass);
    PaginatorFactory.register(metaId, paginatorClass);
  }

  public static void registerViewer(String metaId, Class<? extends StepRunner> stepRunnerClass) {
    StepBuilder.register(metaId, stepRunnerClass);
  }

}
