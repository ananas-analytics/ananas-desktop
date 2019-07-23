---
id: version-0.8.1-cli-overview
title: Ananas Command Line Overview
sidebar_label: CLI
original_id: cli-overview
---

Ananas Command Line Tool is an executable java jar. You need to have JDK 1.8 + installed to run it.

## Installation

- Download the [Ananas Analytics CLI](../downloads/overview) binary, or compile it from [source](https://github.com/ananas-analytics/ananas-desktop)
- Unzip the downloaded zip file

If you want an alias `ananas`, and command line auto completion, run the `install.sh` script to install it. Now you can run command line

```bash
ananas
```

Otherwise you can run the the executable jar with 

```bash
java -jar ananas-cli.jar
``` 

If you choose to run the command line with executable jar, replace `ananas` with `java -jar ananas-cli.jar` in all the following examples.

> All commands exit 0 when succeed, 1 when fail

## Show project details

```bash
Usage: ananas show [--config] [--dataframe] [--steps] [--variables]
                   [--step=stepId] -p=<project>
Show analytics board details
      --config              Show step config
      --dataframe           Show step dataframe
      --step=stepId         Specify the step
      --steps               Show steps
      --variables           Show variables
  -p, --project=<project>   Ananas analytics project path
```


For example, the following command shows all steps in Fifaplayer2019 project:

```bash
ananas show -p /Users/ananas/examples/FifaPlayer2019 --steps
```

The following command shows the config of the step id `5ce6bc0f17968182c59c0a61`:

```bash
ananas show -p /Users/ananas/examples/FifaPlayer2019 --step=5ce6bc0f17968182c59c0a61 --config
```

## Test step

```bash
Usage: ananas test [-t] [-e=<profile>] [-o=<output>] -p=<project>
                   [-m=<String=String>]... <goal>
Test the settings of one step
      <goal>                Id of the step to test
  -e, --profile=<profile>   Profile yaml file, includes execution engine, and
                              parameters (optional). By default, local Flink engine,
                              no parameter
  -m, --param=<String=String>
                            Override the parameter defined in profile, the parameter
                              must be defined in ananas board
  -o, --output=<output>     Output file
  -p, --project=<project>   Ananas analytics project path
  -t, --detail              Show test details
```


The following command test the step `5ce6bc0f17968182c59c0a61` with `France` as the value of variable `NATIONALITY` 

```bash
ananas test -p /Users/ananas/examples/FifaPlayer2019 5ce6bc0f17968182c59c0a61 -mNATIONALITY=France
```

## Run step(s)

Only destination and visualization are runnable.

```bash
Usage: ananas run [--port=<port>] [-e=<profile>] [-i=<interval>] -p=<project>
                  [-m=<String=String>]... [<goals>...]
Run analytics task
      [<goals>...]          Id of the target steps to run (only LOADER and VIEWER)
      --port=<port>         Ananas server port, default 3003
  -e, --profile=<profile>   Profile yaml file, includes execution engine, and
                              parameters (optional). By default, local Flink engine,
                              no parameter
  -i, --interval=<interval> State polling interval in seconds, default 5
  -m, --param=<String=String>
                            Override the parameter defined in profile, the parameter
                              must be defined in ananas board
  -p, --project=<project>   Ananas analytics project path
```

The following command run the step `5ce6bc0f17968182c59c0a61` with `France` as the value of variable `NATIONALITY` on embeded local Flink engine (as there is no profie specified) 

```bash
ananas run -p /Users/ananas/examples/FifaPlayer2019 5ce6bc0f17968182c59c0a61 -mNATIONALITY=France
```

You can run multiple steps at the same time
```bash
ananas run -p /Users/ananas/examples/FifaPlayer2019 5ce6bc0f17968182c59c0a61 5ce6cb8110f33df5c3c8e459
```

The following command run the step `5ce6bc0f17968182c59c0a61` with `France` as the value of variable `NATIONALITY` on Spark.  

```bash
ananas run -p /Users/ananas/examples/FifaPlayer2019 -e /Users/ananas/examples/FifaPlayer2019/profile_spark_dev.yml 5ce6bc0f17968182c59c0a61 -mNATIONALITY=France
```


