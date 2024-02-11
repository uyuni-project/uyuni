#!/bin/bash
set -e
/usr/bin/systemctl is-active multi-user.target
salt-call --local --no-color status.ping_master localhost |grep -q True
curl --fail http://localhost/rhn/manager/login
