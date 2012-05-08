#!/bin/bash
TMP_FILE=.tmptmptmptmptmptmp
# have to dump all the folders off of this one
#LIBS=$(find lib | grep .jar$ | sed -r 's/lib//' | sed -r 's/processingopengl/ /' | tr -s '\n' ':')
LIBS=$(find lib | grep .jar$ | sed 's|processingopengl/||' | \
  sed -r 's|^lib|$JAVAROOT|' | tr -s '\n' ':')
CP=$LIBS"prosevis.jar"
SENTINEL=FIXMEFIXMEFIXME

echo 
echo Fixing classpath in $1
echo 

# run this as the last line so that the return code from this is given as the
# return code from the script, thus avoiding silent errors
cat $1 | sed  "s|${SENTINEL}|${CP}|" > $TMP_FILE && cat $TMP_FILE > $1 && rm $TMP_FILE
