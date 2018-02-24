#!/bin/bash

set -e

cd /manager/susemanager-utils/testing/docker/scripts/

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

# Database schema creation

rpm -ivh /root/susemanager-schema-2.1.50.5-0.7.1.noarch.rpm

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset ORACLE database

echo $PATH
echo $PERLLIB

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf

# Database schema creation
spacewalk-setup --clear-db --db-only --answer-file=clear-db-answers-oracle.txt --external-oracle --non-interactive || {
  cat /var/log/rhn/populate_db.log
  exit 1
}

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

# guessing the link
# obsolete: we have this link now in the package.
#for v in `seq 30 -1 11`; do
#    minusone=$(($v-1))
#    if [ -d /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-3.0.$minusone-to-susemanager-schema-3.0.$v ]; then
#        mkdir /etc/sysconfig/rhn/schema-upgrade/susemanager-schema-3.0.$v-to-susemanager-schema-3.1.0
#        # set hard this destination
#        export SUMA_TEST_SCHEMA_VERSION="3.1.1"
#        break
#    fi
#done
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

# oracle shutdown (avoid stale memory by shmget())
/etc/init.d/oracle stop ||:

