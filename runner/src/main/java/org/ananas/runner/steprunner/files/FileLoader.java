package org.ananas.runner.steprunner.files;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.ananas.runner.kernel.LoaderStepRunner;
import org.ananas.runner.kernel.StepRunner;
import org.ananas.runner.kernel.model.Step;
import org.ananas.runner.misc.AsJsons;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.values.Row;

public class FileLoader extends LoaderStepRunner {

  private static final long serialVersionUID = -752673440816004416L;

  // general configs
  public static final String CONFIG_PATH = "path";
  public static final String CONFIG_PREFIX = "prefix";
  public static final String CONFIG_FORMAT = "format";
  public static final String CONFIG_SHARD = "shard";

  // config specific for CSV
  public static final String CONFIG_HEADER = "header";

  public enum SupportedFormat {
    JSON,
    CSV,
    TXT
  }

  public FileLoader(Step step, StepRunner previous, boolean isTest) {
    super(step, previous, isTest);
  }

  public void build() {
    String directory = (String) step.config.get(CONFIG_PATH);
    String prefix = (String) step.config.get(CONFIG_PREFIX);
    SupportedFormat format =
        SupportedFormat.valueOf(((String) step.config.get(CONFIG_FORMAT)).toUpperCase());
    String shardStr = (String) step.config.get(CONFIG_SHARD);

    super.output = previous.getOutput();

    if (!Files.exists(FileSystems.getDefault().getPath(directory))) {
      throw new RuntimeException("Can't find directory " + directory);
    }

    if (isTest) {
      return;
    }

    int shard = shardStr == null || shardStr.length() == 0 ? 1 : Integer.valueOf(shardStr);

    switch (format) {
      case JSON:
        this.output
            .apply(AsJsons.of(this.errors))
            .apply(
                FileIO.<String>write()
                    .via(new TextFileSink())
                    .to(directory)
                    .withPrefix(prefix)
                    .withSuffix("." + format.name().toLowerCase())
                    .withNumShards(shard));
        break;
      case CSV:
        this.output.apply(
            FileIO.<Row>write()
                .via(new CSVFileSink(true, previous.getSchema().getFieldNames()))
                .withNumShards(shard)
                .to(directory)
                .withPrefix(prefix)
                .withSuffix("." + format.name().toLowerCase()));
        break;
      default:
        throw new RuntimeException("not supported loader format " + format);
    }
  }

  public static class TextFileSink implements FileIO.Sink<String> {
    private static final long serialVersionUID = 1853551573522693151L;
    private PrintWriter writer;

    public TextFileSink() {}

    @Override
    public void open(WritableByteChannel channel) throws IOException {
      this.writer = new PrintWriter(Channels.newOutputStream(channel));
    }

    @Override
    public void write(String element) throws IOException {
      this.writer.println(element);
    }

    @Override
    public void flush() throws IOException {
      this.writer.flush();
    }
  }

  public static class CSVFileSink implements FileIO.Sink<Row> {
    private static final long serialVersionUID = -3687387332727000370L;
    private boolean withHeader;
    private String header;
    private PrintWriter writer;

    public CSVFileSink(boolean withHeader, List<String> headers) {
      this.withHeader = withHeader;
      this.header = Joiner.on(",").join(headers);
    }

    @Override
    public void open(WritableByteChannel channel) throws IOException {
      this.writer = new PrintWriter(Channels.newOutputStream(channel));
      if (this.withHeader) {
        this.writer.println(this.header);
      }
    }

    @Override
    public void write(Row element) throws IOException {
      List<String> strings = new ArrayList<>();
      for (int i = 0; i < element.getSchema().getFields().size(); i++) {
        if (element.getSchema().getField(i).getType().getTypeName().isStringType()) {
          StringBuilder sb = new StringBuilder();
          sb.append('"');
          sb.append(
              element.getValue(i) == null ? "" : element.getValue(i).toString().replace(',', ' '));
          sb.append('"');
          strings.add(sb.toString());
        } else {
          strings.add(element.getValue(i) == null ? "" : element.getValue(i).toString());
        }
      }
      this.writer.println(Joiner.on(",").join(strings));
    }

    @Override
    public void flush() throws IOException {
      this.writer.flush();
    }
  }
}
