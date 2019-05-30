package org.ananas.runner.kernel.paginate;

import java.util.Map;
import org.apache.beam.sdk.schemas.Schema;

/** AutoDetectedSchemaPaginator detects the schema if the initial one is null */
public abstract class AutoDetectedSchemaPaginator extends AbstractPaginator {
  protected String id;
  protected String type;
  protected Map<String, Object> config;

  public AutoDetectedSchemaPaginator(
      String id, String type, Map<String, Object> config, Schema schema) {
    super(id, schema);
    this.id = id;
    this.type = type;
    this.config = config;

    this.parseConfig(config);

    if (this.schema == null) {
      this.schema = this.autodetect();
    }
  }

  public abstract Schema autodetect();

  public abstract void parseConfig(Map<String, Object> config);
}
