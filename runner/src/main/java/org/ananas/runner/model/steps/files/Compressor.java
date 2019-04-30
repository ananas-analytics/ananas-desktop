package org.ananas.runner.model.steps.files;

import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;

public class Compressor {

	public static TextIO.Read compress(TextIO.Read r, Compression compression, boolean compress) {
		if (compress) {
			return r.withCompression(compression);
		}
		return r;
	}

	public static Compression compression(String name) {
		return Compression.AUTO;
	}

}
