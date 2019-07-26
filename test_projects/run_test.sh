#!/bin/sh

for d in */ ; do
  echo "$d"
	"$d"test.sh
done
