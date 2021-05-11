#! /bin/bash

touch /etc/rhn/rhn.conf
mkdir -p /manager/backend/reports
pytest --verbose --junit-xml /manager/backend/reports/server_tests.xml --ignore '/manager/backend/server/test/unit-test/rhnSQL/' -s /manager/backend/server/test/unit-test/
