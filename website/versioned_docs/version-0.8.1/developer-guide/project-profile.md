---
id: version-0.8.1-project-profile
title: profile.yml
sidebar_label: project/profile.yml
original_id: project-profile
---

A profile define the execution engine and the variable values (or parameters) when running the analysis jobs.

The profile file is optional, when running a project from command line without providing profile file, the embedded Local Flink Engine is used with default variable values: `empty string` for string type and credential type, `0` for number type, `1970-1-1 0:0:0` for date type

## Example profile file

There are two sections in profile yaml file: `engine` and `params`

```yaml
engine:
  description: ""
  name: Spark Dev
	type: Spark # ---------------------------------------------(1)
	scope: project # ------------------------------------------(2)
  properties:  # ----------------------------------------------(3)
    database_password: ${visualization_db_password} # ---------(4)
    database_type: mysql
    database_url: mysql://db-host:3306/ananas_visual
    database_user: root
    enableMetricSinks: "true"
    sparkMaster: spark://spark-master:7077
    streaming: "false"
    tempLocation: /tmp/
params: # -----------------------------------------------------(5)
  score_threshold: "60" 
  other_variable: "true"
```

### (1) engine type

Currently we support 3 types of engines:

- Spark
- Flink
- Google Dataflow

more engines coming soon ...

### (2) engine scope

Engine scope in project profile file must be `project`, to be differentiate from the workspace level engine definitions in workspace.yml

### (3) engine properties

Engine properties is a string to string map. Different engine has different properties. A more detailed documentation will cover the config for different engine type.

### (4) visualization database for execution engine

Visualization is a special type of destination, to help Ananas find the job result to be visualized. When running analysis job on a specific engine, you need to tell Ananas Engine where to store these visualization data and make sure the engine platform has the right network settings and access permission to access it.

Two types of databases are supported:

- MySQL
- PostgreSQL

### (5) parameters 

Parameters are a string to string map. The key is the variable name, and the value iss the variable value in **string**.

 


