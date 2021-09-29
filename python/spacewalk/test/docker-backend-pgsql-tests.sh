#! /bin/bash

/sbin/sysctl -w kernel.shmmax=4067832832
su - postgres -c '/usr/lib/postgresql/bin/pg_ctl start'
cp /root/rhn.conf /etc/rhn/rhn.conf
mkdir -p /manager/python/spacewalk/reports
pytest -v --junit-xml /manager/python/spacewalk/reports/pgsql_tests.xml /manager/python/spacewalk/test/runtests-postgresql.py
EXIT=$?
su - postgres -c '/usr/lib/postgresql/bin/pg_ctl stop'
exit $?
