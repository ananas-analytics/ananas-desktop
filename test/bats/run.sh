#!/bin/sh

printUsage() {
	echo "Usage: run.sh [ANANAS_JAR] [EXAMPLE_HOME] [DEBUG_LOG_PATH] [test]"
}

if [ "$#" -lt 4 ]; then
	printUsage
	exit 1	
fi

ANANAS_JAR=$1 EXAMPLE_HOME=$2 DEBUG_OUTPUT=$3 bats $4
