package org.ananas.runner.kernel.paginate;

import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.ananas.runner.kernel.common.VariableRender;
import org.ananas.runner.kernel.errors.AnanasException;
import org.ananas.runner.kernel.errors.ExceptionHandler;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.Step;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class PaginatorFactory implements Paginator {
  private static final Map<String, Class<? extends AutoDetectedSchemaPaginator>> REGISTRY =
      new HashMap<>();

  String id;
  String metadataId;
  Map<String, Object> config;
  Dataframe dataframe;

  public static void register(
      String metadataId, Class<? extends AutoDetectedSchemaPaginator> paginatorClass) {
    REGISTRY.put(metadataId, paginatorClass);
  }

  private PaginatorFactory(String id, PaginationBody body) {
    this.id = id;
    this.metadataId = body.metadataId;
    this.config = VariableRender.renderConfig(body.params, body.config);
    this.dataframe = body.dataframe;
  }

  public static PaginatorFactory of(String id, PaginationBody body) {
    Preconditions.checkNotNull(body.config, "config cannot be null");
    return new PaginatorFactory(id, body);
  }

  public Paginator buildPaginator() {
    if (!REGISTRY.containsKey(this.metadataId)) {
      throw new IllegalStateException("Unsupported source type '" + this.metadataId + "'");
    }
    Class<? extends AutoDetectedSchemaPaginator> clazz = REGISTRY.get(this.metadataId);

    try {
      Constructor<? extends AutoDetectedSchemaPaginator> ctor =
          clazz.getDeclaredConstructor(String.class, Map.class, Schema.class);
      ctor.setAccessible(true);
      // get the schema here if user choose to use the schema from dataframe
      boolean forceSchemaAutodetect =
        (Boolean)this.config.getOrDefault(Step.FORCE_AUTODETECT_SCHEMA, false);
      Schema schema = null;
      if (!forceSchemaAutodetect && (dataframe != null && dataframe.schema != null)) {
        schema = dataframe.schema.toBeamSchema();
      }
      return ctor.newInstance(this.id, this.config, schema);
    } catch (InstantiationException
        | NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      System.out.println(e.getMessage());
      throw new AnanasException(ExceptionHandler.ErrorCode.GENERAL, e.getLocalizedMessage());
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
