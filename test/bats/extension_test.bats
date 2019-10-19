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

@test "Extension - run data view" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Extension

	java -jar $ANANAS_JAR extension install https://raw.githubusercontent.com/ananas-analytics/ananas-extension-example/master/versions.json@0.1.0

  # get explore data
  result="$(java -jar $ANANAS_JAR run number_view)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
	# get job id
  jobid="$(echo $result | jq -r '.data|.jobid')"

	# check extensions folder exists and the extension exists
	[ -d "${EXAMPLE_HOME}/Extension/extensions" ] 
	[ -d "${EXAMPLE_HOME}/Extension/extensions/ananas-extension-example/0.1.0" ] 

	# check data
  view_result="$(java -jar $ANANAS_JAR view $jobid number_view)"
	# echo "$view_result" >> $DEBUG_OUTPUT

	schema_size=$(echo $view_result | jq '.data|.schema|.fields|length')
  [ "$schema_size" -eq 3 ]

  data_size=$(echo $view_result | jq '.data|.data|length')
  [ "$data_size" -gt 0 ]

	# remove extension
	java -jar $ANANAS_JAR extension remove ananas-extension-example@0.1.0
	[ ! -d "${EXAMPLE_HOME}/Extension/extensions/ananas-extension-example/0.1.0" ] 
}

@test "Extension - install & remove all extension versions" {
  run bash -c 'echo ERROR; false'

  cd "$EXAMPLE_HOME"/Extension

	java -jar $ANANAS_JAR extension install https://raw.githubusercontent.com/ananas-analytics/ananas-extension-example/master/versions.json@0.1.0

  # get explore data
  result="$(java -jar $ANANAS_JAR run number_view)"
  # check code
  code="$(echo $result | jq .code)"
  [ "$code" -eq 200 ]
	# get job id
  jobid="$(echo $result | jq -r '.data|.jobid')"

	# check extensions folder exists and the extension exists
	[ -d "${EXAMPLE_HOME}/Extension/extensions" ] 
	[ -d "${EXAMPLE_HOME}/Extension/extensions/ananas-extension-example/0.1.0" ] 

	# check data
  view_result="$(java -jar $ANANAS_JAR view $jobid number_view)"
	# echo "$view_result" >> $DEBUG_OUTPUT

	schema_size=$(echo $view_result | jq '.data|.schema|.fields|length')
  [ "$schema_size" -eq 3 ]

  data_size=$(echo $view_result | jq '.data|.data|length')
  [ "$data_size" -gt 0 ]

	# remove extension
	java -jar $ANANAS_JAR extension remove ananas-extension-example
	[ ! -d "${EXAMPLE_HOME}/Extension/extensions/ananas-extension-example" ] 
}


