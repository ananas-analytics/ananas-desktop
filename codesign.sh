#!/bin/sh

echo "NOTE: Please first code sign the JRE specified in <JDK HOME> parameter"

# ./gradlew codesign-dir -Dapple.packager.signid="$3" -Dsigned.dir="$4/jre"

printHelp()
{
	echo "Usage:"
	echo "codesign.sh <Developer App ID> <Ananas Version> <JDK HOME>"
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

./gradlew packRunner -Pplatform=mac -PJDK_HOME="$3" -Prelease=true -DANANAS_ENV=production -Dorg.gradle.jvmargs=-Xmx4g

if [ "$?" -ne 0 ]; then
	exit 1
fi

# rm -rf tmp/jar-signing 
./gradlew codesign-libs -Dapple.packager.signid="$1" -Dsigned.jar="runner-all-$2.jar"

if [ "$?" -ne 0 ]; then
	exit 1
fi

./gradlew packMac -Pplatform=mac -PJDK_HOME="$3" -Prelease=true -DANANAS_ENV=production -Dorg.gradle.jvmargs=-Xmx4g

if [ "$?" -ne 0 ]; then
	exit 1
fi



