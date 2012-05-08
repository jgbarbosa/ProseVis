#!/bin/bash

if [ $# -lt 1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi
if [ ! -d $1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi

cp executables/prosevis.linux32.tar.gz \
  executables/prosevis.linux64.tar.gz \
  executables/prosevis.mac.tar.gz \
  executables/prosevis.win32.zip \
  executables/prosevis.win64.zip \
  $1
