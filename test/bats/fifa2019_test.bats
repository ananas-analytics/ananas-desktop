#!/usr/bin/env bats

# Please define ANANAS_JAR && EXAMPLE HOME from command line to run the test
# ANANAS_JAR=
# EXAMPLE_HOME=
# DEBUG_OUTPUT=~/Dev/tmp/tmp.log

run_only_test() {
  if [ "$BATS_TEST_NUMBER" -ne "$1"  ]; then
    skip
  fi

}

# setup() {
#  run_only_test 1
# }

@test "Fifa2019 - explore csv source" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Fifa2019
  # get explore data
  result="$(java -jar $ANANAS_JAR explore 5d31c71a84b5674b6a220288 -n 1 -s 5)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # check schema
  schema_size="$(echo $result | jq '.data|.schema|.fields|length')"
  [ "$schema_size" -eq 89 ]
  # check data
  data_size="$(echo $result | jq '.data|.data|length')"
  [ "$data_size" -eq 5 ]
}

@test "Fifa2019 - test transformer" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Fifa2019
  # get explore data
  result="$(java -jar $ANANAS_JAR test 5d31cb6684b5674b6a22028c)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # check schema
  schema_size=$(echo $result | jq '.data|."5d31cb6684b5674b6a22028c"|.schema|.fields|length')
  [ "$schema_size" -eq 2 ]
  # check data
  data_size="$(echo $result | jq '.data|."5d31cb6684b5674b6a22028c"|.data|length')"
  [ "$data_size" -gt 0 ]
}

@test "Fifa2019 - test concat" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Fifa2019
  # get explore data
  result="$(java -jar $ANANAS_JAR test 5d31d45184b5674b6a2202c2)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # check schema
  schema_size=$(echo $result | jq '.data|."5d31d45184b5674b6a2202c2"|.schema|.fields|length')
  [ "$schema_size" -eq 4 ]
  # check data
  data_size="$(echo $result | jq '.data|."5d31d45184b5674b6a2202c2"|.data|length')"
  [ "$data_size" -gt 0 ]
}

@test "Fifa2019 - test data viewer" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Fifa2019
  # get explore data
  result="$(java -jar $ANANAS_JAR run 5d31d26784b5674b6a2202b8)"
	# echo "$result" >> "$DEBUG_OUTPUT"
	
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # get job id
  jobid="$(echo $result | jq -r '.data|.jobid')"

  # check data
  view_result="$(java -jar $ANANAS_JAR view $jobid 5d31d26784b5674b6a2202b8)"

	schema_size=$(echo $view_result | jq '.data|.schema|.fields|length')
  [ "$schema_size" -eq 2 ]

  data_size=$(echo $view_result | jq '.data|.data|length')
  [ "$data_size" -gt 0 ]
}

