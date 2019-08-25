---
id: source-webapi
title: [NEW] Web API Source
---

> Available from version 0.9.0+

![web api source](assets/source_webapi.png)

Connect data from Web API

## What it does 

Read data from web API through HTTP(s).

## Settings 


* HTTP method
  
	One of `GET`, `POST`, and `PUT`

* API URL

	The URL of the web API, includes the protocol (HTTP|HTTPS) and parameters

* Data Format 

	Currently supports `TEXT` and `JSON` data format

* Headers

	HTTP headers, a string to string map

* HTTP Body

	The request body in text. Only available when HTTP method is `POST` or `PUT`

## Advanced Settings

* Description
	
	Documentation of the source in markdown format

* Use Json path to filter data (JSON data format only)

	If **Data Format** is set to `JSON`, you can specify a json path to filter the result. See [JsonPath](https://goessner.net/articles/JsonPath/) for the syntax of json path.

* Line Separator (TEXT data format only)

	The charactor used to separate plain text response	

## Examples

[Weather Forcast Example](https://github.com/ananas-analytics/ananas-examples)


## Tips 

* Use `Credential` type [Variable](variable-overview) for API keys and Authentication tokens to avoid include your credentials directly in the settings.




