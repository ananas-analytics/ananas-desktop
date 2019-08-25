---
id: source-postgresql
title: PostgreSQL
---

![postgres source](assets/postgresql_source.png)

Imports data from PostgreSQL

## What it does 

Connect to and import data from a PostgreSQL database

PostgreSQL is a powerful, open source object-relational database that has earned it a strong reputation for reliability, feature robustness, and performance.

## Settings 

* Username

  the user name to connect the database

* Password

  the password to connect the database

* Database full URL

  the database url, for example: `postgresql://localhost:5432/[your database name]`

* Table Name 

  the table that you want to work on

## Advanced Settings

* SQL query
  
  you can use an SQL query to filter the data that you want to work on. By default, all data will be processed (`SELECT *`). 

## Tips

  Using the advanced SQL query settings to filter the data can reduce the processing time.

