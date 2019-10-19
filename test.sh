#!/bin/sh

./build-cli.sh

./gradlew cleanTest test -Dananas.test.properties=$TEST_PROPERTIES --console plain 

./test/bats/run.sh $ANANAS_DIR/cli/ananas-cli-latest.jar $ANANAS_DIR/ananas-examples $ANANAS_DIR/bats-test.log $ANANAS_DIR/test/bats/fifa2019_test.bats 

./test/bats/run.sh $ANANAS_DIR/cli/ananas-cli-latest.jar $ANANAS_DIR/ananas-examples $ANANAS_DIR/bats-test.log $ANANAS_DIR/test/bats/excel_test.bats 
