#!/bin/sh

for d in */ ; do
	echo ---------------------------
  echo TESTING "$d" ...
	echo ---------------------------
	"$d"test.sh
	echo
	if [ "$?" -ne "0" ]; then
	  echo FAIL!
	  exit 1
	else
	  echo PASS!
	fi
	echo
done
