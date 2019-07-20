---
id: version-0.8.0-steps
title: Step
original_id: steps
---

Step is the building block of any ETL. It can be a *Source*, a *Transform*, a *Destination*, or a *Visualization*.

Visualization step is a special type of Destination, which provides a direct way to see the analysis results.  

*Sources* pull data, *Transforms* changes data, and *Destinations* push data out of Desktop application.

## Extracting

To extract any data you'll need to configure a *Source* step. Sources pull data. Basically each type of source requires different configurations.
The UI enables you to explore your datasource by jumping from one page to another with minimum latency.  

## Transforming

Once you've got data, you might want to change that data according to your analysis needs. The common scenario is to filter out some records or project only a few columns. If you build metrics, chances are you'll need to aggregate some columns. Usually this can be done with [sql](sql.md) transform. Some other scenario requires to join two steps input with a [join](join.md). 


## Loading

When you start automating your ETL you'd like to store your result to a specific *destination* such as a [postgresql database](destination-postgresql.md)  or  [csv files](destination-csv.md). 

Any *Destination* Step enables you to store processed data into any supported destination. 


## Visualization

*Visualization* step is a special type of Loader. It saves the data to a temporary and hidden data store, and provide an easy-to-configure visualization interface. You can use *Visualization* steps to visualize the result directly in Ananas Analytics.


