---
id: version-0.8.0-destination-google-bigquery
title: Google Cloud BigQuery
original_id: destination-google-bigquery
---

Export data from Google Cloud BigQuery 

> To access Google Cloud Platform, you need a credential file. See [**Google Application Authentication**](https://cloud.google.com/docs/authentication/getting-started) Documentation. And set the `GOOGLE_APPLICATION_CREDENTIALS` env in Ananas workspace.yml file. See [**Google Dataflow Engine**](engine-dataflow.md)

## What it does 

Connect to and export data to table in Google Cloud BigQuery.

Google Cloud BigQuery is a serverless, highly-scalable, and cost-effective cloud data warehouse with an in-memory BI Engine and machine learning built in.

## Settings 

* Project Id

  The google cloud project id

* Dataset

	The dataset in Google BigQuery

* Table Name
	
	The table where you wants to read data from

* BigQuery SQL
	
  you can use BigQuery SQL to filter the final result when the job is done. 

## Advanced Settings

* Overwrite

	Overwrite the table ro append to the end
	
## Tips

  - You don't need to replace `[TABLE]` in the BigQuery SQL field, Ananas Analytics Desktop will inject the right value according to your settings.

	

