package org.ananas.runner.model.schema;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Schema {

	private List<SchemaField> fields;

	public static Schema Of(org.apache.beam.sdk.schemas.Schema schema) {
		Schema s = new Schema();
		s.fields = new ArrayList<>();
		if (schema == null) {
			return s;
		}
		int i = 1;
		for (org.apache.beam.sdk.schemas.Schema.Field f : schema.getFields()) {
			s.fields.add(SchemaField.Of(i, f.getName(), f.getType()));
			i++;
		}
		return s;
	}
}
