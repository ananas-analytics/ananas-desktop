---
id: version-0.8.0-testing
title: Testing
original_id: testing
---

![Testing in Ananas Analytics](assets/testing.png)

## Fail-fast testing

The core idea of testing in Ananas Analytics is to quickly verify that your input are correct. 
To do so, we provide a subset of the source data to speed up data processing time. The validation is performed on this subset.
We believe that building analytics requires such a tool to perform quick check on intermediate steps.

> Since your validation is performed on partial data, testing is not meant to render deterministic result on your data. It is rather designed to validate that your work will yield *any data* without errors on a subset of your source data. 

Testing validates authentication configuration, SQL queries on real data etc.. if all you need is to explore data and get exact result, use the explore button to [explore](exploring.md) data in details or [run your job](running.md) on a data flow to get a better insight of your data. 

</aside>
