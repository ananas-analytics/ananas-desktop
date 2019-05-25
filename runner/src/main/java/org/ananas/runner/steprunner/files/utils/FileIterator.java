package org.ananas.runner.steprunner.files.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.beam.sdk.io.CompressedSource;
import org.apache.beam.sdk.io.Compression;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class FileIterator {

  private InputStream io;
  private LineIterator it;

  public FileIterator(String uri, File f) {
    this.io = null;
    if (CompressedSource.CompressionMode.isCompressed(uri)) {
      Compression compression = Compression.detect(uri);
      switch (compression) {
        case ZIP:
          try {
            final ZipFile zipFile = new ZipFile(uri);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            this.io = zipFile.getInputStream(entries.nextElement());
            this.it = IOUtils.lineIterator(this.io, Charsets.toCharset("UTF-8"));
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case GZIP:
          try {
            FileInputStream fis = new FileInputStream(uri);
            this.io = new GZIPInputStream(fis);
            this.it = IOUtils.lineIterator(this.io, Charsets.toCharset("UTF-8"));
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case DEFLATE:
          try {
            FileInputStream fis2 = new FileInputStream(uri);
            this.io = new InflaterInputStream(fis2);
            this.it = IOUtils.lineIterator(this.io, Charsets.toCharset("UTF-8"));
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        default:
          throw new RuntimeException(
              "compression "
                  + compression
                  + " not yet supported. Please decompress your files to proceed. ");
      }
    } else {
      try {
        FileInputStream io = new FileInputStream(f.getPath());
        this.it = IOUtils.lineIterator(io, Charsets.toCharset("UTF-8"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public LineIterator iterator() {
    return this.it;
  }

  public void close() {
    try {
      if (this.io != null) {
        this.io.close();
      }
    } catch (IOException e) {
    }
    try {
      if (this.it != null) {
        this.it.close();
      }
    } catch (IOException e) {
    }
  }
}
