#!/bin/bash

if [ $# -lt 1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi
if [ ! -d $1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi

cp -R executables/prosevis.linux32.tar.gz \
  executables/prosevis.linux64.tar.gz \
  executables/ProseVis.app \
  executables/prosevis.win32.zip \
  executables/prosevis.win64.zip \
  executables/data.zip \
  $1
