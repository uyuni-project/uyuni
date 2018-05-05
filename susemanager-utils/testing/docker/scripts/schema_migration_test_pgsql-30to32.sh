#!/bin/bash

set -e

cd /manager/susemanager-utils/testing/docker/scripts/

# Move Postgres database to tmpfs to speed initialization and testing up
if [ ! -z $PG_TMPFS_DIR ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh $PG_TMPFS_DIR
fi

# Database schema creation

rpm -ivh /root/susemanager-schema-3.0.10-1.1.noarch.rpm

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset pgsql database

echo $PATH
echo $PERLLIB

export SYSTEMD_NO_WRAP=1
sysctl -w kernel.shmmax=18446744073709551615
if [ -e /usr/lib/postgresql-init ]; then
    su - postgres -c "/usr/lib/postgresql-init stop" ||:
    su - postgres -c "/usr/lib/postgresql-init start"
else
    /etc/init.d/postgresql restart
fi

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf
# SUSE Manager initialization
cp /root/rhn.conf /etc/rhn/rhn.conf
smdba system-check autotuning

# this command will fail with certificate error. This is ok, so ignore the error
spacewalk-setup --skip-system-version-test --skip-selinux-test --skip-fqdn-test --skip-gpg-key-import --skip-ssl-cert-generation --skip-ssl-vhost-setup --skip-services-check --clear-db --answer-file=clear-db-answers-pgsql.txt --external-postgresql --non-interactive ||:


# this copy the latest schema from the git into the system
./build-schema.sh

RPMVERSION=`rpm -q --qf "%{version}\n" --specfile /manager/schema/spacewalk/susemanager-schema.spec | head -n 1`
NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-$RPMVERSION-to-susemanager-schema-$NEXTVERSION ]; then
    export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION

else
    export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
fi

################################################
####### START COMMENT OUT
####### IF A FIXED DESTINATION IS WANTED
################################################

set -x
# guessing the link to next major version
# THE NEXT BLOCK CAN BE COMMENTED OUT WHEN WE HAVE THE LINK PACKAGED
for v in `seq 30 -1 2`; do
    minusone=$(($v-1))
    if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-3.1.$minusone-to-susemanager-schema-3.1.$v ]; then
        if [ ! -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-3.1.$v-to-susemanager-schema-3.2.0 ]; then
            mkdir /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-3.1.$v-to-susemanager-schema-3.2.0
            # set hard this destination
            #export SUMA_TEST_SCHEMA_VERSION="3.2.1"
        fi
        break
    fi
done
###############################################
####### END
###############################################

# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    if [ -e /usr/lib/postgresql-init ]; then
        su - postgres -c "/usr/lib/postgresql-init stop"
    else
        rcpostgresql stop
    fi
    exit 1
fi

# Postgres shutdown (avoid stale memory by shmget())
if [ -e /usr/lib/postgresql-init ]; then
    su - postgres -c "/usr/lib/postgresql-init stop"
else
    rcpostgresql stop
fi
