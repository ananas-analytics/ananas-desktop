package org.ananas.runner.core.extension;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.ananas.TestHelper;
import org.ananas.runner.core.model.Extension;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionManagerTest {

  @Test
  public void testLoadStepExtensions() {
    URL extensionRepo = TestHelper.getResource("extensions/test_step_repo");
    LocalExtensionRepository repo =
        LocalExtensionRepository.setDefaultRepository(extensionRepo.getPath());
    repo.load();

    DefaultExtensionManager extManager = new DefaultExtensionManager(repo);
    Map<String, Extension> exts = new HashMap<>();
    exts.put("extension1", new Extension("0.1.0", null, null));
    exts.put("extension2", new Extension("0.1.0", null, null));
    extManager.resolve(exts);

    StepMetadata ext1 = extManager.getStepMetadata("ext1");
    Assert.assertEquals("Source", ext1.type);
    Assert.assertEquals(1, ext1.classpath.size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/0.1.0/lib/extension1.jar",
        ext1.classpath.get(0).getPath());

    StepMetadata ext2 = extManager.getStepMetadata("ext2");
    Assert.assertEquals("Transform", ext2.type);
    Assert.assertEquals(1, ext2.classpath.size());
    Assert.assertEquals(
        extensionRepo.getPath() + "/extension1/0.1.0/lib/extension1.jar",
        ext2.classpath.get(0).getPath());

    StepMetadata ext3 = extManager.getStepMetadata("ext3");
    Assert.assertEquals("Destination", ext3.type);
    Assert.assertEquals(2, ext3.classpath.size());
    List<String> paths =
        ext3.classpath.stream().map(url -> url.getPath()).collect(Collectors.toList());

    Assert.assertTrue(
        paths.contains(extensionRepo.getPath() + "/extension2/0.1.0/lib/extension2.jar"));
    Assert.assertTrue(
        paths.contains(extensionRepo.getPath() + "/extension2/0.1.0/lib/otherfile.txt"));
  }
}
