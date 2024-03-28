#!/bin/bash
set -ex
if [ ! -d /tmp/test-all-in-one ];then
    mkdir /tmp/test-all-in-one
fi

hostname
hostname --fqdn