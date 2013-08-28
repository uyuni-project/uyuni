#!/bin/bash

docker run -v "${PWD}/../:/manager" manager_java_testing_pgsql_Head /bin/sh -c "sysctl -w kernel.shmmax=18446744073709551615; rcpostgresql start; rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected; mgr-ncc-sync --from-dir=/root/UC5; cd /manager/java; ant resolve-ivy; ant -f manager-build.xml run-tests-locally"

