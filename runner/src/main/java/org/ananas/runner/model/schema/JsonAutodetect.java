package org.ananas.runner.model.schema;

import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.ananas.runner.model.datatype.TypeInferer;
import org.apache.beam.sdk.schemas.Schema;
import org.bson.Document;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonAutodetect {

	public static Schema autodetectJson(String json, boolean parseString) {
		Map<String, Schema.FieldType> types = new HashMap<>();
		inferTypes(types, json, FlattenMode.KEEP_ARRAYS, parseString);
		return buildSchema(types);
	}

	public static Schema autodetectJson(Iterator<String> jsons, boolean parseString, Integer limit) {
		Map<String, Schema.FieldType> types = new HashMap<>();
		int i = 0;
		while (jsons.hasNext()) {
			String json = jsons.next();
			inferTypes(types, json, FlattenMode.KEEP_ARRAYS, parseString);
			if (limit != null && i++ >= limit) {
				break;
			}
		}

		return buildSchema(types);
	}

	public static Schema autodetectBson(Iterator<Document> jsons,
										FlattenMode mode,
										boolean parseString,
										Integer limit) {
		Map<String, Schema.FieldType> types = new HashMap<>();
		int i = 0;
		while (jsons.hasNext()) {
			String json = jsons.next().toJson();
			inferTypes(types, json, mode, parseString);
			if (limit != null && i++ >= limit) {
				break;
			}
		}
		return buildSchema(types);
	}

	public static Schema buildSchema(Map<String, Schema.FieldType> types) {
		Schema.Builder builder = Schema.builder();
		for (Map.Entry<String, Schema.FieldType> type : types.entrySet()) {
			builder.addNullableField(type.getKey(), type.getValue());
		}
		return builder.build();
	}

	public static void inferTypes(Map<String, Schema.FieldType> types,
								  String jsonString,
								  FlattenMode mode,
								  boolean parseString) {
		try {
			Map<String, Object> o = new JsonFlattener(jsonString).withFlattenMode(mode).flattenAsMap();
			inferTypes(types, o, parseString);
		} catch (Exception e) {
		}
	}

	static void inferTypes(Map<String, Schema.FieldType> types, Map<String, Object> o, boolean parseString) {
		for (Map.Entry<String, Object> entry : o.entrySet()) {
			if (types.get(entry.getKey()) != null) {
				types.put(entry.getKey(), TypeInferer.mergeType(types.get(entry.getKey()),
						TypeInferer.inferType(entry.getValue(), parseString)));
			} else {
				types.put(entry.getKey(), TypeInferer.inferType(entry.getValue(), parseString));
			}
		}
	}
}
