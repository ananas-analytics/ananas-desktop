#!/bin/sh

: '
java -jar $(pwd)/../cli/*.jar show -p $(pwd)/Fifa2019 --steps
if [ "$?" -ne "0" ]; then
  exit 1
fi
'

echo Test "Top players with 85+ overall ratings"
RESULT=$(java -jar $(pwd)/../cli/*.jar test -p $(pwd)/Fifa2019 5d31cb6684b5674b6a22028c)
if [ "$?" -ne "0" ]; then
  exit 1
fi
OUTPUT=$(echo "$RESULT" | jq '(.data|.["5d31cb6684b5674b6a22028c"]|.schema|.fields|length == 3) and (.data|.["5d31cb6684b5674b6a22028c"]|.data|length > 0)')
test "$OUTPUT" -eq "false"
