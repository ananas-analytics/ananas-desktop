package org.ananas.acceptance.helper;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class NotSoInnocuousWorkerThread extends ForkJoinWorkerThread {
  protected NotSoInnocuousWorkerThread(ForkJoinPool pool) {
    super(pool);
  }
}
