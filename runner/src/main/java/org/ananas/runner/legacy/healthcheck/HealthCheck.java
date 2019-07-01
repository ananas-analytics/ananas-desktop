package org.ananas.runner.legacy.healthcheck;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class HealthCheck {

  public boolean healthy;
  public Date upTime;
  public String version;

  public HealthCheck() {
    this.healthy = true;
    this.upTime = new Date();
    MavenXpp3Reader reader = new MavenXpp3Reader();

    try {
      this.version = reader.read(new FileReader("pom.xml")).getVersion();
    } catch (IOException e) {
    } catch (XmlPullParserException e) {
    }
  }
}
