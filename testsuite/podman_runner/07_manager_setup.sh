#!/bin/bash
set -xe
export CR=docker
${CR} exec uyuni-server-all-in-one-test bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
