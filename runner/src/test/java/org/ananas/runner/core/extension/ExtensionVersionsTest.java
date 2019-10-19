package org.ananas.runner.core.extension;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ExtensionVersionsTest {
  @Test
  public void testSortVersions() {
    ExtensionDescriptor v1 = new ExtensionDescriptor();
    v1.version = "0.1.0";

    ExtensionDescriptor v2 = new ExtensionDescriptor();
    v2.version = "0.2.0";

    List<ExtensionDescriptor> list = new ArrayList<>();
    list.add(v1);
    list.add(v2);

    ExtensionVersions.sortVersions(list);

    Assert.assertEquals("0.2.0", list.get(0).version);
    Assert.assertEquals("0.1.0", list.get(1).version);
  }
}
