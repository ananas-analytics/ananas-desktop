package org.ananas.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.ananas.database.Job;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.SQLDataType;

public class DatabaseHelper {

  public static final String TABLE_JOB = "JOB";

  public static void initiate(Connection connection) throws SQLException {
    DSLContext context = getContext(connection);

    // create job table
    context
        .createTable(TABLE_JOB)
        // usually uuid length will be only 36
        .column("id", SQLDataType.VARCHAR.length(40))
        .column("state", SQLDataType.VARCHAR.length(20))
        .column("createAt", SQLDataType.TIMESTAMP)
        .column("updateAt", SQLDataType.TIMESTAMP)
        .constraints(DSL.constraint("PK_AA_JOB").primaryKey("id"))
        .execute();

    // TODO: init other tables here (quartz)

  }

  public static List<Job> getJob(Connection connection) {
    DSLContext context = getContext(connection);
    List<Job> jobs = context.select().from(TABLE_JOB).fetch().into(Job.class);
    return jobs;
  }

  private static DSLContext getContext(Connection connection) {
    Configuration config = new DefaultConfiguration();
    config.set(connection);
    config.set(new Settings().withRenderSchema(true));
    config.set(SQLDialect.DERBY);
    DSLContext context = DSL.using(config);
    return context;
  }

  private static void executeSQLFromResource(Connection connection, String name)
      throws SQLException, URISyntaxException, IOException {
    String sql =
        new String(
            Files.readAllBytes(
                Paths.get(
                    Objects.requireNonNull(
                            DefaultScheduler.class.getClassLoader().getResource(name))
                        .toURI())));
    Statement statement = connection.createStatement();
    statement.execute(sql);
  }
}
