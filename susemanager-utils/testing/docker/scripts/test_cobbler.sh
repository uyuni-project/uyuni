#!/bin/bash

set -e

zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run the cobbler unit tests
zypper in -y  --no-recommends cobbler-tests cobbler-tests-containers

cp /root/cobbler-apache.conf /etc/apache2/conf.d/cobbler.conf
cp /root/modules.conf /etc/cobbler/modules.conf
cp /root/cobbler_web.conf /etc/apache2/vhosts.d/cobbler_web.conf
cp /root/apache2 /etc/sysconfig/apache2
cp /root/sample.ks /var/lib/cobbler/kickstarts/sample.ks

# migrate modules.conf
/usr/share/cobbler/bin/settings-migration-v1-to-v2.sh -s

# start apache - required by cobbler tests
/usr/sbin/start_apache2 -D SYSTEMD  -k start

ln -s /usr/share/cobbler/ /code

# Configure PAM
useradd -p $(perl -e 'print crypt("test", "password")') test

sh /code/docker/develop/scripts/setup-supervisor.sh || true

# Run system-tests bootstrap to prepare dhcpd and expected pxe testing interface
sh /docker-setup-cobbler-system-tests-env.sh

# execute the tests

cd /code/tests

pytest --junitxml=/reports/cobbler.xml
