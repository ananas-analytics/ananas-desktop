package org.ananas.runner.steprunner.sql.udf;

import org.apache.beam.repackaged.beam_sdks_java_extensions_sql.org.apache.calcite.linq4j.function.Parameter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ToDateFn implements org.apache.beam.sdk.extensions.sql.BeamSqlUdf{

	SimpleDateFormat format;
	TimeZone timezone;

	public ToDateFn() {
		timezone = null;
		format = null;
	}

	public Date eval(
			@Parameter(name = "s") String s,
			@Parameter(name = "n", optional = false) String n,
			@Parameter(name = "t", optional = true) String zoneId) {
		try {
			if (format == null) {
				format = new SimpleDateFormat(n);
			}
			if (timezone == null && zoneId != null) {
				timezone = TimeZone.getTimeZone(zoneId);
				format.setTimeZone(timezone);
			}
			return format.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}
}