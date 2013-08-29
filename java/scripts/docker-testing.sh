#!/bin/bash

# Database preparations and startup
sysctl -w kernel.shmmax=18446744073709551615
smdba system-check autotuning
rcpostgresql start

# SUSE Manager initialization
rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected
mgr-ncc-sync --from-dir=/root/UC5

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy
ant -f manager-build.xml run-tests-locally

