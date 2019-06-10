package org.ananas.scheduler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import lombok.Data;
import org.ananas.runner.api.ApiResponse;
import org.ananas.runner.kernel.common.JsonUtil;
import org.ananas.runner.kernel.model.DagRequest;
import org.ananas.runner.kernel.model.Variable;
import org.ananas.runner.misc.HttpClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunnerJob implements Job {
  private static final Logger LOG = LoggerFactory.getLogger(RunnerJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {

    String optionsStr = context.getTrigger().getJobDataMap().getString("options");
    try {
      ScheduleOptions options = JsonUtil.fromJson(optionsStr, ScheduleOptions.class);

      LOG.info("Running schedule job {}", options.id);

      DagRequest dag = options.dag;

      // inject runtime parameters here
      this.injectRuntimeVariables(dag);

      // call runner API to submit the job
      HashMap<String, String> headers = new HashMap<>();
      headers.put("Content-Type", "application/json");
      ApiResponse<JobState> response =
          HttpClient.POST(
              "http://localhost:3003/v1/schedule_job/dag/run",
              headers,
              dag,
              conn -> JsonUtil.fromJsonToApiResponse(conn.getInputStream(), JobState.class));

      String jobState = "SUBMITTED";

      if (response.code != 200) {
        LOG.warn("FAILED: submit job {}", options.id);
        throw new JobExecutionException("Failed to submit job");
      }

      jobState = response.data.state;
      // poll job status
      while (!"DONE".equals(jobState) && !"FAIL".equals(jobState)) {
        Thread.sleep(5000);
        ApiResponse<JobState> pollingResp =
            HttpClient.POST(
                "http://localhost:3003/v1/schedule_job/dag/run",
                headers,
                dag,
                conn -> {
                  return JsonUtil.fromJsonToApiResponse(conn.getInputStream(), JobState.class);
                });
        if (pollingResp.code == 200) {
          jobState = pollingResp.data.state;
        }
        LOG.warn("Failed to poll job state for job {}", options.id);
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new JobExecutionException(e);
    }
  }

  private void injectRuntimeVariables(DagRequest dagRequest) {
    Variable executionDate = new Variable();
    executionDate.name = "EXECUTE_DATE";
    executionDate.type = "date";

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    executionDate.value = sdf.format(new Date(System.currentTimeMillis()));

    dagRequest.params.put(executionDate.name, executionDate);
  }

  @Data
  private class JobState {
    public String state;
  }
}
