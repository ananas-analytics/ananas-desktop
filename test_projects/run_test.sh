#!/bin/sh

for d in */ ; do
	echo ---------------------------
  echo TESTING "$d" ...
	echo ---------------------------
	"$d"test.sh
	if [ "$?" -ne "0" ]; then
	  echo
	  echo FAIL!
	  exit 1
	else
	  echo
	  echo PASS!
	fi
	echo
done
