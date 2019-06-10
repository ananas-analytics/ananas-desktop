package org.ananas.scheduler;

import static org.quartz.DateBuilder.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.steprunner.files.utils.HomeManager;
import org.ananas.runner.steprunner.jdbc.JDBCDriver;
import org.ananas.runner.steprunner.jdbc.JDBCStatement;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScheduler implements Scheduler {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultScheduler.class);

  private static final String DB_NAME = "quartz";

  private static DefaultScheduler INSTANCE = null;

  private org.quartz.Scheduler quartsScheduler;

  private DefaultScheduler() throws SchedulerException {
    Properties prop = new Properties();
    prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    prop.put("org.quartz.threadPool.threadCount", "2");

    prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
    prop.put(
        "org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
    prop.put("org.quartz.jobStore.dataSource", "quartzDataSource");
    prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
    prop.put("org.quartz.jobStore.isClustered", "false");

    // Database properties
    prop.put(
        "org.quartz.dataSource.quartzDataSource.driver", "org.apache.derby.jdbc.EmbeddedDriver");
    prop.put(
        "org.quartz.dataSource.quartzDataSource.URL",
        String.format("jdbc:derby:%s/%s;create=true", HomeManager.getHome(), DB_NAME));
    prop.put("org.quartz.dataSource.quartzDataSource.maxConnections", "2");

    initiateDB();

    this.quartsScheduler = new StdSchedulerFactory(prop).getScheduler();

    this.quartsScheduler.getListenerManager().addJobListener(new JobListener() {
      @Override
      public String getName() {
        return "example job listener";
      }

      @Override
      public void jobToBeExecuted(JobExecutionContext context) {
        String id = UUID.randomUUID().toString();
        context.getJobDetail().getJobDataMap().put("id", id);
        System.out.println("job to be executed");
        System.out.println(id);
      }

      @Override
      public void jobExecutionVetoed(JobExecutionContext context) {
        System.out.println("job execution vetoed");
      }

      @Override
      public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        System.out.println("Job executed");
        String id = context.getJobDetail().getJobDataMap().getString("id");
        System.out.println(id);
        System.out.println(jobException.getLocalizedMessage());
      }
    });
  }

  public static DefaultScheduler of() throws SchedulerException {
    if (INSTANCE == null) {
      INSTANCE = new DefaultScheduler();
    }
    return INSTANCE;
  }

  private void initiateDB() {
    // check if table exist
    JDBCStatement.Execute(
        JDBCDriver.DERBY,
        String.format("jdbc:derby:%s/%s;create=true", HomeManager.getHome(), DB_NAME),
        null,
        null,
        (conn, statement) -> {
          ResultSet rs =
              conn.getMetaData().getTables(null, "APP", "qrtz_scheduler_state".toUpperCase(), null);
          if (!rs.next()) { // table not exist
            String[] scripts =
                new String[] {
                  "quartz/job_details.sql",
                  "quartz/triggers.sql",
                  "quartz/simple_triggers.sql",
                  "quartz/cron_triggers.sql",
                  "quartz/simprop_triggers.sql",
                  "quartz/blob_triggers.sql",
                  "quartz/calendars.sql",
                  "quartz/paused_trigger_grps.sql",
                  "quartz/fired_triggers.sql",
                  "quartz/scheduler_state.sql",
                  "quartz/locks.sql",
                };

            Arrays.stream(scripts)
                .forEach(
                    script -> {
                      try {
                        DefaultScheduler.executeSQLFromResource(conn, script);
                      } catch (SQLException | URISyntaxException | IOException e) {
                        e.printStackTrace();
                      }
                    });
          } else {
            LOG.info("quartz table detected!");
          }
          return null;
        });
  }

  @Override
  public void start() throws SchedulerException {
    this.quartsScheduler.start();
  }

  @Override
  public void stop() throws SchedulerException {
    this.quartsScheduler.shutdown();
  }

  @Override
  public void schedule(ScheduleOptions options) {
    TriggerOptions triggerOptions = options.trigger;
    ScheduleBuilder builder = null;
    Date startDate = dateOf(triggerOptions.startHour, triggerOptions.startMinute, 0,
      triggerOptions.startDay, triggerOptions.startMonth, triggerOptions.startYear);

    switch (options.trigger.type) {
      case TriggerOptions.SECONDS:
        builder = simpleSchedule().withIntervalInSeconds(options.trigger.interval).repeatForever();
        break;
      case TriggerOptions.HOURLY:
        builder = simpleSchedule().withIntervalInHours(1).repeatForever();
        break;
      case TriggerOptions.WEEKLY:
        builder = CronScheduleBuilder.weeklyOnDayAndHourAndMinute(options.trigger.dayOfWeek, options.trigger.hour, options.trigger.minute);
        break;
      case TriggerOptions.MONTHLY:
        builder = CronScheduleBuilder.monthlyOnDayAndHourAndMinute(options.trigger.dayOfMonth, options.trigger.hour, options.trigger.minute);
        break;
      case TriggerOptions.DAILY:
      default:
        builder = CronScheduleBuilder.dailyAtHourAndMinute(options.trigger.hour, options.trigger.minute);
    }

    Trigger trigger =
        newTrigger()
            .withIdentity(options.id, TriggerKey.DEFAULT_GROUP)
            .usingJobData("options", JsonUtil.toJson(options))
            .startAt(startDate)
            //.startNow()
            .withSchedule((ScheduleBuilder<? extends Trigger>) builder)
            .build();

    // define the job and tie it to our RunnerJob class
    JobDetail job =
        newJob(RunnerJob.class)
            .withIdentity(options.id, JobKey.DEFAULT_GROUP) // name "myJob", group "group1"
            .build();

    try {
      this.quartsScheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void unschedule(String id) {
    try {
      this.quartsScheduler.unscheduleJob(triggerKey(id));
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ScheduleOptions getTriggerById(String id) throws SchedulerException, IOException {
    Trigger trigger = this.quartsScheduler.getTrigger(triggerKey(id));

    System.out.println(trigger.getStartTime());
    System.out.println(trigger.getPreviousFireTime());
    System.out.println(trigger.getNextFireTime());
    System.out.println(new Date());

    JobDataMap data = trigger.getJobDataMap();
    return JsonUtil.fromJson(data.getString("options"), ScheduleOptions.class);
  }

  public List<ScheduleOptions> getAllTriggers() throws SchedulerException {
    return this.quartsScheduler.getTriggerGroupNames().stream()
        .map(
            name -> {
              try {
                return this.quartsScheduler.getTriggerKeys(GroupMatcher.groupEquals(name));
              } catch (SchedulerException e) {
                e.printStackTrace();
              }
              return new HashSet<TriggerKey>();
            })
        .flatMap(Collection::stream)
        .map(
            triggerKey -> {
              try {
                Trigger trigger = this.quartsScheduler.getTrigger(triggerKey);
                JobDataMap data = trigger.getJobDataMap();

                return JsonUtil.fromJson(data.getString("options"), ScheduleOptions.class);
              } catch (SchedulerException | IOException e) {
                e.printStackTrace();
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public void getJobsByTriggerId(String id) throws SchedulerException, IOException {
    this.quartsScheduler.getJobKeys(GroupMatcher.jobGroupEquals(JobKey.DEFAULT_GROUP))
      .forEach(jobKey -> {
        System.out.println(jobKey.getName());
      });
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
