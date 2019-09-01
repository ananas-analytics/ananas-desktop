package org.ananas.runner.steprunner.gcs;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import org.ananas.runner.core.model.StepType;
import org.ananas.runner.core.paginate.AutoDetectedSchemaPaginator;
import org.ananas.runner.misc.HomeManager;
import org.ananas.runner.steprunner.files.csv.CSVPaginator;
import org.ananas.runner.steprunner.files.json.JsonPaginator;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.com.google.common.collect.Lists;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.fs.MatchResult;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class GCSPaginator extends AutoDetectedSchemaPaginator {

  private AutoDetectedSchemaPaginator delegate;

  public GCSPaginator(String id, String type, Map<String, Object> config, Schema schema) {
    super(id, type, config, schema);
  }

  public String getSampleFileUrl(Map<String, Object> config) throws IOException {
    String url = "";
    if (StepType.from(type) == StepType.Connector) {
      url = StepFileConfigToUrl.gcsSourceUrl(config);
    } else {
      // loader
      url = StepFileConfigToUrl.gcsDestinationUrlPatternWithPrefix(config);
    }
    FileSystems.setDefaultPipelineOptions(PipelineOptionsFactory.create());

    // Find Bucket
    final AtomicReference<ResourceId> sampleResourceId = new AtomicReference<>();
    AtomicLong minSize = new AtomicLong(Long.MAX_VALUE);
    MatchResult listResult = FileSystems.match(url);
    listResult
        .metadata()
        .forEach(
            metadata -> {
              long size = metadata.sizeBytes();
              if (minSize.get() > size && size > 0) {
                minSize.set(size);
                sampleResourceId.set(metadata.resourceId());
              }
            });

    // check if we already downloaded it
    String hash =
        Hashing.sipHash24()
            .hashString(sampleResourceId.get().toString(), StandardCharsets.UTF_8)
            .toString();
    String filename = sampleResourceId.get().getFilename();
    // String extension = FilenameUtils.getExtension(filename);
    String extension = Files.getFileExtension(filename);
    File sampleFile = new File(HomeManager.getHomeFilePath(this.id + "_" + hash + "." + extension));
    if (sampleFile.exists()
        && config.getOrDefault("resampling", Boolean.FALSE).equals(Boolean.FALSE)) {
      return sampleFile.getAbsolutePath();
    }

    // if not, download it to temp file
    List<String> compressionExts = Lists.asList("gz", new String[] {"zip"});
    File tempFile = File.createTempFile("ananas", ".tmp");
    float limitSize = Float.parseFloat((String) config.getOrDefault("sampleSize", "0"));
    ReadableByteChannel readerChannel = FileSystems.open(sampleResourceId.get());
    if (limitSize > 0 && !compressionExts.contains(extension)) {
      InputStream in =
          ByteStreams.limit(Channels.newInputStream(readerChannel), (int) (limitSize * 1024));
      ByteStreams.copy(in, new FileOutputStream(tempFile));
    } else {
      ByteStreams.copy(Channels.newInputStream(readerChannel), new FileOutputStream(tempFile));
    }

    // decompression if necessary
    if (compressionExts.contains(extension)) {
      decompression(extension, tempFile, sampleFile);
    } else {
      // move to sample file
      // TODO: check result?
      tempFile.renameTo(sampleFile);
    }

    return sampleFile.getAbsolutePath();
  }

  private void decompression(String ext, File src, File dest) throws IOException {
    switch (ext) {
      case "zip":
        unzip(src, dest);
        break;
      case "gz":
      default:
        gunzip(src, dest);
    }
  }

  private void gunzip(File src, File dest) throws IOException {
    byte[] buffer = new byte[1024];

    GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(src));

    FileOutputStream out = new FileOutputStream(dest);

    int len;
    while ((len = gzis.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }

    gzis.close();
    out.close();
  }

  private void unzip(File src, File dest) {
    // TODO: implement unzip
  }

  @Override
  public Schema autodetect() {
    return this.delegate.autodetect();
  }

  @Override
  public void parseConfig(Map<String, Object> config) {
    String format = (String) config.getOrDefault("format", "text");
    switch (format) {
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
        break;
      case "json":
        Map<String, Object> jsonConfig = new HashMap<>();
        // call google cloud api to download example file
        try {
          jsonConfig.put("path", getSampleFileUrl(config));
        } catch (IOException e) {
          // TODO: handle error
          e.printStackTrace();
        }
        delegate = new JsonPaginator(this.id, this.type, jsonConfig, this.schema);
        break;
    }
  }

  @Override
  public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
    return this.delegate.iterateRows(page, pageSize);
  }
}
