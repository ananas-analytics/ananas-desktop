---
id: version-0.8.0-destination-google-gcs
title: Google Cloud Storage
original_id: destination-google-gcs
---

Export data to Google Cloud Storage 

> To access Google Cloud Platform, you need a credential file. See [**Google Application Authentication**](https://cloud.google.com/docs/authentication/getting-started) Documentation. And set the `GOOGLE_APPLICATION_CREDENTIALS` env in Ananas workspace.yml file. See [**Google Dataflow Engine**](engine-dataflow.md)

## What it does 

Export data to the files stored in Google Cloud Storage.

Google Cloud Storage is the unified object storage for developers and enterprises from Google.

## Settings 

* File Format

  The file format, current version supports CSV & JSON Log

* Bucket Name

  The Google Cloud Storage bucket name

* Destination Path

	The path in the bucket to store the output file

* File prefix

  The prefix of the output file

## Advanced Settings

* Redownload sample file

	When exploring Google Cloud Storage files, Ananas Desktop will download and cache sample file to your local computer to make exploration faster. This option will make Ananas Desktop clear its cache and redownload the sample file. 

* Explore sample size limit (MB)

	The size limit of the downloaded sample file. By default, the smallest file is downloaded. If there is no file smaller than this size limit, Ananas Desktop will choose the smallest file and cut it to the size limit. 0 means no limit.


# Tips

You can directly explore the result from the destination editor.
	

