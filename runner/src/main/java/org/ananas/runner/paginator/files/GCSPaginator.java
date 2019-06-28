package org.ananas.runner.paginator.files;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.ananas.runner.kernel.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.steprunner.files.utils.HomeManager;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.fs.MatchResult;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.jboss.netty.buffer.ChannelBufferInputStream;


public class GCSPaginator extends AutoDetectedSchemaPaginator {
  private AutoDetectedSchemaPaginator delegate;
  public GCSPaginator(String id,
    String type,
    Map<String, Object> config,
    Schema schema) {
    super(id, type, config, schema);
  }

  private String getSampleFileUrl(Map<String, Object> config) throws IOException {
    String url = StepFileConfigToUrl.gcsSourceUrl(config);
    FileSystems.setDefaultPipelineOptions(PipelineOptionsFactory.create());

    // Find Bucket
    final AtomicReference<ResourceId> sampleResourceId = new AtomicReference<>();
    long minSize = Long.MAX_VALUE;
    MatchResult listResult = FileSystems.match(url);
    listResult
      .metadata()
      .forEach(
        metadata -> {
          long size = metadata.sizeBytes();
          if (minSize > size && size > 0) {
            sampleResourceId.set(metadata.resourceId());
          }
        });

    // check if we already downloaded it
    String hash = Hashing.sipHash24().hashString(sampleResourceId.get().toString(), StandardCharsets.UTF_8)
      .toString();
    File sampleFile = new File(HomeManager.getHomeFilePath(this.id + "_" + hash + ".csv"));
    if (sampleFile.exists() && config.getOrDefault("resampling", Boolean.FALSE).equals(Boolean.FALSE)) {
      return sampleFile.getAbsolutePath();
    }

    // if not, download it
    float limitSize = Float.parseFloat((String)config.getOrDefault("sampleSize", "5"));
    ReadableByteChannel readerChannel = FileSystems.open(sampleResourceId.get());
    InputStream in = ByteStreams.limit(Channels.newInputStream(readerChannel), (int) (limitSize * 1024));
    ByteStreams.copy(in, new FileOutputStream(sampleFile));
    return sampleFile.getAbsolutePath();
  }

  @Override
  public Schema autodetect() {
    return this.delegate.autodetect();
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    String format = (String) config.getOrDefault("format", "text");
    switch(format) {
      case "csv":
        Map<String, Object> csvConfig = new HashMap<>();
        // call google cloud api to download example file
        try {
          csvConfig.put("path", getSampleFileUrl(config));
        } catch (IOException e) {
          // TODO: handle error
          e.printStackTrace();
        }
        csvConfig.put("header", config.get("header"));
        delegate = new CSVPaginator(this.id, this.type, csvConfig, this.schema);
    }
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return this.delegate.iterateRows(page, pageSize);
  }
}
