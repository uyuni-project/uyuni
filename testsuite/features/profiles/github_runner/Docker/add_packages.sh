#!/bin/bash
set -e

cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

# skip installing packages
# this will fail for github actions because the daemon
# runs on the ubuntu github runner and has no access
# zypper --non-interactive --gpg-auto-import-keys ref
# zypper --non-interactive in  

# zypper --non-interactive in hoag-dummy orion-dummy
# zypper --non-interactive up milkyway-dummy

# kill avahi
/usr/sbin/avahi-daemon -k
