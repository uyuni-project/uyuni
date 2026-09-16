#!/bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

CWD=`pwd`
LIST=`ls ../core/src/main/resources/com/redhat/rhn/frontend/strings/`
for j in $LIST ; do
cd ../core/src/main/resources/com/redhat/rhn/frontend/strings/$j
echo -e "$j\n==============================================="
$CWD/findmissingstrings.py
cd $CWD
done
