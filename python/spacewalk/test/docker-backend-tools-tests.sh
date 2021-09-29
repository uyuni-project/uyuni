#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/backend/reports
pytest -v --junit-xml /manager/backend/reports/satellite_tools_tests.xml -s /manager/backend/satellite_tools/test/unit/
