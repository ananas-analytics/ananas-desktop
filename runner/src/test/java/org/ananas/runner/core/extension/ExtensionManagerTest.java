package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.ananas.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionManagerTest {

  @Test
  public void testLoadExtensions() {
    ExtensionManager extManager = ExtensionManager.getInstance();

    URL extensionRepo = TestHelper.getResource("extensions/simple_case");
    extManager.loadExtensions(extensionRepo.getPath());

    StepMetadata ext1 = extManager.getStepMetadata("ext1");
    Assert.assertEquals("Source", ext1.getType());
    Assert.assertEquals(1, ext1.getClasspath().size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/libs/extension1.jar",
        ext1.getClasspath().get(0).getPath());

    StepMetadata ext2 = extManager.getStepMetadata("ext2");
    Assert.assertEquals("Transform", ext2.getType());
    Assert.assertEquals(1, ext2.getClasspath().size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/libs/extension1.jar",
        ext2.getClasspath().get(0).getPath());

    StepMetadata ext3 = extManager.getStepMetadata("ext3");
    Assert.assertEquals("Destination", ext3.getType());
    Assert.assertEquals(2, ext3.getClasspath().size());
    List<String> paths =
        ext3.getClasspath().stream().map(url -> url.getPath()).collect(Collectors.toList());
    Assert.assertTrue(paths.contains(extensionRepo.getPath() + "/extension2/libs/extension2.jar"));
    Assert.assertTrue(paths.contains(extensionRepo.getPath() + "/extension2/libs/otherfile.txt"));
  }
}
