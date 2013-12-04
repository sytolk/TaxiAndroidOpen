#!/bin/sh

VERSION=r03
# ARTIFACT=gcm-server
GROUP=com.google.android.gcm

# rm -rf $ARTIFACT* > /dev/null

echo Downloading binaries of $VERSION
# https://dl-ssl.google.com/android/repository/addon.xml
wget https://dl-ssl.google.com/android/repository/gcm_$VERSION.zip # > /dev/null

echo Preparing
unzip gcm_$VERSION.zip > /dev/null

echo Installing Maven artifact: jar
mvn install:install-file -Dfile=gcm_$VERSION/gcm-server/dist/gcm-server.jar -DgroupId=$GROUP -DartifactId=$ARTIFACT -Dversion=$VERSION -Dpackaging=jar -DgeneratePom=true > /dev/null
echo Installing Maven artifact: bin
mvn install:install-file -Dfile=$NAME-bins/x86_64-unknown-linux-gnu/librxtxSerial.so -DgroupId=$GROUP -DartifactId=$ARTIFACT -Dversion=$VERSION -Dpackaging=so -Dclassifier=bin > /dev/null

echo Downloading sources of $NAME
wget http://rxtx.qbang.org/pub/rxtx/$NAME.zip

echo Preparing
unzip $NAME.zip > /dev/null
cd $NAME/src
find . -name "CVS" -type d -print | xargs rm -rf
jar cf src.jar ./gnu

echo Installing Maven artifact: sources
mvn install:install-file -Dfile=src.jar -DgroupId=$GROUP -DartifactId=$ARTIFACT -Dversion=$VERSION -Dpackaging=jar -Dclassifier=sources > /dev/null

cd ../../
echo Cleanup
rm -rf $ARTIFACT* > /dev/null

echo Done
exit 0