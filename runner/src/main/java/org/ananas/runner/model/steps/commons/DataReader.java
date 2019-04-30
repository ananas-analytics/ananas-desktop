package org.ananas.runner.model.steps.commons;

import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.Row;

import java.util.List;

public interface DataReader {

	List<List<Object>> getData();

	MapElements<Row, Void> mapElements();

}
