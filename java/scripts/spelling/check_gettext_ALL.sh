#!/bin/bash

# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# all langs in our po dirs
langs=(
ar
as
bg
bn_IN
bn
ca
cs
cy
da
de
el
en_GB
en
es
et
fa
fi
fr
gu
he
hi
hr
hu
hy
id
is
it_IT
it
ja
ka
kn
ko
ku
lo
lv
mk
ml
mr
ms
my
nb
nl
no
or
pa
pl
pt_BR
pt
ro
ru
si
sk
sl
sq
sr@Latn
sr
sv
ta
te
tr
uk
ur
vi
zh_CN.GB2312
zh_CN
zh_TW.Big5
zh_TW
)

for lang in "${langs[@]}"
do
    ./check_gettext.sh "$lang"
done
