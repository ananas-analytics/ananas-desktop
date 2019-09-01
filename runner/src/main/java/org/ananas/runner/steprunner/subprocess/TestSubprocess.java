package org.ananas.runner.steprunner.subprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ananas.runner.steprunner.subprocess.utils.CallingSubProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSubprocess {

  static final Logger LOG = LoggerFactory.getLogger(SubProcessTransformer.EchoInputDoFn.class);

  public static void main(String[] args) throws Exception {

    Map<String, Object> m = new HashMap<>();
    m.put(SubProcessConfiguration.BINARY_NAME, "python");
    m.put(SubProcessConfiguration.SOURCE_FILE, "hello.py");
    m.put(SubProcessConfiguration.SOURCE_PATH, "/home/grego/");
    m.put(SubProcessConfiguration.WORKER_PATH, "/tmp/");
    m.put(SubProcessConfiguration.WAIT_TIME, "1000");
    m.put(SubProcessConfiguration.CONCURRENCY, 1);
    m.put(SubProcessConfiguration.ONLY_UPLOAD_LOGS_ON_ERROR, false);

    SubProcessConfiguration configuration = new SubProcessConfiguration(m);
    CallingSubProcessUtils.setUp(configuration, configuration.executableName);
    try {

      // The ProcessingKernel deals with the execution of the process
      SubProcessKernel kernel = new SubProcessKernel(configuration);

      // Run the command and work through the results
      List<String> results = kernel.exec("hello");
      for (String s : results) {
        System.out.println(s);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
