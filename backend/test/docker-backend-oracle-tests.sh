#! /bin/bash

/manager/backend/test/oracle-init.sh
cp /root/rhn.conf /etc/rhn/rhn.conf
mkdir -p /manager/backend/reports
nosetests --with-xunit --xunit-file /manager/backend/reports/oracle_tests.xml /manager/backend/test/runtests-oracle.py
EXIT=$?
/etc/init.d/oracle stop
exit $?
