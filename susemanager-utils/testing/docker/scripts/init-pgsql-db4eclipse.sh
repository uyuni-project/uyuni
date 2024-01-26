#!/bin/bash

set -e

# Check if the schema package is using the new directory structure, otherwise the upgrade files are in the old
# place and so we need a symlink to make the schema upgrade script able to pick them up
if [ ! -d /usr/share/susemanager/db ]; then
    mkdir -p /usr/share/susemanager
    ln -s /etc/sysconfig/rhn /usr/share/susemanager/db
fi

cd /manager/susemanager-utils/testing/docker/scripts/

# Move Postgres database to tmpfs to speed initialization and testing up
if [ ! -z $PG_TMPFS_DIR ]; then
    trap "umount $PG_TMPFS_DIR" EXIT INT TERM
    ./docker-testing-pgsql-move-data-to-tmpfs.sh $PG_TMPFS_DIR
fi

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/:/manager/schema/spacewalk/lib
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset pgsql database

echo $PATH
echo $PERLLIB

export SYSTEMD_NO_WRAP=1
#sysctl -w kernel.shmmax=18446744073709551615
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl start" ||:

# this copy the latest schema from the git into the system
./build-schema.sh

if [ -z "$NEXTVERSION" ]; then

    RPMVERSION=`rpm -q --qf "%{version}\n" --specfile /manager/schema/spacewalk/susemanager-schema.spec | head -n 1`
    NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

    if [ -d /usr/share/susemanager/db/schema-upgrade/susemanager-schema-$RPMVERSION-to-susemanager-schema-$NEXTVERSION ]; then
        export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION

    else
        export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
    fi
else
    export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION
fi

# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
    exit 1
fi

echo "select create_new_org('Test Default Organization', '$RANDOM') from dual;" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
            'private-channel-family-1', 1);" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -

./build-reportdb-schema.sh
if [ -z "$REPORTNEXTVERSION" ]; then

    RPMVERSION=`rpm -q --qf "%{version}\n" --specfile /manager/schema/reportdb/uyuni-reportdb-schema.spec | head -n 1`
    NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

    if [ -d /usr/share/susemanager/db/reportdb-schema-upgrade/uyuni-reportdb-schema-$RPMVERSION-to-uyuni-reportdb-schema-$NEXTVERSION ]; then
        export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION

    else
        export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
    fi
else
    export SUMA_TEST_SCHEMA_VERSION=$REPORTNEXTVERSION
fi

# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y --reportdb; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
    exit 1
fi

su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
su - postgres -c '/usr/lib/postgresql/bin/postgres -D /var/lib/pgsql/data'

