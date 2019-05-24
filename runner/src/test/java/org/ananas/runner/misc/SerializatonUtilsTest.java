package org.ananas.runner.misc;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import smile.clustering.KMeans;

public class SerializatonUtilsTest {

  @Test
  public void testSymetry() {
    String fileName = UUID.randomUUID().toString();
    KMeans kmeans = KMeans.lloyd(new double[][] {{11}, {11}}, 20);
    SerializationUtils.serialize(kmeans, fileName, true);
    KMeans deserialized = (KMeans) SerializationUtils.deserialize(fileName, true);
    Assert.assertEquals(20, deserialized.getNumClusters());
  }
}
