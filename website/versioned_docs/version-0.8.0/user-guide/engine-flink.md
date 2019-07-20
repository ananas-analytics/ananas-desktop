---
id: version-0.8.0-engine-flink
title: Apache Flink
sidebar_label: Apache Flink (beta)
original_id: engine-flink
---

> Apache Flink Execution Engine support is current in beta

## Basic Settings

- Engine Name 

	The name of the engine, this name will be shown in the `RUN` dropdown options

- Engine Description

	The description of the engine

- Master URL

	The url of the Flink JobManager on which to execute pipelines. This can either be the address of a cluster JobManager, in the form "host:port" or one of the special Strings "[local]" or "[auto]". "[auto]" will run the embeds local flink engine.

- Temp Location

	A pipeline level default location for storing temporary files. Could be a local location or a cloud location for example google cloud storage `gs://`.

- Parallelism

	The pipeline wide maximum degree of parallelism to be used. The maximum parallelism specifies the upper limit for dynamic scaling and the number of key groups used for partitioned state.

- Max Bundle Size

	The maximum number of elements in a bundle. Default 1000000 

- Enable Object Reuse

	Sets the behavior of reusing objects. Default: false.

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
