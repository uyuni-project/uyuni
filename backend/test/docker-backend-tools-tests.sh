#! /bin/bash

touch /etc/rhn/rhn.conf
nosetests --with-xunit --xunit-file /manager/backend/reports/satellite_tools_tests.xml -s /manager/backend/satellite_tools/test/unit/
