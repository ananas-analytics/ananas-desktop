package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.ananas.TestHelper;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionManagerTest {

  @Test
  public void testLoadStepExtensions() {
    ExtensionManager extManager = ExtensionManager.getInstance();

    URL extensionRepo = TestHelper.getResource("extensions/test_step_repo");
    extManager.load(extensionRepo.getPath());

    StepMetadata ext1 = extManager.getStepMetadata("ext1");
    Assert.assertEquals("Source", ext1.type);
    Assert.assertEquals(1, ext1.classpath.size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/lib/extension1.jar",
        ext1.classpath.get(0).getPath());

    StepMetadata ext2 = extManager.getStepMetadata("ext2");
    Assert.assertEquals("Transform", ext2.type);
    Assert.assertEquals(1, ext2.classpath.size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/lib/extension1.jar",
        ext2.classpath.get(0).getPath());

    StepMetadata ext3 = extManager.getStepMetadata("ext3");
    Assert.assertEquals("Destination", ext3.type);
    Assert.assertEquals(2, ext3.classpath.size());
    List<String> paths =
        ext3.classpath.stream().map(url -> url.getPath()).collect(Collectors.toList());

    Assert.assertTrue(paths.contains(extensionRepo.getPath() + "/extension2/lib/extension2.jar"));
    Assert.assertTrue(paths.contains(extensionRepo.getPath() + "/extension2/lib/otherfile.txt"));
  }
}
