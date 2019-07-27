#!/bin/sh

: '
java -jar $(pwd)/../cli/*.jar show -p $(pwd)/Fifa2019 --steps
if [ "$?" -ne "0" ]; then
  exit 1
fi
'

# Test exploring the csv source
echo Explore data source
RESULT=$(java -jar $(pwd)/../cli/*.jar explore -p $(pwd)/Fifa2019 5d31c71a84b5674b6a220288)
if [ "$?" -ne "0" ]; then
  exit 1
fi
OUTPUT=$(echo "$RESULT" | jq '.data|.data|length == 0')
if [ "$OUTPUT" != "true" ]; then
  exit 1
fi


# Test transformer
echo Test transformer - "Top players with 85+ overall ratings"
RESULT=$(java -jar $(pwd)/../cli/*.jar test -p $(pwd)/Fifa2019 5d31cb6684b5674b6a22028c)
if [ "$?" -ne "0" ]; then
  exit 1
fi
OUTPUT=$(echo "$RESULT" | jq '(.data|.["5d31cb6684b5674b6a22028c"]|.schema|.fields|length == 2) and (.data|.["5d31cb6684b5674b6a22028c"]|.data|length > 0)')
if [ "$OUTPUT" != "true" ]; then
  exit 1
fi
