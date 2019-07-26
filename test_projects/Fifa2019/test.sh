#!/bin/sh

: '
java -jar $(pwd)/../cli/*.jar show -p $(pwd)/Fifa2019 --steps
if [ "$?" -ne "0" ]; then
  exit 1
fi

java -jar $(pwd)/../cli/*.jar test -p $(pwd)/Fifa2019 5d31cb6684b5674b6a22028c
if [ "$?" -ne "0" ]; then
  exit 1
fi
'

java -jar $(pwd)/../cli/*.jar test -p $(pwd)/Fifa2019 not-existed
echo "$?"
if [ "$?" -ne "0" ]; then
  exit 1
fi

