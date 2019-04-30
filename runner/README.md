
# api
API that test or run DAGs

## Build Instructions
 Install Java 8 ( or more recent version)

## Running 

1. Use IntelijIDEA IDE
2. Import Project 
3. Run Main class

or 

1. Build the fat jar
```gradle clean build fatjar```

2. Run 
```java -jar build/libs/ananas-all-0.1.jar 8888``` 
where first program argument is the port ( 3003 by default)

## Healthcheck

HTTP GET http://localhost:8888/healthcheck


## Testing (deprecated)

Run the script that provisions test data for pipeline "CSV scores"
```bash
$  ./tests/2_testcase_run_pipleine.sh
```

## API result reference

### General Guide

All requests will return HTTP status code 200, no matter if there is an error in the service

### When the API request is successfully served
HTTP status: 200
Response Body:

```
{
 code: 200,
 data: [any]
}
```

### When the API request is failed
HTTP status: 200
Response Body:

```
{
 code: [error code], // must NOT be 200
 message: string, // error message
}
```

## Endpoint


```  
  /v1/jobs/:jobid/poll
```  
  Get the current job state

``` 
 curl -sXGET http://localhost:3003/v1/jobs/5bd061d2219c020ef93e5f69/poll   -H "Content-Type: application/json"   -H "Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1YmI2NTc4OGIyZjdkYmM5OGIzMTk1ODQiLCJlbWFpbCI6ImdAZy5jb20iLCJwZXJtaXNzaW9uIjoicnciLCJpYXQiOjE1NDAzNzE1MDMsImV4cCI6MTU0MDk3NjMwM30.RuWwW4m0bQ_TA0y9FDzRAGo-9QV6MsQAlHGL-qU32VA" {
  "code" : 200,
  "data" : {
    "id" : "5bd061d2219c020ef93e5f69",
    "state" : "DONE",
    "pipelineid" : "5bbd03000f2ec37893004f2b"
  }

``` 

```  
  /v1/jobs/
```  
  List jobs
Example : 
```  
curl -sXGET http://localhost:3003/v1/jobs/   -H "Content-Type: application/json"   -H "Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1YmI2NTc4OGIyZjdkYmM5OGIzMTk1ODQiLCJlbWFpbCI6ImdAZy5jb20iLCJwZXJtaXNzaW9uIjoicnciLCJpYXQiOjE1NDAzNzE1MDMsImV4cCI6MTU0MDk3NjMwM30.RuWwW4m0bQ_TA0y9FDzRAGo-9QV6MsQAlHGL-qU32VA" {
  "code" : 200,
  "data" : [ {
    "id" : "5bd061d2219c020ef93e5f69",
    "state" : "DONE",
    "pipelineid" : "5bbd03000f2ec37893004f2b"
  } ]
```  


```  
  /v1/jobs/:id/cancel
```  
  Cancel the job execution

```  
  /v1/:step/paginate?page=1&pagesize=3
```  
  Paginate a Source and return the dataframe ( same response as test pipeline endpoint) 


Example 1: Read a text file 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "subtype": "file",
  "platform": "local",
  "format": "text",
  "path": "/home/grego/Documents/data/links.csv"
}'
```
Response : 
```  
{
    "code": 200,
    "data": {
        "id": "lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh",
        "schema": {
            "fields": [
                {
                    "index": 1,
                    "name": "text",
                    "type": "VARCHAR",
                    "userDefined": false,
                    "rowSchema": null,
                    "arrayElementType": null
                }
            ]
        },
        "data": [
            [
                "2,0113497,8844"
            ],
            [
                "3,0113228,15602"
            ],
            [
                "4,0114885,31357"
            ]
        ]
    }
}
```


Example 2: Read a CSV file 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "subtype": "file",
  "platform": "local",
  "format": "csv",
  "header": true,
  "path": "/home/grego/Documents/data/links.csv"
}' 
```

Example 3: Read an Excel file 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "subtype": "file",
  "platform": "local",
  "format": "excel",
  "path": "/home/grego/Documents/data/sales_sheet.xlsx"
}'
```

Example 4: Read a JSON file 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "subtype": "file",
  "platform": "local",
  "format": "json",
  "path": "/home/grego/Documents/data/auctions_1_9.json"
}'  
```  

