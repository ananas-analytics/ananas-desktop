---
id: visualizing
title: Visualizing
---

![ananas analytic bar chart](assets/get_started_visualization.png)

## Visualizing your data


You can visualize your data by [running](running.md) a job in a visualization step.  

1. **Drag & drop** one of the visualization step

2. Connect the visualization step to the step of your choice

3. Choose the **dimensions** X and Y of your choice

4. Choose the **measures** which will be the actual Y values. 

5. Click `test` button to make sure you didn't miss any field. 

6. Now it's time to run your job to compute your measures on all data. Click `run`. 

7. If the job has completed successfully, you should see its state as `DONE`. 

8. Click `Explore` to **vizualize** your data.

9. **Refine** your chart by changing the SQL query in advanced settings

Congratulations you've got a chart in minutes. 


> Once you've run your visualization job, intermediate data is stored in your desktop. This means you don't have to run it again whenever you change your SQL query to filter the data for visualization. 


## Advanced settings

Beware that sometimes it's useful to order your X and Y axis by sorting them in the Query. Any updates can be done directly in the Query. For example use the SQL `ORDER BY` and click `explore` button to display X in order. 

