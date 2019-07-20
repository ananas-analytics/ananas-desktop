---
id: version-0.8.0-destination-postgresql
title: PostgreSQL
original_id: destination-postgresql
---


Export data to PostgreSQL

## What it does 

Export data to a PostgreSQL database

PostgreSQL is a powerful, open source object-relational database that has earned it a strong reputation for reliability, feature robustness, and performance.

## Settings 

* Username
  
  the user name to connect the PostgreSQL database  

* Password

  the password to connect the PostgreSQL database

* Database full URL

  the database url, for example: `postgresql://localhost:5432/[your database name]`

* Table 
  
  the table in the database to work on

## Advanced Settings

* Overwrite

	Overwrite the data in the table or append to the end

## Tips 

* Your table is automatically overriden if it already exists.
* You table is created if it does not exist.

