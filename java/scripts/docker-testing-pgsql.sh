#!/bin/bash

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_pgsql_database.sh

# SUSE Manager initialization
cp /root/rhn.conf /etc/rhn/rhn.conf
sysctl -w kernel.shmmax=18446744073709551615
smdba system-check autotuning
rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

cp buildconf/test/rhn.conf.postgresql-example buildconf/test/rhn.conf
ant -f manager-build.xml refresh-branding-jar test

# Postgres shutdown (avoid stale memory by shmget())
rcpostgresql stop ||:

