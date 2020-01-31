package org.ananas.runner.core.job;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.misc.HttpClient;

public class RemoteJobRepository implements JobRepository {

  private static RemoteJobRepository singleton;

  public static final String URL = "http://127.0.0.1:3004%s";

  private RemoteJobRepository() {}

  public static RemoteJobRepository Of() {
    if (singleton == null) {
      singleton = new RemoteJobRepository();
    }
    return singleton;
  }

  @Override
  public Job upsertJob(Job job) {
    try {
      return HttpClient.PUT(
          String.format(URL, "/v1/job"),
          new HashMap<>(),
          job,
          conn -> {
            return JsonUtil.fromJson(conn.getInputStream(), Job.class);
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Job getJob(String id) {
    try {
      return HttpClient.GET(
          String.format(URL, "/v1/job?id=" + id),
          new HashMap<>(),
          conn -> {
            return JsonUtil.fromJson(conn.getInputStream(), Job.class);
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Set<Job> getJobs(int offset, int n) {
    try {
      return new HashSet<>(
          Arrays.asList(
              HttpClient.GET(
                      String.format(URL, "/v1/jobs"),
                      ImmutableMap.of(),
                      conn -> {
                        return JsonUtil.fromJsonToApiResponse(conn.getInputStream(), Job[].class);
                      })
                  .data));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Job> getJobsByScheduleId(String triggerId, int offset, int n) {
    try {
      return Arrays.asList(
          HttpClient.GET(
                  String.format(URL, String.format("/v1/schedule/%s/jobs", triggerId)),
                  ImmutableMap.of(),
                  conn -> {
                    return JsonUtil.fromJsonToApiResponse(conn.getInputStream(), Job[].class);
                  })
              .data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Job> getJobsByGoal(String goalId, int offset, int n) {
    try {
      return Arrays.asList(
          HttpClient.GET(
                  String.format(URL, String.format("/v1/goal/%s/jobs", goalId)),
                  ImmutableMap.of(),
                  conn -> {
                    return JsonUtil.fromJsonToApiResponse(conn.getInputStream(), Job[].class);
                  })
              .data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteJob(String jobId) {
    try {
      HttpClient.PUT(
          String.format(URL, String.format("/v1/%s/jobs", jobId)),
          ImmutableMap.of("id", jobId),
          "",
          conn -> {
            return null;
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
