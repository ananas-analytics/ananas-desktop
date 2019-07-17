---
title: Release Version 0.8.0
author: Bo HOU
authorURL: https://twitter.com/bhoustudio
authorFBID: 1640873970
---

We are proud to announce the first release of Ananas Desktop - a hackable data integration/analysis tool to enable non technical users to edit data processing jobs and visualise data on demand.

In the very first version, we support following features:

<!--truncate-->

## Analysis Board 

### Supported Sources
- Local CSV
- Local JSON log
- Google Cloud Storage
- Google BigQuery
- PostgreSQL
- MySQL

### Supported Transforms
- SQL transform
- JOIN
- Concat (Union)

### Supported Destinations
- Local CSV
- Google Cloud Storage
- Google BigQuery
- PostgreSQL
- MySQL

### Supported Visualization
- Bar Chart
- Line Chart
- Big Number

## Variable
- 4 types of variables: String, Number, Date, and Credential
- Predefined variables
	- EXECUTE_DATE, date, the execution date time 
	- PROJECT_PATH, string, the absolute path of the current project

## Execution Engine
- Flink
- Spark
- Google Dataflow 

## CLI tool for developer
- a CLI tool is also provided for engineers test, deploy, run, and automate Ananas data jobs.
