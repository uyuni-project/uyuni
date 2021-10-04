#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/python/spacewalk/reports
pytest -v --junit-xml /manager/python/spacewalk/reports/common_tests.xml -s /manager/python/test/unit/spacewalk/common
