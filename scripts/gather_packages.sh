#!/bin/bash

cd executables
zip all_packages.zip prosevis.linux32.tar.gz \
  prosevis.linux64.tar.gz \
  prosevis.mac.tar.gz \
  prosevis.win32.zip \
  prosevis.win64.zip \
  data.zip
