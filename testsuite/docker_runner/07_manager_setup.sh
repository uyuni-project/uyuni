#!/bin/bash
set -xe
docker exec uyuni-server-all-in-one-test-1 bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
