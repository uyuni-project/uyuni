#!/bin/bash
set -e

rpm -ih --nodeps /root/*.rpm
cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

zypper --non-interactive --gpg-auto-import-keys ref
zypper --non-interactive in aaa_base aaa_base-extras net-tools timezone vim less sudo tar python python-xml
