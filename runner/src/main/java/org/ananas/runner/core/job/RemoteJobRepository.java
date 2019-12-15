package org.ananas.runner.core.job;

import com.google.common.collect.ImmutableMap;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.misc.HttpClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RemoteJobRepository implements JobRepository {

  private static RemoteJobRepository singleton;

  private RemoteJobRepository() {
  }

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
        "http://locahost:3004/v1/job",
        new HashMap<>(),
        JsonUtil.toJson(job),
        conn -> {
          return null;
        });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Job getJob(String id) {
    try {
      return HttpClient.GET(
        "http://locahost:3004/v1/job",
        ImmutableMap.of("id", id),
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
      return HttpClient.GET(
        "http://locahost:3004/v1/jobs",
        new HashMap<>(),
        conn -> {
          return JsonUtil.fromJson(conn.getInputStream(), Set.class);
        });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Job> getJobsByScheduleId(String triggerId, int offset, int n) {
    return null;
  }

  @Override
  public List<Job> getJobsByGoal(String goalId, int offset, int n) {
    return null;
  }

  @Override
  public void deleteJob(String jobId) {
    try {
      HttpClient.PUT(
        "http://locahost:3004/v1/job/delete",
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
