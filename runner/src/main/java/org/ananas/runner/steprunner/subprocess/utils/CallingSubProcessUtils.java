package org.ananas.runner.steprunner.subprocess.utils;

import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.ananas.runner.steprunner.subprocess.SubProcessConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class for dealing with concurrency and binary file copies to the worker. */
public class CallingSubProcessUtils {

  // Prevent Instantiation
  private CallingSubProcessUtils() {}

  static final Logger LOG = LoggerFactory.getLogger(CallingSubProcessUtils.class);

  static boolean initCompleted = false;

  // Allow multiple subclasses to create files, but only one thread per subclass can add the file to
  // the worker
  private static final Set<String> downloadedFiles = Sets.<String>newConcurrentHashSet();

  // Limit the number of threads able to do work
  private static Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();

  public static void setUp(SubProcessConfiguration configuration, String binaryName)
      throws Exception {

    if (!semaphores.containsKey(binaryName)) {
      initSemaphore(configuration.getConcurrency(), binaryName);
    }

    synchronized (downloadedFiles) {
      if (!downloadedFiles.contains(binaryName)) {
        // Create Directories if needed
        FileUtils.createDirectoriesOnWorker(configuration);
        LOG.info("Calling filesetup to move Executables to worker.");
        ExecutableFile executableFile = new ExecutableFile(configuration, binaryName);
        FileUtils.copyFileFromGCSToWorker(executableFile);
        downloadedFiles.add(binaryName);
      }
    }
  }

  public static synchronized void initSemaphore(Integer permits, String binaryName) {
    if (!semaphores.containsKey(binaryName)) {
      LOG.info(String.format(String.format("Initialized Semaphore for binary %s ", binaryName)));
      semaphores.put(binaryName, new Semaphore(permits));
    }
  }

  private static void aquireSemaphore(String binaryName) throws IllegalStateException {
    if (!semaphores.containsKey(binaryName)) {
      throw new IllegalStateException("Semaphore is NULL, check init logic in @Setup.");
    }
    try {
      semaphores.get(binaryName).acquire();
    } catch (InterruptedException ex) {
      LOG.error("Interupted during aquire", ex);
    }
  }

  private static void releaseSemaphore(String binaryName) throws IllegalStateException {
    if (!semaphores.containsKey(binaryName)) {
      throw new IllegalStateException("Semaphore is NULL, check init logic in @Setup.");
    }
    semaphores.get(binaryName).release();
  }

  /** Permit class for access to worker cpu resources. */
  public static class Permit implements AutoCloseable {

    private String binaryName;

    public Permit(String binaryName) {
      this.binaryName = binaryName;
      CallingSubProcessUtils.aquireSemaphore(binaryName);
    }

    @Override
    public void close() {
      CallingSubProcessUtils.releaseSemaphore(binaryName);
    }
  }
}
