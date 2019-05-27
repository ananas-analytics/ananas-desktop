package org.ananas.runner.kernel.schema;

import org.ananas.runner.kernel.common.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class SchemaV2Test {
  @Test
  public void deserializeFlatSchema() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/flat_schema.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    SchemaV2 schema = JsonUtil.fromJson(json, SchemaV2.class);
    Assert.assertEquals(4, schema.fields.size());

    Assert.assertEquals("id", schema.fields.get(0).name);
    Assert.assertEquals("STRING", schema.fields.get(0).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(0).mode);

    Assert.assertEquals("first_name", schema.fields.get(1).name);
    Assert.assertEquals("STRING", schema.fields.get(1).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(1).mode);

    Assert.assertEquals("last_name", schema.fields.get(2).name);
    Assert.assertEquals("STRING", schema.fields.get(2).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(2).mode);

    Assert.assertEquals("dob", schema.fields.get(3).name);
    Assert.assertEquals("DATE", schema.fields.get(3).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(3).mode);
  }

  @Test
  public void deserializeNestedSchema() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/example_schema_1.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    SchemaV2 schema = JsonUtil.fromJson(json, SchemaV2.class);
    Assert.assertEquals(5, schema.fields.size());

    Assert.assertEquals("id", schema.fields.get(0).name);
    Assert.assertEquals("STRING", schema.fields.get(0).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(0).mode);

    Assert.assertEquals("first_name", schema.fields.get(1).name);
    Assert.assertEquals("STRING", schema.fields.get(1).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(1).mode);

    Assert.assertEquals("last_name", schema.fields.get(2).name);
    Assert.assertEquals("STRING", schema.fields.get(2).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(2).mode);

    Assert.assertEquals("dob", schema.fields.get(3).name);
    Assert.assertEquals("DATE", schema.fields.get(3).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(3).mode);

    Assert.assertEquals("addresses", schema.fields.get(4).name);
    Assert.assertEquals("RECORD", schema.fields.get(4).type);
    Assert.assertEquals("REPEATED", schema.fields.get(4).mode);

    List<SchemaFieldV2> nestedFields = schema.fields.get(4).fields;
    Assert.assertEquals(6, nestedFields.size());
    Assert.assertEquals("status", nestedFields.get(0).name);
    Assert.assertEquals("numberOfYears", nestedFields.get(5).name);


  }
}
