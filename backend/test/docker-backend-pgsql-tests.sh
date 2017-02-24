#! /bin/bash

/sbin/sysctl -w kernel.shmmax=4067832832
#su - postgres -c '/usr/lib/postgresql-init start'
rcpostgresql start
cp /root/rhn.conf /etc/rhn/rhn.conf
nosetests --with-xunit --xunit-file /manager/backend/reports/pgsql_tests.xml /manager/backend/test/runtests-postgresql.py
EXIT=$?
#su - postgres -c '/usr/lib/postgresql-init stop'
rcpostgresql stop
exit $?
