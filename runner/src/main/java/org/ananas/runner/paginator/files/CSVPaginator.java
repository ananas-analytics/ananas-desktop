package org.ananas.runner.paginator.files;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.schema.SchemaAutodetect;
import org.ananas.runner.kernel.schema.StringFieldAutodetect;
import org.ananas.runner.steprunner.files.csv.CSVStepConfig;
import org.apache.beam.sdk.extensions.sql.impl.schema.BeamTableUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVPaginator extends AutoDetectedSchemaPaginator {
  private CSVStepConfig csvStepConfig;
  private CSVFormat format;

  public CSVPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    csvStepConfig = new CSVStepConfig(StepType.from(type), config);
    this.format =
        CSVFormat.DEFAULT
            .withDelimiter(csvStepConfig.delimiter)
            .withRecordSeparator(csvStepConfig.recordSeparator);
  }

  @Override
  public Schema autodetect() {
    List<String> lines = PageProcessor.readFile(csvStepConfig.url, 0, DEFAULT_LIMIT, (e, i) -> e);
    Schema inputschema;
    try {
      Iterator<String> it = lines.iterator();
      Map<Integer, String> headerReversedMap = new HashMap<>();
      if (csvStepConfig.hasHeader) {
        String header = it.next();
        CSVParser headerParser = CSVParser.parse(header, format);
        CSVRecord headerRecord = headerParser.iterator().next();
        for (int i = 0; i < headerRecord.size(); i++) {
          headerReversedMap.put(i, headerRecord.get(i));
        }
      }
      inputschema = withoutHeader(it, this.format, headerReversedMap);
    } catch (Exception e) {
      throw new RuntimeException(
          "Can't parse CSV file " + csvStepConfig.url + " :" + e.getMessage());
    }
    return inputschema;
  }

  private Schema withoutHeader(Iterator<String> it, CSVFormat format, Map<Integer, String> m)
      throws IOException {
    SchemaAutodetect<String> autodetecter = StringFieldAutodetect.of();
    while (it.hasNext()) {
      try {
        CSVParser parser = CSVParser.parse(it.next(), format);
        for (CSVRecord csvRecord : parser) {
          Iterator<String> iti = csvRecord.iterator();
          int i = 0;
          while (iti.hasNext()) {
            autodetecter.add(i, m.get(i) == null ? "col_" + i : m.get(i), iti.next());
            i++;
          }
        }
      } catch (Exception e) {
      }
    }
    return autodetecter.autodetect();
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return PageProcessor.readFile(
        csvStepConfig.url,
        page,
        pageSize,
        (e, i) -> {
          if (page == 0 && csvStepConfig.hasHeader && i == 0) {
            return null;
          }
          try {
            Iterator<Row> it =
                BeamTableUtils.csvLines2BeamRows(this.format, e, this.schema).iterator();
            return it.hasNext() ? it.next() : null;
          } catch (Exception ex) {
            return null;
          }
        });
  }
}
