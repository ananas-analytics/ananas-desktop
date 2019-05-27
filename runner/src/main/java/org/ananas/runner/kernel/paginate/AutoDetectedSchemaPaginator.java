package org.ananas.runner.kernel.paginate;

import java.util.Map;
import org.apache.beam.sdk.schemas.Schema;

/** AutoDetectedSchemaPaginator detects the schema if the initial one is null */
public abstract class AutoDetectedSchemaPaginator extends AbstractPaginator {
  public String id;
  public Map<String, Object> config;

  public AutoDetectedSchemaPaginator(String id, Map<String, Object> config, Schema schema) {
    super(id, schema);
    this.id = id;
    this.config = config;

    this.parseConfig(config);

    if (this.schema == null) {
      this.schema = this.autodetect();
    }
  }

  public abstract Schema autodetect();

  public abstract void parseConfig(Map<String, Object> config);
}
