#! /bin/bash

touch /etc/rhn/rhn.conf
nosetests --with-xunit --xunit-file /manager/backend/reports/common_tests.xml -s /manager/backend/common/test/unit-test/
