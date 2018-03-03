#!/bin/bash

set -e
MYHOSTNAME=`hostname`

echo "
LISTENER =
  (DESCRIPTION_LIST =
    (DESCRIPTION =
      (ADDRESS = (PROTOCOL = TCP)(HOST = $MYHOSTNAME)(PORT = 1521))
      (ADDRESS = (PROTOCOL = IPC)(KEY = EXTPROC1521))
    )
  )
" > /opt/apps/oracle/product/12.1.0/dbhome_1/network/admin/listener.ora

umount /dev/shm
mount tmpfs -t tmpfs -o defaults /dev/shm

/etc/init.d/oracle start

cd /manager/susemanager-utils/testing/docker/scripts/

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset pgsql database

echo $PATH
echo $PERLLIB

export SYSTEMD_NO_WRAP=1
#sysctl -w kernel.shmmax=18446744073709551615
#/etc/init.d/oracle start

# this copy the latest schema from the git into the system
./build-schema.sh

################################################
####### START COMMENT OUT
####### IF A FIXED DESTINATION IS WANTED
################################################

RPMVERSION=`rpm -q --qf "%{version}\n" --specfile /manager/schema/spacewalk/susemanager-schema.spec | head -n 1`
NEXTVERSION=`echo $RPMVERSION | awk '{ pre=post=$0; gsub("[0-9]+$","",pre); gsub(".*\\\\.","",post); print pre post+1; }'`

if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-$RPMVERSION-to-susemanager-schema-$NEXTVERSION ]; then
    export SUMA_TEST_SCHEMA_VERSION=$NEXTVERSION

else
    export SUMA_TEST_SCHEMA_VERSION=$RPMVERSION
fi

###############################################
####### END
#export SUMA_TEST_SCHEMA_VERSION="3.0"
###############################################

# run the schema upgrade from git repo
if ! /manager/schema/spacewalk/spacewalk-schema-upgrade -y; then
    cat /var/log/spacewalk/schema-upgrade/schema-from-*.log
    /etc/init.d/oracle stop
    exit 1
fi

echo "Creating First Org"

echo "
VARIABLE x NUMBER
call create_new_org('Test Default Organization', '$RANDOM', :x);
" | spacewalk-sql --select-mode -

echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id, product_url)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
            'private-channel-family-1', 1, 'First Org Created');" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -

#/etc/init.d/oracle restart
echo "Ready!"
sleep infinity
