---
id: version-0.8.0-source-mysql
title: MySQL
original_id: source-mysql
---

Imports data from MySQL

## What it does 

Connect to and import data from a MySQL database

MySQL is an open source relational database management system developed by Oracle.

## Settings 

* Username

  the user name to connect the database

* Password

  the password to connect the database

* Database full URL

  the database url, for example: `mysql://localhost:3306/[your datebase]`

* Table 

  the table that you want to work on

## Advanced Settings

* SQL query
  
  you can use an SQL query to filter the data that you want to work on. By default, all data will be processed (`SELECT *`). 

## Tips

  Using the advanced SQL query settings to filter the data can reduce the processing time.



