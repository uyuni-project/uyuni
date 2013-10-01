#!/bin/bash

# Enable hostname resolving (eg. Java InetAddress.getLocalHost())
echo 127.0.0.1 `hostname`>>/etc/hosts

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_oracle_database.sh

# SUSE Manager initialization
sysctl -w kernel.shmmax=18446744073709551615
smdba system-check
rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy
ant -f manager-build.xml test

