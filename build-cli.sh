#!/bin/sh

if [ -z "$1" ]
  then
    echo "Usage: build.sh <JDK8_HOME>"
		exit 1
fi

rm runner/build/libs/ananas-cli*.jar

./gradlew :runner:shadowJar -Dorg.gradle.java.home=$1 -Dtarget=cli

cp runner/build/libs/ananas-cli*.jar ./cli



