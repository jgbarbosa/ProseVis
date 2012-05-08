#!/bin/bash

if [ $# -lt 1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi
if [ ! -d $1 ]; then
  echo "usage: ./scripts/push_to.sh <path to cp packages to>"
  exit -1
fi

cp prosevis.linux32.tar.gz \
  prosevis.linux64.tar.gz \
  prosevis.mac.tar.gz \
  prosevis.win32.zip \
  prosevis.win64.zip \
  $1
