---
id: engine-spark
title: Apache Spark
---

## Basic Settings

- Engine Name 

	The name of the engine, this name will be shown in the `RUN` dropdown options

- Engine Description

	The description of the engine

- Master URL

	The url of the Spark Master. It can either be local[x] to run local with x cores, spark://host:port to connect to a Spark Standalone cluster, mesos://host:port to connect to a Mesos cluster, or yarn to connect to a yarn cluster.

- Temp Location

	A pipeline level default location for storing temporary files. Could be a local location or a cloud location for example google cloud storage `gs://`.

- Enable Metrics Sinks

	Enable reporting metrics to Spark's metrics Sinks. Default: false

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
