#!/bin/bash

docker run -v "${PWD}/../:/manager" manager_java_testing_pgsql /bin/sh -c "rcpostgresql start; rhn-satellite-activate --rhn-cert /manager/branding/setup/spacewalk-public.cert --disconnected; cd /manager/java; ant resolve-ivy; ant -f manager-build.xml run-tests-locally"

