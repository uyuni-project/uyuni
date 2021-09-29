#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/python/spacewalk/reports
pytest -v --junit-xml /manager/python/spacewalk/reports/satellite_tools_tests.xml -s /manager/python/spacewalk/satellite_tools/test/unit/
