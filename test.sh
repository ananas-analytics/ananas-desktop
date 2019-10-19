#!/bin/sh

# build cli 
./build-cli.sh

# run runner test
./gradlew cleanTest test -Dananas.test.properties=$TEST_PROPERTIES --console plain 

# run bats test
echo 'fifa2019 test'
./test/bats/run.sh $ANANAS_DIR/cli/ananas-cli-latest.jar $ANANAS_DIR/ananas-examples $ANANAS_DIR/bats-test.log $ANANAS_DIR/test/bats/fifa2019_test.bats 

echo 'excel test'
./test/bats/run.sh $ANANAS_DIR/cli/ananas-cli-latest.jar $ANANAS_DIR/ananas-examples $ANANAS_DIR/bats-test.log $ANANAS_DIR/test/bats/excel_test.bats 

echo 'extension test'
./test/bats/run.sh $ANANAS_DIR/cli/ananas-cli-latest.jar $ANANAS_DIR/ananas-examples $ANANAS_DIR/bats-test.log $ANANAS_DIR/test/bats/extension_test.bats 




