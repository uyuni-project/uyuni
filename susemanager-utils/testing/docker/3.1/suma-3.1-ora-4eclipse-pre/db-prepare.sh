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
echo -e "\nquit\n" >> /etc/sysconfig/rhn/oracle/deploy.sql

su - oracle -c "sqlplus susemanager/susemanager@susemanager @/etc/sysconfig/rhn/oracle/deploy.sql"

