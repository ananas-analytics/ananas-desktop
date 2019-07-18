---
id: version-0.8.0-project-ananas
title: ananas.yml
sidebar_label: project/ananas.yml
original_id: project-ananas
---

`ananas.yml` is the mandatory project file in [**YAML**](https://yaml.org/) format, it contains following information:

- steps, theirs settings, and optionally step output schemas 
- connections among steps
- variable definitions

## Ananas File Example

Here is an example `ananas.yml` file

```yml
# unique id of the project
id: the-unique-project-id-of-example-project
# project name
name: example project
# a map of steps in the project, with unique step id as the key
steps:
	student-score-csv-source: # step id, must be same as the id field
		id: student-score-csv-source 
		name: student score
		type: connector	# -----------------------------------(1)
		metadataId: org.ananas.source.file.csv # ------------(2)
		description: | +  # ---------------------------------(3)
			# Student score source 
			
			The CSV file contains the students score
		config: # -------------------------------------------(4)
			subtype: file
			format: csv
			header: true
			path: ${PROJECT_PATH}/score.csv # ---------------(5)
			header: true
		dataframe: # ----------------------------------------(6)
			schema:
				fields:
					- name: Id
					  type: VARCHAR
					- name: Name
					  type: VARCHAR
					- name: Score
					  type: INTEGER
					- name: Grade
					  type: INTEGER
				data: [] # ----------------------------------(7)

	not-pass:
		id: not-pass
		name: Student not pass exam by grade
		type: transformer
		metadataId: org.ananas.transform.sql
		description: Student not pass exam by grade
		config:
			subtype: sql
			sql: |-
				SELECT 
					count(1) as cnt,
					grade
				FROM PCOLLECTION
				WHERE score < ${score_threshold} # -----------(8)
				GROUP BY grade

	save-to-result-csv:
		id: save-to-result-csv
		name: Save result to csv
		type: loader
		metadataId: org.ananas.destination.file.csv
		description: Save average score by grade to CSV
		config:
			subtype: file
			format: csv
			header: true
			path: ${PROJECT_PATH}/output/
			prefix: average-score-by-grade

dag:
	connections: # ------------------------------------------(9)
		- source: student-score-csv-source
		  target: average-by-grade
		- source: average-by-grade
		  target: save-to-result-csv
				
variables: # ------------------------------------------------(10) 
	- name: score_threshold
	  description: the score threshold 
	  scope: project
	  type: number
```

Some more details in the example:


### (1) step types

Each step must be one of the following 4 types:

- connector
- transformer
- loader
- viewer

### (2) step metadata id 

Each step must have one metadata to help the Ananas understand what kind of config is expected from this step.

You can find the list of metadata from the [step metadata list](https://github.com/ananas-analytics/ananas-desktop/tree/master/ui/resources/metadata)

### (3) step description

You can have an optional description text for step in Markdown.

### (4) step config

Each step has a config map containing necessary information of the step. A more detailed config for each type of step will be added in developer guide soon. 

### (5) variable in step config

It is possible to use variable in any step config values. Here we are referencing the predefined `PROJECT_PATH` variable

### (6) step dataframe (schema)

You can also attach an output dataframe with schema in the step. This will allow Ananas uses the specified schema instead auto-detecting one.  

> You need to set connector `forceAutoDetectSchema` config to `false` to inform Ananas stop auto-detecting schema

### (7) step dataframe example data

You can also attach some example dataframe data to the step. This will help others better understand how data looks like

### (8) reference user defined variable

Beside user defined variable, you can create and reference you own variable too

### (9) connect steps as a DAG

DAG is defined as a list of connections with source and target point to step id.

### (10) user defined variables

You can define your own variables in `variables` section. Set variable `scope` to `project` makes the Ananas GUI user possible to modify the variable. If you do not want non-technical user to modify the variable definition from GUI, set it to `runtime`.
