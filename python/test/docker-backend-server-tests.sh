#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/python/spacewalk/reports
pytest --verbose --junit-xml /manager/python/spacewalk/reports/server_tests.xml -s /manager/python/test/unit/spacewalk/server
