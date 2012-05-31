#!/bin/bash

cd executables
tar czvf all_packages.tgz prosevis.linux32.tar.gz \
  prosevis.linux64.tar.gz \
  prosevis.mac.tar.gz \
  prosevis.win32.zip \
  prosevis.win64.zip \
  data.zip
