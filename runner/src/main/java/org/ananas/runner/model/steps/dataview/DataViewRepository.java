package org.ananas.runner.model.steps.dataview;

import org.ananas.runner.model.core.Dataframe;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.commons.StepType;
import org.ananas.runner.model.steps.commons.paginate.Paginator;
import org.ananas.runner.model.steps.commons.paginate.SourcePaginator;
import org.ananas.runner.model.steps.db.jdbc.JDBCDriver;
import org.ananas.runner.model.steps.files.utils.HomeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DataViewRepository {

	private static final Logger LOG = LoggerFactory.getLogger(DataViewRepository.class);

	private JDBCDriver driver;

	private static String URL = "derby:%s/dataview;create=true";

	public DataViewRepository() {
		this.driver = JDBCDriver.DERBY;
	}

	static String URL(boolean withPrefix) {
		return String.format((withPrefix ? "jdbc:" : "") + URL, HomeManager.getHome());
	}

	public Dataframe query(String sql, String stepId) {

		String tName = "table_" + stepId;
		String s = sql.replaceFirst("PCOLLECTION", tName);

		Map<String, Object> config = new HashMap<>();

		config.put(StepConfig.SUBTYPE, "jdbc");
		config.put(StepConfig.JDBC_TYPE, JDBCDriver.DERBY.driverName);
		config.put(StepConfig.JDBC_SQL, s);
		config.put(StepConfig.JDBC_URL, URL(false));
		config.put(StepConfig.JDBC_TABLENAME, tName);
		config.put(StepConfig.JDBC_OVERWRITE, true);
		config.put(StepConfig.JDBC_USER, "");
		config.put(StepConfig.JDBC_PASSWORD, "");

		Paginator paginator =
				SourcePaginator.of(tName, StepType.Connector.name(), config, new HashMap<>());
		Dataframe dataframe = paginator.paginate(0, Integer.MAX_VALUE);
		return dataframe;
	}

}
