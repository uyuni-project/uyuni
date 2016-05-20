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

# Database schema creation
cd /manager/susemanager-utils/testing/docker/scripts/
./reset_oracle_database.sh

# Resolve libs and run tests
cd /manager/java
ant resolve-ivy

cp /root/rhn.conf /etc/rhn/rhn.conf

cp buildconf/test/rhn.conf.oracle-example buildconf/test/rhn.conf
ant -f manager-build.xml refresh-branding-jar test


/etc/init.d/oracle stop
