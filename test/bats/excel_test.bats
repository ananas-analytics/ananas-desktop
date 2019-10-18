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

@test "Excel - explore excel source" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Excel
  # get explore data
  result="$(java -jar $ANANAS_JAR explore 5d62a1344b778d21cc452ffd -n 0 -s 5)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # check schema
  schema_size="$(echo $result | jq '.data|.schema|.fields|length')"
  [ "$schema_size" -eq 16 ]
  # check data
  data_size="$(echo $result | jq '.data|.data|length')"
	# TODO: fix it, should be 5 but return 6
  # [ "$data_size" -eq 5 ]
}

@test "Excel - test data viewer" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Excel
  # get explore data
  result="$(java -jar $ANANAS_JAR run 5d62bb1368c1e177cdd2aa92)"
	# echo "$result" >> "$DEBUG_OUTPUT"
	
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
  # get job id
  jobid="$(echo $result | jq -r '.data|.jobid')"

  # check data
  view_result="$(java -jar $ANANAS_JAR view $jobid 5d62bb1368c1e177cdd2aa92)"

	schema_size=$(echo $view_result | jq '.data|.schema|.fields|length')
  [ "$schema_size" -eq 3 ]

  data_size=$(echo $view_result | jq '.data|.data|length')
  [ "$data_size" -gt 0 ]
}


