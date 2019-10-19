package org.ananas.runner.core.schema;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.ananas.runner.core.common.JsonUtil;
import org.apache.beam.sdk.schemas.Schema.TypeName;
import org.junit.Assert;
import org.junit.Test;

public class SchemaV2Test {
  @Test
  public void deserializeFlatSchema() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/flat_schema.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema schema = JsonUtil.fromJson(json, Schema.class);
    Assert.assertEquals(4, schema.fields.size());

    Assert.assertEquals("id", schema.fields.get(0).name);
    Assert.assertEquals("VARCHAR", schema.fields.get(0).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(0).mode);

    Assert.assertEquals("first_name", schema.fields.get(1).name);
    Assert.assertEquals("VARCHAR", schema.fields.get(1).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(1).mode);

    Assert.assertEquals("last_name", schema.fields.get(2).name);
    Assert.assertEquals("VARCHAR", schema.fields.get(2).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(2).mode);

    Assert.assertEquals("dob", schema.fields.get(3).name);
    Assert.assertEquals("DATETIME", schema.fields.get(3).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(3).mode);
  }

  @Test
  public void deserializeNestedSchema() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/nested_schema_repeated.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema schema = JsonUtil.fromJson(json, Schema.class);
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
    Assert.assertEquals("DATETIME", schema.fields.get(3).type);
    Assert.assertEquals("NULLABLE", schema.fields.get(3).mode);

    Assert.assertEquals("addresses", schema.fields.get(4).name);
    Assert.assertEquals("RECORD", schema.fields.get(4).type);
    Assert.assertEquals("REPEATED", schema.fields.get(4).mode);

    List<SchemaField> nestedFields = schema.fields.get(4).fields;
    Assert.assertEquals(6, nestedFields.size());
    Assert.assertEquals("status", nestedFields.get(0).name);
    Assert.assertEquals("numberOfYears", nestedFields.get(5).name);
  }

  @Test
  public void beamSimpleSchemaConversion() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/flat_schema.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema ananasSchema = JsonUtil.fromJson(json, Schema.class);

    org.apache.beam.sdk.schemas.Schema schema = ananasSchema.toBeamSchema();

    Assert.assertEquals(4, schema.getFieldCount());
    Assert.assertEquals("id", schema.getFields().get(0).getName());
    // type must be converted to beam field type
    Assert.assertEquals("STRING", schema.getFields().get(0).getType().getTypeName().name());

    Assert.assertEquals("first_name", schema.getFields().get(1).getName());
    Assert.assertEquals("last_name", schema.getFields().get(2).getName());
    Assert.assertEquals("dob", schema.getFields().get(3).getName());
  }

  @Test
  public void beamSimpleRepeatedSchemaConversion() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/flat_schema_repeated.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema ananasSchema = JsonUtil.fromJson(json, Schema.class);

    org.apache.beam.sdk.schemas.Schema schema = ananasSchema.toBeamSchema();

    Assert.assertEquals(4, schema.getFieldCount());
    Assert.assertEquals("id", schema.getFields().get(0).getName());
    Assert.assertEquals("first_name", schema.getFields().get(1).getName());
    Assert.assertEquals("last_name", schema.getFields().get(2).getName());
    Assert.assertEquals("dob", schema.getFields().get(3).getName());

    Assert.assertEquals(false, schema.getField(3).getType().getTypeName().isCompositeType());
    Assert.assertEquals(true, schema.getField(3).getType().getTypeName().isCollectionType());
  }

  @Test
  public void beamNestedSchemaConversion() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/nested_schema.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema ananasSchema = JsonUtil.fromJson(json, Schema.class);

    org.apache.beam.sdk.schemas.Schema schema = ananasSchema.toBeamSchema();

    Assert.assertEquals(5, schema.getFieldCount());
    Assert.assertEquals("id", schema.getFields().get(0).getName());
    Assert.assertEquals("first_name", schema.getFields().get(1).getName());
    Assert.assertEquals("last_name", schema.getFields().get(2).getName());
    Assert.assertEquals("dob", schema.getFields().get(3).getName());
    Assert.assertEquals("addresses", schema.getFields().get(4).getName());

    org.apache.beam.sdk.schemas.Schema.Field field = schema.getFields().get(4);

    Assert.assertEquals(false, field.getType().getTypeName().isCollectionType());
    Assert.assertEquals(true, field.getType().getTypeName().isCompositeType());

    org.apache.beam.sdk.schemas.Schema rowSchema = field.getType().getRowSchema();
    Assert.assertEquals("status", rowSchema.getField(0).getName());
    Assert.assertEquals("address", rowSchema.getField(1).getName());
    Assert.assertEquals("city", rowSchema.getField(2).getName());
    Assert.assertEquals("state", rowSchema.getField(3).getName());
    Assert.assertEquals("zip", rowSchema.getField(4).getName());
    Assert.assertEquals("numberOfYears", rowSchema.getField(5).getName());
  }

  @Test
  public void beamNestedRepeatedSchemaConversion() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("schema/nested_schema_repeated.json");
    String json =
        new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);

    Schema ananasSchema = JsonUtil.fromJson(json, Schema.class);

    org.apache.beam.sdk.schemas.Schema schema = ananasSchema.toBeamSchema();

    Assert.assertEquals(5, schema.getFieldCount());
    Assert.assertEquals("id", schema.getFields().get(0).getName());
    Assert.assertEquals("first_name", schema.getFields().get(1).getName());
    Assert.assertEquals("last_name", schema.getFields().get(2).getName());
    Assert.assertEquals("dob", schema.getFields().get(3).getName());
    Assert.assertEquals("addresses", schema.getFields().get(4).getName());

    org.apache.beam.sdk.schemas.Schema.Field field = schema.getFields().get(4);

    Assert.assertEquals(true, field.getType().getTypeName().isCollectionType());
    Assert.assertEquals(false, field.getType().getTypeName().isCompositeType());

    org.apache.beam.sdk.schemas.Schema.FieldType rowFieldType =
        field.getType().getCollectionElementType();
    Assert.assertEquals(TypeName.ROW, rowFieldType.getTypeName());

    org.apache.beam.sdk.schemas.Schema rowSchema = rowFieldType.getRowSchema();
    Assert.assertEquals("status", rowSchema.getField(0).getName());
    Assert.assertEquals("address", rowSchema.getField(1).getName());
    Assert.assertEquals("city", rowSchema.getField(2).getName());
    Assert.assertEquals("state", rowSchema.getField(3).getName());
    Assert.assertEquals("zip", rowSchema.getField(4).getName());
    Assert.assertEquals("numberOfYears", rowSchema.getField(5).getName());
  }
}
