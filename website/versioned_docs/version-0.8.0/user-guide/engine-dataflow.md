---
id: version-0.8.0-engine-dataflow
title: Google Dataflow
original_id: engine-dataflow
---

## Prerequisite

Before you run analysis job on Google Dataflow. You need to setup a `GOOGLE_APPLICATION_CREDENTIALS` environment.

To generate google cloud application credential file, see [**Google Application Authentication**](https://cloud.google.com/docs/authentication/getting-started). Usually, it is a json file.  

Next go to [**Ananas Analytics Desktop workspace folder**](where-to-find), edit the `workspace.yml` file, and add the following `env` settings: 

```yml
settings:
  env:
    GOOGLE_APPLICATION_CREDENTIALS: [your_google_credentials_file_path]
```

## Basic Settings

- Engine Name 

	The name of the engine, this name will be shown in the `RUN` dropdown options

- Engine Description

	The description of the engine

- Project Id

	The google cloud project id that you want to run the dataflow job

- Google Cloud Storage Temp Location

	A pipeline level default location for storing temporary files on Google cloud storage `gs://`.

## Visualization Storage Settings

> Visualization is a special form of Destination, where a relational database is used to host the final result so that Ananas Desktop can query and visualize the result. When running on a different engine, it is mandatory to specify a relational database that the engine can access.

- Database Type

	Choose between MySQL and PostgreSQL

- Database URL
	
	The url of the database. Please make sure your engine have good network setting and permission to access it	

- Database User

	The database user name

- Database Password

	The database password
