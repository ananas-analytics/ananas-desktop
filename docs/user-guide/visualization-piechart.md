---
id: visualization-piechart
title: Pie Chart
sidebar_label: [NEW] Pie Chart
---

> Available from version 0.9.0+

![ananas analytic pie chart](assets/piechart.png)


# Settings

- Dimension

  The possible columns used as the X axis, only `String` (or compatible) type can be used as dimensions. Only 1 column can be selected

- Measure

  The numeric columns to be visualized, only numeric type could be selected.

- Chart Title

  The title of the chart

- Display in 3D?

	Display in 3D

- Display in donut? 

  Display as donut. If `Display in 3D?` option is selected, this option is ignored.

# Advanced Settings

- SQL

  The SQL query to filter the result for visualization. 
  
  Note: SQL query will not be used to calculate the processed data. It only filters the data for display. You can change the way your job result looks like without re-running the job.
