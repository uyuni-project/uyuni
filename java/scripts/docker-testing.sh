#!/bin/bash

# Database preparations and startup
sysctl -w kernel.shmmax=18446744073709551615
rcpostgresql start

# Schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_pgsql_database.sh

# SUSE Manager initialization
smdba system-check autotuning
rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy
ant -f manager-build.xml run-tests-locally

