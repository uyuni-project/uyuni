#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/backend/reports
pytest -v --junit-xml /manager/backend/reports/common_tests.xml -s /manager/backend/common/test/unit-test/
