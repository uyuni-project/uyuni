#!/bin/bash

set -e

zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run the cobbler unit tests
zypper in -y  --no-recommends cobbler-tests

cp /root/cobbler-apache.conf /etc/apache2/conf.d/cobbler.conf
cp /root/modules.conf /etc/cobbler/modules.conf
cp /root/cobbler_web.conf /etc/apache2/vhosts.d/cobbler_web.conf
cp /root/apache2 /etc/sysconfig/apache2
cp /root/sample.ks /var/lib/cobbler/kickstarts/sample.ks

# start apache - required by cobbler tests
/usr/sbin/start_apache2 -D SYSTEMD  -k start

# start cobbler daemon
cobblerd

# execute the tests

cd /usr/share/cobbler/tests
pytest --junitxml=/reports/cobbler.xml
