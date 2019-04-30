package org.ananas.runner.model.steps.commons;

import org.apache.beam.sdk.values.Row;

import java.util.Map;

public interface RowConverter {

	Row convertMap(Map<String, Object> o);
}
