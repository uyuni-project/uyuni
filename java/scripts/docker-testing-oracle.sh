#!/bin/bash

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_oracle_database.sh

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

ssh root@1c059.qa.suse.de rhn-satellite-activate --rhn-cert /usr/share/spacewalk/setup/spacewalk-public.cert --disconnected

cp buildconf/test/rhn.conf.oracle-example buildconf/test/rhn.conf
ant -f manager-build.xml test
