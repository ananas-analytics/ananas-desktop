package org.ananas.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.ananas.cli.commands.extension.ExtensionSource;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionSourceTest {
  @Test
  public void testExtensionSourceRelativePathParser()
      throws MalformedURLException, URISyntaxException {
    String currentDirectory = System.getProperty("user.dir");

    // scenario 1
    String path = "./score.csv";
    ExtensionSource source = ExtensionSource.parse(path);

    File current = new File(currentDirectory, path);
    Path root = Paths.get(current.toURI());

    Assert.assertEquals(root.normalize().toUri().toURL().toString(), source.url.toString());
    Assert.assertEquals("latest", source.version);
    Assert.assertFalse(source.resolved);

    // scenario 2
    path = "../score.csv";
    source = ExtensionSource.parse(path);

    current = new File(currentDirectory, path);
    root = Paths.get(current.toURI());

    Assert.assertEquals(root.normalize().toUri().toURL().toString(), source.url.toString());
    Assert.assertEquals("latest", source.version);
    Assert.assertFalse(source.resolved);
  }

  @Test
  public void testExtensionSourceAbsolutePathParser()
      throws MalformedURLException, URISyntaxException {
    String path = "/tmp/test/score.csv";
    ExtensionSource source = ExtensionSource.parse(path);

    Assert.assertEquals(
        Paths.get("/tmp/test/score.csv").normalize().toUri().toURL().toString(),
        source.url.toString());
    Assert.assertEquals("latest", source.version);
    Assert.assertFalse(source.resolved);
  }

  @Test
  public void testExtensionSourceHttpParser() throws MalformedURLException, URISyntaxException {
    String path = "http://tmp/test/score.csv@0.2.0";
    ExtensionSource source = ExtensionSource.parse(path);

    Assert.assertEquals("http://tmp/test/score.csv", source.url.toString());
    Assert.assertEquals("0.2.0", source.version);
    Assert.assertFalse(source.resolved);
  }

  @Test
  public void testResolvedExtensionSourceParser() throws MalformedURLException, URISyntaxException {
    String path = "http://tmp/test/score.zip";
    ExtensionSource source = ExtensionSource.parse(path);

    Assert.assertEquals("http://tmp/test/score.zip", source.url.toString());
    Assert.assertEquals("latest", source.version);
    Assert.assertTrue(source.resolved);
  }
}
