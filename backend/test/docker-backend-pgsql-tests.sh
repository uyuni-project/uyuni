#! /bin/bash

/sbin/sysctl -w kernel.shmmax=4067832832
su - postgres -c '/usr/lib/postgresql10/bin/pg_ctl start'
cp /root/rhn.conf /etc/rhn/rhn.conf
mkdir -p /manager/backend/reports
nosetests --with-xunit --xunit-file /manager/backend/reports/pgsql_tests.xml /manager/backend/test/runtests-postgresql.py
EXIT=$?
su - postgres -c '/usr/lib/postgresql10/bin/pg_ctl stop'
exit $?
