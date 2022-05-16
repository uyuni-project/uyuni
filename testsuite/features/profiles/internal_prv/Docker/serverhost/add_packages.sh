#!/bin/bash
set -e

# temporarily disable non-working repo
zypper mr --disable Test-Channel-x86_64 || :
zypper --non-interactive --gpg-auto-import-keys ref

# install, configure, and start avahi
zypper --non-interactive in avahi
cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

# install python3 on the container
zypper --non-interactive in python3

# re-enable normal repo and remove helper repo
zypper mr --enable Test-Channel-x86_64 || :
zypper rr sles15sp4

# do the real test
zypper --non-interactive --gpg-auto-import-keys ref
zypper --non-interactive in hoag-dummy orion-dummy
zypper --non-interactive up milkyway-dummy

# kill avahi
/usr/sbin/avahi-daemon -k
