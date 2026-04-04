#!/bin/bash

CWD=`pwd`
LIST=`ls ../core/src/main/resources/com/redhat/rhn/frontend/strings/`
for j in $LIST ; do
cd ../core/src/main/resources/com/redhat/rhn/frontend/strings/$j
echo -e "$j\n==============================================="
$CWD/findmissingstrings.py
cd $CWD
done
