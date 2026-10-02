#!/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ $# != 1 ]; then
    echo "Usage: $0 <dir>"
    exit 1
fi

dir=$1;

while [ ! -d $dir ]; do
    dir=`dirname $dir`
done

echo -n `df $dir | awk '{v=$4} END {print v}'`
