package org.ananas.runner.core.extension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.ananas.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class LocalExtensionRepositoryTest {
  @Test
  public void testExtensionRepository() throws IOException {
    // first install
    File temp = Files.createTempDirectory("ananas-test-").toFile();
    LocalExtensionRepository repo = new LocalExtensionRepository(temp.getAbsolutePath());
    repo.load();

    URL zip = TestHelper.getResource("extensions/example-ext.zip");

    repo.install(zip);

    System.out.println(temp.getAbsoluteFile());

    File extFolder = new File(new File(temp, "ananas-ext-example"), "0.1.0");

    File descriptor = new File(extFolder, "extension.yml");
    Assert.assertTrue(descriptor.exists());

    File metadata = new File(extFolder, "metadata.yml");
    Assert.assertTrue(metadata.exists());

    File lib = new File(extFolder, "lib");
    Assert.assertTrue(lib.exists());

    File jar = new File(lib, "extension-example-0.10.0-SNAPSHOT.jar");
    Assert.assertTrue(jar.exists());

    Assert.assertTrue(repo.hasExtension("ananas-ext-example", "0.1.0"));

    ExtensionManifest manifest = repo.getExtension("ananas-ext-example", "0.1.0");
    Assert.assertEquals(descriptor.getAbsolutePath(), manifest.getDescriptor().getPath());
    Assert.assertEquals(metadata.getAbsolutePath(), manifest.getMetadata().getPath());
    Assert.assertEquals(jar.getAbsolutePath(), manifest.getLibs().get(0).getPath());

    // now create a new repo from the temp folder
    LocalExtensionRepository anotherRepo = new LocalExtensionRepository(temp.getAbsolutePath());
    anotherRepo.load();
    ExtensionManifest manifest1 = anotherRepo.getExtension("ananas-ext-example", "0.1.0");
    Assert.assertEquals(descriptor.getAbsolutePath(), manifest1.getDescriptor().getPath());
    Assert.assertEquals(metadata.getAbsolutePath(), manifest1.getMetadata().getPath());
    Assert.assertEquals(jar.getAbsolutePath(), manifest1.getLibs().get(0).getPath());

    // now remove the extension
    anotherRepo.delete("ananas-ext-example", "0.1.0");
    ExtensionManifest manifest2 = anotherRepo.getExtension("ananas-ext-example", "0.1.0");
    Assert.assertNull(manifest2);

    temp.delete();
  }
}
