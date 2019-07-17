---
id: source-google-gcs
title: Google Cloud Storage
---

Imports data from Google Cloud Storage 


> To access Google Cloud Platform, you need a credential file. See [**Google Application Authentication**](https://cloud.google.com/docs/authentication/getting-started) Documentation. And set the `GOOGLE_APPLICATION_CREDENTIALS` env in Ananas workspace.yml file. See [**Google Dataflow Engine**](engine-dataflow.md)

## What it does 

Connect to and import data from the files stored in Google Cloud Storage.

Google Cloud Storage is the unified object storage for developers and enterprises from Google.

## Settings 

* File Format

  The file format, current version supports CSV & JSON Log

* Bucket Name

  The Google Cloud Storage bucket name

* File Path/Pattern

  You can specify a single file path, or a set of files with File Pattern. For example, `a/b/**/*.log` matches both `a/b/c/d.log` and `a/b/c/d/e/f.log`

## Advanced Settings

* Force schema auto detection

	Should ananas auto detect json structure if there is already one defined in the project.

* Redownload sample file

	When exploring Google Cloud Storage files, Ananas Desktop will download and cache sample file to your local computer to make exploration faster. This option will make Ananas Desktop clear its cache and redownload the sample file. 

* Explore sample size limit (MB)

	The size limit of the downloaded sample file. By default, the smallest file is downloaded. If there is no file smaller than this size limit, Ananas Desktop will choose the smallest file and cut it to the size limit. 0 means no limit.

## Tips

  Ananas Analytics Desktop will automatically unzip your file(s) if your file name ends with `.gz` and `.zip`. We suggest to set explore sample size limit to 0 for compressed file.


	

