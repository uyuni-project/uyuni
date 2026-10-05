#!/bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

langs=(
bn_IN
de
en_US
es
fr
gu
hi
it
ja
ko
pa
pt_BR
ru
ta
zh_CN
zh_TW
)

for lang in "${langs[@]}"
do
    ./check_java.sh ../.. "$lang"
done
