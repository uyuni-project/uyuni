#!/usr/bin/python  #  pylint: disable=missing-module-docstring,invalid-name
#
# Checking if the values encoded in hex (base 16) are properly decoded.
#

import os
import sys

filename = "base16values.txt"
filename = os.path.join(os.path.dirname(sys.argv[0]), filename)
f = open(filename)  #  pylint: disable=unspecified-encoding

while 1:
    line = f.readline()
    if not line:
        break
    arr = line.split(" ", 1)
    if len(arr) != 2:
        break
    i = int(arr[0])
    val = int(arr[1], 16)
    assert i == val, i
