#!/bin/bash
set -e

cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

# do the real test
zypper --non-interactive --gpg-auto-import-keys ref
zypper --non-interactive in hoag-dummy orion-dummy
zypper --non-interactive up milkyway-dummy

# kill avahi
/usr/sbin/avahi-daemon -k
