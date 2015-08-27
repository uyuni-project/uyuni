#!/bin/bash

/etc/init.d/oracle start

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_oracle_database.sh

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

cp /root/rhn.conf /etc/rhn/rhn.conf

rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected

cp buildconf/test/rhn.conf.oracle-example buildconf/test/rhn.conf
ant -f manager-build.xml refresh-branding-jar test


/etc/init.d/oracle stop
