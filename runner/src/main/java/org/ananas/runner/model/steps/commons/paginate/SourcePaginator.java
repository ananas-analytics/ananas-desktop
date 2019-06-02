package org.ananas.runner.model.steps.commons.paginate;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.ananas.runner.kernel.common.VariableRender;
import org.ananas.runner.kernel.model.Dataframe;
import org.ananas.runner.kernel.model.StepType;
import org.ananas.runner.kernel.model.Variable;
import org.ananas.runner.kernel.paginate.Paginator;
import org.ananas.runner.model.core.StepConfig;
import org.ananas.runner.model.steps.api.APIPaginator;
import org.ananas.runner.model.steps.api.APIStepConfig;
import org.ananas.runner.model.steps.db.JdbcPaginator;
import org.ananas.runner.steprunner.jdbc.JdbcStepConfig;
import org.ananas.runner.model.steps.db.MongoDBPaginator;
import org.ananas.runner.model.steps.db.MongoStepConfig;
import org.ananas.runner.model.steps.files.*;
import org.ananas.runner.model.steps.files.csv.CSVPaginator;
import org.ananas.runner.steprunner.files.csv.CSVStepConfig;
import org.ananas.runner.steprunner.files.utils.StepFileConfigToUrl;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;

public class SourcePaginator implements Paginator {

  Map<String, Object> config;
  String id;
  StepType type;

  public SourcePaginator(String id, StepType type, Map<String, Object> conf) {
    this.id = id;
    this.config = conf;
    this.type = type;
  }

  public static SourcePaginator of(
      String id, String type, Map<String, Object> config, Map<String, Variable> variables) {
    Preconditions.checkNotNull(config, "config cannot be null");
    config = VariableRender.renderConfig(variables, config);
    StepType t = StepType.from(type);
    return new SourcePaginator(id, t, config);
  }

  @Override
  public MutablePair<Schema, Iterable<Row>> paginateRows(Integer page, Integer pageSize) {
    return createPaginator().paginateRows(page, pageSize);
  }

  @Override
  public Dataframe paginate(Integer page, Integer pageSize) {
    return createPaginator().paginate(page, pageSize);
  }

  public Paginator createPaginator() {
    switch ((String) this.config.get(StepConfig.SUBTYPE)) {
      case "mongo":
        MongoStepConfig mongoConfig = new MongoStepConfig(this.config);
        return new MongoDBPaginator(this.id, mongoConfig);
      case "jdbc":
        JdbcStepConfig jdbcConfig = new JdbcStepConfig(this.config);
        return new JdbcPaginator(this.id, jdbcConfig);
      case "file":
        String format = (String) this.config.get(StepConfig.FORMAT);
        Preconditions.checkNotNull(format, "format cannot be empty");
        switch (format) {
          case "csv":
            CSVStepConfig csvConfig = new CSVStepConfig(this.type, this.config);
            return new CSVPaginator(this.id, csvConfig);
          case "text":
            return new TextPaginator(
                this.id,
                StepFileConfigToUrl.url(this.type, this.config, FileLoader.SupportedFormat.TXT));
          case "json":
            return new JsonPaginator(
                this.id,
                StepFileConfigToUrl.url(this.type, this.config, FileLoader.SupportedFormat.JSON));
          case "api":
            return new APIPaginator(this.id, new APIStepConfig(this.config));
          case "excel":
            ExcelStepConfig excelConfig = new ExcelStepConfig(this.config);
            return new ExcelPaginator(this.id, excelConfig);
          default:
            throw new IllegalStateException(
                "Unsupported files format '" + this.config.get(StepConfig.FORMAT) + "'");
        }

      default:
        throw new IllegalStateException(
            "Unsupported source type '" + this.config.get(StepConfig.SUBTYPE) + "'");
    }
  }
}
