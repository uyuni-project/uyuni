#!/bin/bash
set -xe
sudo -i podman exec uyuni-server-all-in-one-test bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "/usr/bin/spacewalk-schema-upgrade -y"
# Make sure latest sql migration scripts have been executed
sudo -i podman exec uyuni-server-all-in-one-test bash -c "/testsuite/podman_runner/run_db_migrations.sh"
