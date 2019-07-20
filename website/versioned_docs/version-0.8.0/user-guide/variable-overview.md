---
id: version-0.8.0-variable-overview
title: Variable Overview
sidebar_label: Overview
original_id: variable-overview
---

## Overview

*Ananas Analytics Desktop* provides a variable system to help you parameterize your analysis.

For example, in our [first data flow](getting-started) guide, we analyzed the top 10 countries with most footplayers with a simple SQL transform:

```sql
SELECT 
    count(1) AS players,
    Nationality
FROM PCOLLECTION
GROUP BY Nationality
ORDER BY players DESC
LIMIT 10
``` 

What if we want to analyze top 20 countries? 

One solution would be to modify the SQL from `LIMIT 10` to `LIMIT 20`, and rerun the analysis. But when you have complex data flow with multiple steps, finding the right step to modify turns out to be a tricky task. What if you want to change such parameter multiple times per day? isn't it much easier to put all the parameters in a single place?

*Ananas Analytisc Desktop* Variable tab helps you to create new variables that you can use in your data analysis flow.

![variable tab](assets/variable_tab.png)


## Add a new variable

In variable tab (left sidebar), click `+` button leads you to a new variable editor.

![new variable](assets/variable_new.png)

By applying a unique variable name (please avoid white space in the variable), and choose the variable type.

There are 4 types variables:

- String
- Number
- Date
- Credential

You can also provide an optional description in the editor.

## Use variable in your step settings

To reference a variable in your step settings, you can use the following format:

```bash
${variable_name}
```

For example, in our Fifaplayer example, we can replace `LIMIT 10` to `LIMIT ${nationality_num}`

```sql
SELECT 
    count(1) AS players,
    Nationality
FROM PCOLLECTION
GROUP BY Nationality
ORDER BY players DESC
LIMIT ${nationality_num}
``` 

On the right sidebar -> variable tab, you can review current available variables. Above `Test` and `RUN` button, there would be a variable field ready for you to fill the parameter value.

![apply variable in step](assets/variable_apply.png)

Now you can use the destination and visualization steps as an ad-hoc query interface.


![ad hoc query with variable](assets/variable_run.png)

## Predefine Variables

There are several predefined variables

### EXECUTE_DATE

Type `date`. The timestamp, when your analysis job start to execute.


### PROJECT_PATH

Type `String`. The absolute project path. This variable is useful when you want to reference resources in your project folder.

## Variable Modifier

The simplest use case is to reference your variable directly in the form `${variable_name}`. You can also apply modifiers to your variables. For example, you can format a `date` type variable to `year-month-day` format with following:

```bash
${date_var?string['YYYY-MM-dd']}
``` 

More details on how to use modifiers see [FreeMarker Built-in reference](https://freemarker.apache.org/docs/ref_builtins.html)

## Tips

Variables can be used not only in step settings but also Execution Engine settings
