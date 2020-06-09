package org.ananas.runner.core.paginate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.ananas.runner.core.model.LoopStepConfigGenerator;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public abstract class LoopedAutoDectectedSchemaPaginator extends AutoDetectedSchemaPaginator {
  protected LoopStepConfigGenerator configGenerator;
  protected Schema rawSchema;

  public LoopedAutoDectectedSchemaPaginator(
      String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  public abstract Schema autodetectRawSchema();

  public abstract Iterable<Row> iterateRawRows(Integer page, Integer pageSize);

  @Override
  public void parseConfig(Map<String, Object> config) {
    this.configGenerator = new LoopStepConfigGenerator(config);
  }

  @Override
  public Schema autodetect() {
    rawSchema = this.autodetectRawSchema();
    Schema.Builder builder = Schema.builder();
    rawSchema
        .getFields()
        .forEach(
            field -> {
              builder.addField(field);
            });
    // now add loop fields
    configGenerator
        .toBeamFields()
        .forEach(
            field -> {
              builder.addField(field);
            });
    return builder.build();
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    Iterable<Row> rawRows = iterateRawRows(page, pageSize);
    return StreamSupport.stream(rawRows.spliterator(), false)
        .map(
            row -> {
              List<Object> values = row.getValues();
              this.configGenerator
                  .getCurrentCondition()
                  .forEach(
                      l -> {
                        values.add(l.toRowValue());
                      });
              return Row.withSchema(this.schema).addValues(values).build();
            })
        .collect(Collectors.toList());
  }
}
