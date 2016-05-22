#!/bin/bash

set -e

cd /manager/susemanager-utils/testing/docker/scripts/

# Database schema creation

rpm -ivh /root/susemanager-schema-2.1.50.5-0.7.1.noarch.rpm

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset oracle database

echo $PATH
echo $PERLLIB

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf

sysctl -w kernel.shmmax=18446744073709551615
/etc/init.d/oracle start

# this command will fail with certificate error. This is ok, so ignore the error
spacewalk-setup --skip-system-version-test --skip-selinux-test --skip-fqdn-test --skip-gpg-key-import --skip-ssl-cert-generation --skip-ssl-vhost-setup --skip-services-check --clear-db --answer-file=clear-db-answers-oracle.txt --external-oracle --non-interactive ||:

# SUSE Manager initialization
cp /root/rhn.conf /etc/rhn/rhn.conf
smdba system-check autotuning

# this copy the latest schema from the git into the system
./build-schema.sh

RPMVERSION=`rpm -q --qf "%{version}" --specfile /manager/schema/spacewalk/susemanager-schema.spec`
NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-$RPMVERSION-to-susemanager-schema-$NEXTVERSION ]; then
    export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION
else
    export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
fi
# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    /etc/init.d/oracle stop
    exit 1
fi

# oracle shutdown (avoid stale memory by shmget())
/etc/init.d/oracle stop