Example 5: Query a JDBC DERBY table with postgresql syntax 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=12&pagesize=2' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{ "subtype": "jdbc",
  "database": "derby",
  "url": "derby:/home/grego/datumania;create=true", 
  "sql": "select count(*), deviceType, dealId from table_test2 group by dealId, deviceType limit 10",
  "sql_dialect" : "postgres"
}'
```
If you omit sql_dialect parameter, the sql statement is not translated and its dialect is the database dialect. 

Example 6 : Query a mongo table 
```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=3&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{ "subtype": "mongo",
 "filters": "{}",
 "host": "localhost",
 "port": "27017",
 "database": "datumania",
 "collection": "step" 
}'
```  

Example 7 : Paginate an API connector 

```  
curl -X POST \
  'http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 3b3acfe5-28fc-d842-969a-83fc4f2b78d6' \
  -d '{
  "subtype": "file",
  "platform": "local",
  "format": "api",
  "url": "http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/paginate?page=1&pagesize=3",
  "method": "post",
  "headers" : null,
  "body": {
  "subtype": "file",
  "platform": "local",
  "format": "text",
  "path": "/home/grego/Documents/data/links.csv"
}
}'
```  


```  
  /v1/:projectid/dag/test
```  
Test a DAG
  
Example  :  

```
curl -X POST \
  http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/dag/test \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "dag": {
    "connections": [
      {
        "source": "csvstepId1",
        "target": "jsonstepId1"
      },
      {
        "source": "csvstepId1",
        "target": "jsonstepId2"
      },
      {
        "source": "csvstepId1",
        "target": "sqltransformer"
      },
      {
        "source": "sqltransformer",
        "target": "jsonstepId3"
      }
    ],
    "steps": [
      {
        "id": "csvstepId1",
        "type": "connector",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/home/grego/Documents/data/auctions_1_9.json"
        }
      },
      {
        "id": "jsonstepId1",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader2"
        }
      },
      {
        "id": "jsonstepId2",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader2"
        }
      },
      {
        "id": "jsonstepId3",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader3"
        }
      },
      {
        "id": "sqltransformer",
        "type": "transformer",
        "config": {
          "subtype": "sql",
          "sql": "select * from PCOLLECTION"
        }
      }
    ],
    "runnables": [
      "jsonstepId2",
      "jsonstepId3"
    ]
  },
  "variables": {}
}'
```

```  
  /v1/:projectid/dag/run
