#!/bin/bash
PROSEVIS_NAME=prosevis
TARGET_WINDOWS64=windows64
TARGET_WINDOWS32=windows32
TARGET_MAC=macosx

TARGET=$TARGET_WINDOWS64

if [ "$1" = "mac" ]; then
  TARGET=$TARGET_MAC
fi

if [ "$1" = "win32" ]; then
  TARGET=$TARGET_WINDOWS32
fi

CLASS_DIR=bin
OUTPUT_DIR=executables

SRC=`find . | grep .java$`
LIBS=`find lib | grep .jar$`
LIB_LIST=
for jar in $LIBS
do
  LIB_LIST=$jar:$LIB_LIST
done


if [ ! -d "src" ]; then 
  echo "Please run this script from the root directory of the ProseVis repo"
  exit -1
fi

if [ -d "$CLASS_DIR" ]; then 
  rm -r $CLASS_DIR
fi

mkdir $CLASS_DIR

if [ -d "$OUTPUT_DIR" ]; then 
  rm -r $OUTPUT_DIR
fi
mkdir $OUTPUT_DIR

echo compiling the source into binaries
javac -d $CLASS_DIR -classpath $LIB_LIST $SRC

echo creating the jar file for our binaries
jar cmf scripts/manifest $OUTPUT_DIR/$PROSEVIS_NAME.jar -C $CLASS_DIR prosevis

echo copying dependency jar files
cp $LIBS $OUTPUT_DIR

echo copying over data
cp -r public_data colorschemes $OUTPUT_DIR

echo copying over the opengl binaries for $TARGET
cp lib/processingopengl/$TARGET/* $OUTPUT_DIR

echo zipping executable
pushd $OUTPUT_DIR
find . | zip $PROSEVIS_NAME.$TARGET.zip -@
popd
