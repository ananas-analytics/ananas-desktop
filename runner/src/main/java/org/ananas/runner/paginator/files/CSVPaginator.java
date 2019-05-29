package org.ananas.runner.paginator.files;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.kernel.schema.SchemaAutodetect;
import org.ananas.runner.kernel.schema.StringFieldAutodetect;
import org.apache.beam.sdk.extensions.sql.impl.schema.BeamTableUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVPaginator extends AutoDetectedSchemaPaginator {
  public static final String CONFIG_URL = "path";
  public static final String CONFIG_HEADER = "header";
  public static final String CONFIG_DELIMITER = "delimiter";
  public static final String CONFIG_RECORD_SEPARATOR = "recordSeparator";

  private String url;
  private boolean hasHeader;
  private char delimiter;
  private String recordSeparator;

  private CSVFormat format;

  public CSVPaginator(String id, Map<String, Object> config, Schema schema) {
    super(id, config, schema);
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    this.url = (String) config.get(CONFIG_URL);
    this.hasHeader = (Boolean) config.getOrDefault(CONFIG_HEADER, false);
    this.delimiter =
        config.containsKey(CONFIG_DELIMITER)
            ? (char) config.get(CONFIG_DELIMITER)
            : CSVFormat.DEFAULT.getDelimiter();
    this.recordSeparator =
        config.get(CONFIG_RECORD_SEPARATOR) == null
            ? CSVFormat.DEFAULT.getRecordSeparator()
            : (String) config.get(CONFIG_RECORD_SEPARATOR);

    this.format =
        CSVFormat.DEFAULT.withDelimiter(this.delimiter).withRecordSeparator(this.recordSeparator);
  }

  @Override
  public Schema autodetect() {
    List<String> lines = PageProcessor.readFile(this.url, 0, DEFAULT_LIMIT, (e, i) -> e);
    Schema inputschema;
    try {
      Iterator<String> it = lines.iterator();
      Map<Integer, String> headerReversedMap = new HashMap<>();
      if (hasHeader) {
        String header = it.next();
        CSVParser headerParser = CSVParser.parse(header, format);
        CSVRecord headerRecord = headerParser.iterator().next();
        for (int i = 0; i < headerRecord.size(); i++) {
          headerReversedMap.put(i, headerRecord.get(i));
        }
      }
      inputschema = withoutHeader(it, this.format, headerReversedMap);
    } catch (Exception e) {
      throw new RuntimeException("Can't parse CSV file " + url + " :" + e.getMessage());
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
        this.url,
        page,
        pageSize,
        (e, i) -> {
          if (page == 0 && hasHeader && i == 0) {
            return null;
          }
          Iterator<Row> it =
              BeamTableUtils.csvLines2BeamRows(this.format, e, this.schema).iterator();
          return it.hasNext() ? it.next() : null;
        });
  }
}
