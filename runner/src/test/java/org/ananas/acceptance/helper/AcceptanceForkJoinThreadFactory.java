package org.ananas.acceptance.helper;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class AcceptanceForkJoinThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
  @Override
  public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
    return new NotSoInnocuousWorkerThread(pool);
  }
}
