#!/bin/bash

# Enable hostname resolving (eg. Java InetAddress.getLocalHost())
echo 127.0.0.1 `hostname`>>/etc/hosts

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_oracle_database.sh

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

cp buildconf/test/rhn.conf.oracle-example buildconf/test/rhn.conf
ant -f manager-build.xml test
