#!/bin/bash
TMP_FILE=.tmptmptmptmptmptmp
# have to dump all the folders off of this one
#LIBS=$(find lib | grep .jar$ | sed -r 's/lib//' | sed -r 's/processingopengl/ /' | tr -s '\n' ':')
LIBS=$(find lib | grep .jar$ | sed 's|processingopengl/||' | \
  sed -r 's|^lib|			<string>$JAVAROOT|' | sed -r 's|.jar$|.jar</string>|' | \
  tr "\n" "~")
CP=$LIBS"			<string>\$JAVAROOT/prosevis.jar</string>"
SENTINEL=THISISWHERETHEJARFILESGO

echo 
echo Fixing classpath in $1
echo 

# run this as the last line so that the return code from this is given as the
# return code from the script, thus avoiding silent errors
cat $1 | sed  "s|${SENTINEL}|${CP}|" | tr "~" "\n" > $TMP_FILE && cat $TMP_FILE > $1 && rm $TMP_FILE
