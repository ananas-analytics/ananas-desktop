package org.ananas.runner.model.steps.commons;

import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.Row;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * DataReader working on DirectRunner context only. DO NOT USE IT IN PRODUCTION WHEN IT IS DISTRIBUTED ACCROSS MORE THAN ONE INSTANCE
 */
public class DirectRunnerDataReader implements Serializable, DataReader {
	private static final int MAX_BUFFER_SIZE = 1000;
	private static final long serialVersionUID = 5225989128858377626L;
	private static Map<String, List<List<Object>>> buffer;
	private String id;

	private DirectRunnerDataReader(String id) {
		this.id = id;
		this.buffer = new ConcurrentHashMap<>();
	}

	public static DataReader of(String id) {
		return new DirectRunnerDataReader(id);
	}

	@Override
	public List<List<Object>> getData() {
		return buffer.get(this.id) != null ? buffer.get(this.id) : new ArrayList<>();
	}

	@Override
	public MapElements<Row, Void> mapElements() {
		return MapElements.<Row, Void>via(new SimpleFunction<Row, Void>() {
			private static final long serialVersionUID = 9133954279124948160L;

			@Override
			public @Nullable
			Void apply(Row input) {
				if (buffer.get(DirectRunnerDataReader.this.id) == null) {
					buffer.putIfAbsent(DirectRunnerDataReader.this.id, new ArrayList<>());
				}
				if (buffer.get(DirectRunnerDataReader.this.id).size() < MAX_BUFFER_SIZE) {
					buffer.get(DirectRunnerDataReader.this.id).add(input.getValues());
				}
				return null;
			}
		});
	}

}
