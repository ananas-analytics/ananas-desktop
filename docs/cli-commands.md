---
id: cli-commands
title: CLI commands
---

Ananas Desktop tools comes with a set of CLI commands for developers. 

> This function is still in Alpha phase, it could be changed in the future release.

## What CLI commands are for? 

CLI commands help your developers testing, running and deploying your data flow job in any environment.

## Requirements

> Ensure you have Java installed (> 1.8)

Download [ananas analytics jar](http://bit.ly/2Cv0qsk)

## CLI test

```sh
usage: test
 -d,--project_directory <arg>   use given directory path
 -i,--step_id <arg>             The id of the step
 -p,--profile_file <arg>        use given profile file. Variables are
                                defined in this file.
 -test                          Test a project step. This will test a step
                                without recording its state. For testing
                                purpose input dataframe are sliced and
                                sampled.
```

### Example 

Testing step `5c80e70b57512602965dbc95` defined in the project folder `5c753f37eac4f456f9f11055` 
```sh
 java -jar datumania-runner-0.4.jar run -d $HOME/.config/AnanasAnalytics/5c753f37eac4f456f9f11055/ -i 5c80e70b57512602965dbc95
```




