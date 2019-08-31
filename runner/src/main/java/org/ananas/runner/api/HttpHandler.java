package org.ananas.runner.api;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.errors.AnanasException;
import org.ananas.runner.core.job.BeamRunner;
import org.ananas.runner.core.job.Job;
import org.ananas.runner.core.job.JobRepositoryFactory;
import org.ananas.runner.core.job.Runner;
import org.ananas.runner.core.model.Dataframe;
import org.ananas.runner.core.paginate.PaginationBody;
import org.ananas.runner.core.paginate.Paginator;
import org.ananas.runner.core.paginate.PaginatorFactory;
import org.ananas.runner.steprunner.DefaultDataViewer;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;

class HttpHandler {

  // DATASET

  // TEST
  static Route paginateStep =
      (Request request, Response response) -> {
        String id = request.params(":id");
        String page = request.queryParams("page");
        String pageSize = request.queryParams("pagesize");

        String body = request.body();

        PaginationBody paginationBody =
            body == null || body.length() == 0
                ? new PaginationBody()
                : JsonUtil.fromJson(body, PaginationBody.class);

        Paginator paginator = PaginatorFactory.of(id, paginationBody);
        try {
          Dataframe dataframe =
              paginator.paginate(
                  page == null ? 0 : Integer.valueOf(page),
                  pageSize == null ? 1000 : Integer.valueOf(pageSize));
          return JsonUtil.toJson(ApiResponseBuilder.Of().OK(dataframe).build());
        } catch (Exception e) {
          return JsonUtil.toJson(ApiResponseBuilder.Of().KO(e).build());
        }
      };

  // TEST
  static Route testDag =
      (Request request, Response response) -> {
        String id = request.params(":projectid");
        String body = request.body();

        if (body == null || body.length() == 0) {
          return JsonUtil.toJson(
              ApiResponseBuilder.Of()
                  .KO(
                      new AnanasException(
                          org.ananas.runner.core.errors.ExceptionHandler.ErrorCode.GENERAL,
                          "missing body"))
                  .build());
        }

        return Services.testDag(body);
      };

  // PREDICT
  static Route runDag =
      (Request request, Response response) -> {
        String id = request.params(":projectid");
        String token = request.headers("Authorization");
        String body = request.body();

        if (body == null || body.length() == 0) {
          return JsonUtil.toJson(
              ApiResponseBuilder.Of()
                  .KO(
                      new AnanasException(
                          org.ananas.runner.core.errors.ExceptionHandler.ErrorCode.GENERAL,
                          "missing body"))
                  .build());
        }

        return Services.runDag(id, token, body);
      };

  static Route cancelPipeline =
      (Request request, Response response) -> {
        String id = request.params(":id");
        Runner runner = new BeamRunner();
        runner.cancel(id);
        return JsonUtil.toJson(ApiResponseBuilder.Of().OK(id + " cancelled").build());
      };

  static Route listJobs =
      (Request request, Response response) -> {
        Runner runner = new BeamRunner();
        Set<Job> jobs = runner.getJobs();
        return JsonUtil.toJson(ApiResponseBuilder.Of().OK(jobs).build());
      };

  static Route getJobsByGoal =
      (Request request, Response response) -> {
        String goalid = request.params(":goalid");
        String skip = request.queryParams("skip");
        String size = request.queryParams("size");

        List<Job> jobs =
            JobRepositoryFactory.getJobRepostory()
                .getJobsByGoal(
                    goalid,
                    skip == null ? 0 : Integer.valueOf(skip),
                    size == null ? 10 : Integer.valueOf(size));

        List<Job> output =
            jobs.stream().map(Job::JobStateResultFilter).collect(Collectors.toList());

        return JsonUtil.toJson(ApiResponseBuilder.Of().OK(output).build());
      };

  static Route pollJob =
      (Request request, Response response) -> {
        Runner runner = new BeamRunner();
        String jobid = request.params(":jobid");
        Job job = runner.getJob(jobid);
        if (job == null) {
          return JsonUtil.toJson(
              ApiResponseBuilder.Of().KO(new NoSuchElementException("job not found")).build());
        } else {
          HashMap<String, String> stateResponse = new HashMap<String, String>();
          stateResponse.put("state", job.getResult().getLeft().toString());
          if (job.getResult().getRight() != null) {
            stateResponse.put("message", job.getResult().getRight().getLocalizedMessage());
          }
          return JsonUtil.toJson(ApiResponseBuilder.Of().OK(stateResponse).build());
        }
      };

  static Route dataView =
      (Request request, Response response) -> {
        String jobid = request.params(":jobid");
        String stepid = request.params(":stepid");
        if (stepid == null) {
          return JsonUtil.toJson(
              ApiResponseBuilder.Of().KO(new NoSuchElementException("stepid not found")).build());
        }
        if (jobid == null) {
          return JsonUtil.toJson(
              ApiResponseBuilder.Of().KO(new NoSuchElementException("jobid not found")).build());
        }

        DefaultDataViewer.DataViewRepository repository =
            new DefaultDataViewer.DataViewRepository();
        return JsonUtil.toJson(
            ApiResponseBuilder.Of()
                .OK(repository.query(request.queryParams("sql"), jobid, stepid))
                .build());
      };

  static ExceptionHandler error =
      (Exception e, Request request, Response response) -> {
        response.status(200);
        response.body(JsonUtil.toJson(ApiResponseBuilder.Of().KO(e).build()));
        e.printStackTrace();
      };
}
