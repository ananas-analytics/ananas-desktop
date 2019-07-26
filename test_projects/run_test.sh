#!/bin/sh

for d in */ ; do
  echo Testing "$d" ...
	"$d"test.sh
	if [ "$?" -ne "0" ]; then
	  echo ---------------------------
	  echo FAILED
	  echo ---------------------------
	  exit 1
	fi
done
