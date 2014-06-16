#!/bin/bash

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_pgsql_database.sh

cp /manager/java/scripts/rhn.conf.pgsql /etc/rhn/rhn.conf

# SUSE Manager initialization
sysctl -w kernel.shmmax=18446744073709551615
smdba system-check autotuning
rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy
ant -f manager-build.xml test

# Postgres shutdown (avoid stale memory by shmget())
rcpostgresql stop

