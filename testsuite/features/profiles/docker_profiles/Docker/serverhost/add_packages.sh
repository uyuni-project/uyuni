#!/bin/bash
set -e

# temporarily disable non-working repo
zypper mr --disable Fake-RPM-SUSE-Channel || :
zypper --non-interactive --gpg-auto-import-keys ref

# install, configure, and start avahi
zypper --non-interactive in avahi
cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

# install python3 and python3-psutil in the container
zypper --non-interactive in tar gzip python3 python3-psutil

# re-enable normal repo and remove helper repo
zypper mr --enable Fake-RPM-SUSE-Channel || :
zypper rr sles15sp7

# do the real test
zypper --non-interactive --gpg-auto-import-keys ref
zypper --non-interactive in hoag-dummy orion-dummy
zypper --non-interactive up milkyway-dummy

# kill avahi
/usr/sbin/avahi-daemon -k
