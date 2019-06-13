package org.ananas.database;

import static org.ananas.runner.kernel.common.Database.TABLE_GOAL;
import static org.ananas.runner.kernel.common.Database.TABLE_JOB;
import static org.jooq.impl.DSL.condition;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.ananas.runner.kernel.common.Database;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.apache.commons.io.FileUtils;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDatabaseHelper {

  private static Database db;
  private static String tmpDir;

  @BeforeClass
  public static void init() throws IOException {
    tmpDir = Files.createTempDirectory("ananas_test").toString();

    db =
        Database.of(
            JDBCDriver.DERBY,
            String.format("jdbc:derby:%s/%s;create=true", tmpDir, "test"),
            null,
            null,
            SQLDialect.DERBY);
  }

  @After
  public void tearDown() throws IOException {
    db.clean();

    FileUtils.deleteDirectory(new File(tmpDir));
  }

  @Test
  public void testInitiateJob() throws IOException {
    db.initiate();

    String jobId = UUID.randomUUID().toString();
    System.out.println(jobId);

    db.execute(
        context -> {
          return context
              .insertInto(DSL.table(TABLE_JOB))
              .set(DSL.field("ID"), jobId)
              .set(DSL.field("TRIGGER_ID"), UUID.randomUUID().toString())
              .set(DSL.field("TRIGGER_TYPE"), "HOURLY")
              .set(DSL.field("STATE"), DSL.val("SUBMITTED"))
              .set(DSL.field("MESSAGE"), "message")
              .set(DSL.field("DAG"), DSL.val("{\"nkey\": 1}".getBytes()))
              .set(DSL.field("TRIGGER"), DSL.val("message".getBytes()))
              .set(DSL.field("CREATE_AT"), DSL.currentTimestamp())
              .set(DSL.field("UPDATE_AT"), DSL.currentTimestamp())
              .execute();
        });

    db.execute(context -> {
      return context.update(DSL.table(TABLE_JOB))
        .set(DSL.field("STATE"), "DONE")
        .where(condition("ID = ?", jobId))
        .execute();
    });

    List<Job> jobs =
        db.execute(
            context -> {
              return context.selectFrom(TABLE_JOB).fetch().into(Job.class);
            });

    String goalId = UUID.randomUUID().toString();
    db.execute(context -> {
      return context.insertInto(DSL.table(TABLE_GOAL))
        .set(DSL.field("ID"), goalId)
        .set(DSL.field("JOB_ID"), jobId)
        .execute();
    });

    List<Result> j = db.execute(context-> {
      return context.select(DSL.field("DAG")).select(
        DSL.field("JOB.ID").as("ID"),
        DSL.field("STATE"),
        DSL.field("TRIGGER_TYPE"),
        DSL.field("GOAL.ID").as("GOAL_ID")
      )
        .from(TABLE_JOB)
        .join(TABLE_GOAL)
        .on(String.format("%s.ID = %s.JOB_ID", TABLE_JOB, TABLE_GOAL))
        .where(condition(TABLE_JOB + ".ID = ?", jobId))
        .orderBy(DSL.field("UPDATE_AT"))
        .fetch().into(Result.class);
    });

    assertEquals(1, j.size());
    assertEquals("{\"nkey\": 1}", new String(j.get(0).dag));
    assertEquals(goalId, j.get(0).goalId);
    assertEquals(jobId, j.get(0).id);
    assertEquals("DONE", j.get(0).state);
    assertEquals("HOURLY", j.get(0).triggerType);
  }

  private static class Result {
    public String id;
    public byte[] dag;
    public String state;
    public String triggerType;
    public String goalId;
  }
}
