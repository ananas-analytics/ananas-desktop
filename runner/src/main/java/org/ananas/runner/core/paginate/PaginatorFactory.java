package org.ananas.runner.core.paginate;

import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.ananas.runner.core.common.VariableRender;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.errors.ExceptionHandler;
import org.ananas.runner.core.errors.ExceptionHandler.ErrorCode;
import org.ananas.runner.core.extension.ExtensionManager;
import org.ananas.runner.core.extension.ExtensionRegistry;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.model.Step;
import org.ananas.runner.core.model.StepType;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class PaginatorFactory implements Paginator {
  String id;
  String metadataId;
  String type;
  Map<String, Object> config;
  Dataframe dataframe;
  ExtensionManager extensionManager;

  private PaginatorFactory(String id, PaginationBody body, ExtensionManager extensionManager) {
    this(
        id,
        body.metadataId,
        body.type,
        VariableRender.renderConfig(body.params, body.config),
        body.dataframe,
        extensionManager);
  }

  private PaginatorFactory(
      String id,
      String metadataId,
      String type,
      Map<String, Object> config,
      Dataframe dataframe,
      ExtensionManager extensionManager) {
    this.id = id;
    this.type = type;
    this.metadataId = metadataId;
    this.config = config;
    this.dataframe = dataframe;
    this.extensionManager = extensionManager;
  }

  public static PaginatorFactory of(
      String id, PaginationBody body, ExtensionManager extensionManager) {
    Preconditions.checkNotNull(body.config, "config cannot be null");
    return new PaginatorFactory(id, body, extensionManager);
  }

  public static PaginatorFactory of(
      String id,
      String metadataId,
      String type,
      Map<String, Object> config,
      Dataframe dataframe,
      ExtensionManager extensionManager) {
    Preconditions.checkNotNull(config, "config cannot be null");
    return new PaginatorFactory(id, metadataId, type, config, dataframe, extensionManager);
  }

  public static PaginatorFactory of(
      String id,
      String metadataId,
      String type,
      Map<String, Object> config,
      Schema schema,
      ExtensionManager extensionManager) {
    Preconditions.checkNotNull(config, "config cannot be null");
    Dataframe dataframe = new Dataframe();
    dataframe.schema = org.ananas.runner.core.schema.Schema.of(schema);
    return new PaginatorFactory(id, metadataId, type, config, dataframe, extensionManager);
  }

  public AutoDetectedSchemaPaginator buildPaginator() {
    if (!ExtensionRegistry.hasPaginator(this.metadataId, extensionManager)) {
      throw new IllegalStateException("Unsupported source type '" + this.metadataId + "'");
    }
    Class<? extends AutoDetectedSchemaPaginator> clazz =
        ExtensionRegistry.getPaginator(this.metadataId, extensionManager);

    try {
      Constructor<? extends AutoDetectedSchemaPaginator> ctor =
          clazz.getDeclaredConstructor(String.class, String.class, Map.class, Schema.class);
      ctor.setAccessible(true);

      // get the schema here if user choose to use the schema from dataframe
      boolean forceSchemaAutodetect =
          (Boolean) this.config.getOrDefault(Step.FORCE_AUTODETECT_SCHEMA, false);
      Schema schema = null;

      if (!forceSchemaAutodetect
          && (dataframe != null && dataframe.schema != null)
          && StepType.from(this.type)
              .equals(StepType.Connector)) { // only avoid autodetect for connector
        schema = dataframe.schema.toBeamSchema();
        if (schema.getFieldCount() == 0) {
          schema = null;
        }
      }
      return ctor.newInstance(this.id, this.type, this.config, schema);
    } catch (InstantiationException | NoSuchMethodException | IllegalAccessException e) {
      throw new AnanasException(ExceptionHandler.ErrorCode.GENERAL, e.getLocalizedMessage());
    } catch (InvocationTargetException ex) {
      Throwable targetException = ex.getTargetException();
      if (targetException != null) {
        throw new AnanasException(
            ExceptionHandler.ErrorCode.GENERAL, targetException.getLocalizedMessage());
      } else {
        throw new AnanasException(ErrorCode.GENERAL);
      }
    }
  }

  @Override
  public MutablePair<Schema, Iterable<Row>> paginateRows(Integer page, Integer pageSize) {
    return buildPaginator().paginateRows(page, pageSize);
  }

  @Override
  public Dataframe paginate(Integer page, Integer pageSize) {
    return buildPaginator().paginate(page, pageSize);
  }
}
