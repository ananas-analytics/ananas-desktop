
The Runner API provides a RESTful interface for submitting and managing [Apache Beam](https://beam.apache.org/) streaming data 
processing jobs, and job contexts. It was originally started at [Ananas Analytics](http://ananasanalytics.com/).

## Features

* Execute pipelines on multiple execution environments ( Spark and Flink )

* Synchronous job API for testing only with Standalone Flink.

* Asynchronous job API

* Currently works with Standalone Spark as well on cluster, Mesos, YARN client and on EMR

* Support Google Dataflow

* Support Flink (in beta)

* Support Bounded and unbounded sources

* Synchronous CLI for testing and submitting streaming data processing jobs for developers

## Build Instructions
 Install Java 8 ( or more recent version)

## Running 

1. Build the fat jar
```../build-cli.sh```

2. Run 
```java -jar build/libs/ananas-cli-xxx.jar start --host [host] --port 8888``` 

or 

1. Using gradle run task
```gradle :runner:run -DfilesToStage=mock.jar```

## Documentation

* [API Reference](https://github.com/ananas-analytics/ananas-desktop/wiki/Runner-API-Reference)

## Dependencies

* [Apache Beam](https://beam.apache.org/get-started/quickstart-java/)
* [Spark Java ](http://sparkjava.com/)

## Coming soon

* New I/O Connector : Redis, Kafka, Pubsub
* New transformer step *javascript*
 
## Contribute

* Find a non assigned feature request or a create it if it does not exist with your request
* Push a Pull Request

## Submit issues and request

[Click here to submit. Your feedback is always welcome!](https://github.com/ananas-analytics/issues/new)
