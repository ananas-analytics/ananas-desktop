#!/bin/sh

echo "NOTE: Please first code sign the JRE specified in <JDK HOME> parameter, edit this file to enable it, if not yet done it"

printHelp()
{
	echo "Usage:"
	echo "pack-mac.sh <Developer ID Application> <Ananas Version> <JDK HOME>"
}

if [ -z "$1" ]; then
	printHelp
	exit 1
fi

if [ -z "$2" ]; then
	printHelp
	exit 1
fi

if [ -z "$3" ]; then
	printHelp
	exit 1
fi

# 1. code sign jvm (do it only once)

# ./gradlew codesign-dir -Dapple.packager.signid="$1" -Dsigned.dir="$3/jre"

# 2. pack runner, codesign ananas jar and runner app, & build app without code signing

./codesign.sh "$1" "$2" "$3"

# 3. code sign electron builder related Frameworks 
# NOTE: the codedesign.dir is related to buildSrc/src/main/resources
./gradlew codesign-dir -Dcodesign.dir="../../../../bin/mac/Ananas Analytics Desktop Edition.app/Contents/Frameworks" -Dapple.packager.signid="$1"

# 4. code sign app

codesign -s "$1" -v -f --deep --entitlements "./ui/build/entitlements.mac.plist" --options runtime "./bin/mac/Ananas Analytics Desktop Edition.app"


