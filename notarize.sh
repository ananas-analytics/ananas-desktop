#!/bin/sh

echo "Please first package the app with Disk Utility to a DMG"

printHelp()
{
	echo "Usage:"
	echo "notarize.sh <DMG path> <Apple ID> <Apple ID Password> <Developer ID Application>"
}

if [ -z "$1" ] 
	then
		printHelp
		exit 1
fi

if [ -z "$2" ] 
	then
		printHelp
		exit 1
fi

if [ -z "$3" ] 
	then
		printHelp
		exit 1
fi

if [ -z "$4" ] 
	then
		printHelp
		exit 1
fi

codesign -s "$4" "$1" --options runtime

echo "Uploading to notarization server ... Please wait ..."

OUTPUT=$(xcrun altool --notarize-app -f "$1" --primary-bundle-id com.ananas.desktop -u "$2" -p "$3")

echo "$OUTPUT"

ID=$(echo "$OUTPUT" | sed $'s/RequestUUID = /\\\n/g' | tail -n 1)

echo "ID is $ID"

while true
do
	echo "Check status ..."
	xcrun altool --notarization-info "$ID" -u "$2" -p "$3"
	sleep 60
done
