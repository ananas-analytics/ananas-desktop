package org.ananas.runner.core.model;

import java.util.*;
import org.ananas.runner.core.common.JsonUtil;
import org.ananas.runner.core.common.VariableRender;
import org.ananas.runner.core.model.DataSourceLoop.Loop;
import org.apache.beam.sdk.schemas.Schema;

public class LoopStepConfigGenerator {
  private Map<String, Object> config;
  public DataSourceLoop loop;

  private List<Schema.Field> beamFields;

  public LoopStepConfigGenerator(Map<String, Object> config) {
    this.loop = new DataSourceLoop();
    ((List<Map<String, Object>>)
            config.getOrDefault(Step.DATA_SOURCE_LOOP, Collections.emptyList()))
        .forEach(
            l -> {
              this.loop.addLoop(l);
            });
    this.config = config;

    this.toBeamFields();
  }

  public List<Schema.Field> toBeamFields() {
    if (beamFields != null) {
      return beamFields;
    }
    beamFields = new ArrayList<>();
    int n = this.loop.size();
    for (int i = 0; i < n; i++) {
      beamFields.add(this.loop.get(i).toBeamField());
    }
    return beamFields;
  }

  public void reset() {
    int n = this.loop.size();
    for (int i = 0; i < n; i++) {
      this.loop.get(i).reset();
    }
  }

  public boolean hasNext() {
    int n = this.loop.size();
    for (int i = 0; i < n; i++) {
      if (this.loop.get(i).hasNext()) {
        return true;
      }
    }
    return false;
  }

  public Map<String, Object> next() {
    boolean hasNextValue = prepareNext();
    if (hasNextValue) {
      return getConfig();
    }
    return null;
  }

  public boolean prepareNext() {
    int n = this.loop.size();
    for (int i = 0; i < n; i++) {
      Loop l = this.loop.get(i);
      if (l.hasNext()) {
        l.next();
        return true;
      } else {
        l.reset();
      }
    }
    return false;
  }

  public Map<String, Object> getConfig() {
    int n = this.loop.size();
    Map<String, Variable> variables = new HashMap<>();
    for (int i = 0; i < n; i++) {
      Loop l = this.loop.get(i);
      variables.put(l.name, new Variable(l.name, l.type.toString(), "", "", l.getValue()));
    }

    String json = JsonUtil.toJson(config);
    json = json.replace("%{", "${");
    return VariableRender.renderConfigFromString(variables, json);
  }

  public List<Loop> getCurrentCondition() {
    return this.loop.getAll();
  }
}
