package org.ananas.runner.model.steps.files.csv;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ananas.runner.kernel.errors.ErrorHandler;
import org.ananas.runner.model.schema.SchemaAutodetect;
import org.ananas.runner.model.schema.StringFieldAutodetect;
import org.ananas.runner.model.steps.commons.paginate.PageProcessor;
import org.ananas.runner.model.steps.commons.paginate.Paginator;
import org.ananas.runner.model.steps.files.AbstractFilePaginator;
import org.apache.beam.sdk.extensions.sql.impl.schema.BeamTableUtils;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVPaginator extends AbstractFilePaginator implements Paginator {
  CSVStepConfig csvConfig;

  ErrorHandler handler;
  CSVFormat format;

  public CSVPaginator(String id, CSVStepConfig csvConfig) {
    super(id, csvConfig.url);
    this.csvConfig = csvConfig;
    this.handler = new ErrorHandler();
    this.format =
        CSVFormat.DEFAULT
            .withDelimiter(this.csvConfig.delimiter)
            .withRecordSeparator(this.csvConfig.recordSeparator);
    this.schema = autodetect(DEFAULT_LIMIT);
  }

  @Override
  protected Schema autodetect(Integer pageSize) {
    List<String> lines = PageProcessor.readFile(this.url, 0, pageSize, (e, i) -> e);
    Schema inputschema;
    try {
      Iterator<String> it = lines.iterator();
      Map<Integer, String> headerReversedMap = new HashMap<>();
      if (this.csvConfig.hasHeader) {
        String header = it.next();
        CSVParser headerParser = CSVParser.parse(header, this.format);
        CSVRecord headerRecord = headerParser.iterator().next();
        for (int i = 0; i < headerRecord.size(); i++) {
          headerReversedMap.put(i, headerRecord.get(i));
        }
      }
      inputschema = withoutHeader(it, this.format, headerReversedMap);
    } catch (Exception e) {
      throw new RuntimeException(
          "Can't parse CSV file " + this.csvConfig.url + " :" + e.getMessage());
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
          if (page == 0 && this.csvConfig.hasHeader && i == 0) {
            return null;
          }
          Iterator<Row> it =
              BeamTableUtils.csvLines2BeamRows(this.format, e, this.schema).iterator();
          return it.hasNext() ? it.next() : null;
        });
  }
}
