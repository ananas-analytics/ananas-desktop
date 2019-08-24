#!/bin/sh

rm runner/build/libs/ananas-cli*.jar
rm cli/ananas-cli*.jar

if [ -z "$1" ]
  then
    echo "use default jdk to build CLI"
    ./gradlew :runner:shadowJar -Dtarget=cli -Prelease=true
else
  echo "use jdk '$1' to build CLI"
  ./gradlew :runner:shadowJar -Dorg.gradle.java.home=$1 -Dtarget=cli -Prelease=true
fi

cp runner/build/libs/ananas-cli*.jar ./cli
cp $(ls ./cli/*.jar) ./cli/ananas-cli-latest.jar



