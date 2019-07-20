---
id: version-0.8.0-sql
title: SQL
original_id: sql
---

SQL Step is the core transform you can use to **filter**, **transform** and **aggregate** you data. 

## What it does

Query & Transform your data with SQL. All SQL steps use `PCOLLECTION` as the table name, it stands for the input data that connects to the SQL step

## Settings

- SQL
  
  the SQL query

## Basic syntax

This page describes the SQL dialect supported by Desktop application. 

[SQL Reference Guide](https://beam.apache.org/documentation/dsls/sql/calcite/overview/)

SQL is the most powerful Query language in earth. Here are a few things you can do in SQL to get started. 

### Filtering Column

```sql
WHERE Day_of_Week = 1
```


### Aggregating Column

```sql
SELECT count(*), min(size), max(size), average(size)
```


### Renaming column

```sql
SELECT Id as CustomerId
```


### Some maths on columns

```sql
SELECT size - 20, size * 20, weigh / size as ratio
```

### IF logic on columns

```sql
SELECT case when gender='Male' then 'M' else 'F' END 
```


### Tips

* Sometimes columns contains spaces and SQL columns is expected to be space-free. Use quote in that case
```
 select `Sale rep name`
```
* the FROM PCOLLECTION is the default clause. You do not need to change it.

