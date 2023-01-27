#!/bin/bash

set -xe

echo 'root:root123!' | chpasswd
# FIXME hack to correct the report db script to work on containers
cp /usr/bin/uyuni-setup-reportdb /usr/bin/uyuni-setup-reportdb.original
sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb
sed -i 's/-a "$PG_SOCKET"//g' /usr/bin/uyuni-setup-reportdb