```  
Run a given DAG. 
The API returns 200 status response and launches the DAG execution asynchronously. 
   To get the job status the client API must poll the job status. 


 Example : 
```
  http://localhost:3003/v1/lmj234LKJ4LLMKJ5KJ5JLJ5LJ5LKJ51Hjkjdjhdkh/dag/run \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'cookie: dmxId=abcdefg' \
  -H 'dnt: 1' \
  -H 'postman-token: ce572729-e25a-16ab-0e97-3bdeeb1753bf' \
  -H 'referer: http://www.dailymotion.com/' \
  -H 'x-real-ip: 198.54.201.163' \
  -b dmxId=abcdefg \
  -d '{
  "dag": {
    "connections": [
      {
        "source": "csvstepId1",
        "target": "jsonstepId1"
      },
      {
        "source": "csvstepId1",
        "target": "jsonstepId2"
      },
      {
        "source": "csvstepId1",
        "target": "sqltransformer"
      },
      {
        "source": "sqltransformer",
        "target": "jsonstepId3"
      }
    ],
    "steps": [
      {
        "id": "csvstepId1",
        "type": "connector",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/home/grego/Documents/data/auctions_1_9.json"
        }
      },
      {
        "id": "jsonstepId1",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader1"
        }
      },
      {
        "id": "jsonstepId2",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader2"
        }
      },
      {
        "id": "jsonstepId3",
        "type": "loader",
        "config": {
          "subtype": "file",
          "platform": "local",
          "format": "json",
          "path": "/tmp/",
          "prefix": "loader3"
        }
      },
      {
        "id": "sqltransformer",
        "type": "transformer",
        "config": {
          "subtype": "sql",
          "sql": "select * from PCOLLECTION"
        }
      }
    ],
    "runnables": [
      "jsonstepId3"
    ]
  },
  "variables": {}
}'
```
RESPONSE
```
{
  "code" : 200,
  "data" : {
    "jobid" : "5bd061d2219c020ef93e5f69",
    "sessionid" : "53d061d2219c020ef93e5z34",
  }
```  

# Machine learning

## ML Workflow 

The user will **train**, optionnaly **validates** (test) and then use an ML model to **predict** Y response variable from any step output dataframe X columns. 

*Train* --> *Test* --> *Predict*

1. **Train mode**

*Precondition*: The user has connected the ML step to at least 1 step 

The user chooses an ML algorithm and train it. The runner API returns stats on the ML model.  

2. **Test mode**

*Precondition*: The user has connected the ML step to at least 2 step

The user chooses a *Train* step and a *test* step. It validates the trained model by comparing Y response variable in Train and Test outputs. Both schema must be equivalent so that the validator can compare values and return the error rate. 

Note on *Train* and *Test* mode:  
* The API runner caches input of train and test step by flushing automatically ML steps input dataframes to disk if there is any change detected in previous steps.
* The ML step reads file cache and run ML model training or testing in batch. No apache beam runner is involved at this step. The ML model is trained locally in your localhost, serialized and then store in ananas home directory with ML step stepId. 

3. **Predict mode**

When a Step is using the *predict* mode, the model is deserialized ( note that it was previously serialized at training time) and then used by the predictor transformer. A prediction is similar to any transformer. The predictor transformer predicts a Y response variable (ie a Y column) in each row from its set of X columns ( the ML features). X columns must match X columns defined in the "train" mode otherwise an error is thrown. 
 
## ML Algorithms 

There are 4 types of algorithms : 
* Clustering ( Unsupervised algorithms) 
* Classification ( supervised algorithms where Y response variable is a discrete value (int) )
* Regression ( supervised algorithms where Y response variable is a continuous value (double)
* Feature selection ( it helps user to select Best k features ie "X" columns ) 

The main difference between unsupervised and supervised algorithms is that test mode is not required/relevant/omitted for unsupervised algorithm.  

Feature selection is a special case because the mode is always "train". The user will use this algorithm to report statistics on train data to select best features for any model. 

For those 4 types of algorithms we will list them . Note that each algorithm has specific config key/value pair expected. The API runner will throw an exception if required key/value is missing. 

### Clustering

* "kmeans"
* "xmeans"
* "gmeans"
* "deterministicannealing"
* "clarans"

Example : 
```
      {
        "id": "mlstep",
        "type": "transformer",
        "config": {
          "subtype": "ml",
          "algo": "kmeans",
          "trainId": "csvtrain",  //id of step having train data
          "predictId" : "csvtrain", //step having input data to be predicted (a column is added )
          "kcluster": 2, // specific kmeans parameter
          "mode": "predict" //mode can be "train" or "predict"
        }
      }
```

### Classification

* "adaboost"
* "gradienttreeboost"
* "decisiontree"
* "knn"
* "logisticregression"
* "neuralnetwork"
* "randomforest"
* "rda"

Example:
```
      {
        "id": "mlstep",
        "type": "transformer",
        "config": {
          "subtype": "ml",
          "algo": "adaboost",
          "trainId": "csvtrain",  //id of step having train data
          "testId" : "csvtest",   //id of step having test data
          "predictId" : "csvtrain", //step having input data to be predicted (a column is added )
          "ntrees": 2,   //specific adaboost param
          "mode": "predict" //mode can be "train", "test" or "predict"
        }
      }
```

### Regression

* "gbregression"
* "lasso"
* "ols"
* "randomeforestregression"
* "ridge"

Example:
```
      {        "id": "mlstep",
        "type": "transformer",
        "config": {
          "subtype": "ml",
          "algo": "lasso",
          "trainId": "csvtrain",  //id of step having train data
          "testId" : "csvtest",   //id of step having test data
          "predictId" : "csvtrain", //step having input data to be predicted (a column is added )
          "lambda": 0.8,   //specific lasso param
          "mode": "predict" //mode can be "train", "test" or "predict"
        }
      }
```

### Feature selection

* "gafeatureselection"

Example:
```
      {        
        "id": "mlstep",
        "type": "transformer",
        "config": {
          "subtype": "ml",
          "algo": "gafeatureselection",
          "algo_selected" : "adaboost", // can be any regression or classification algorithm 
          "trainId": "csvtrain",  //id of step having train data
          "mode": "train" //mode can only be "train"
        }
      }
```

## ML Algorithms Documentation 
Each algo has a wiki page ( in progress ) . 
See https://github.com/bhou/datumania-runner/wiki/ML-Learning-Reference 



## References

https://beam.apache.org/get-started/quickstart-java/
http://sparkjava.com/

