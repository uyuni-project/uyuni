#!/bin/bash
set -e

# temporarily disable non-working repo
zypper mr --disable Fake-RPM-SUSE-Channel || :
zypper --non-interactive --gpg-auto-import-keys ref

# install, configure, and start avahi
zypper --non-interactive in avahi
cp /root/avahi-daemon.conf /etc/avahi/avahi-daemon.conf
/usr/sbin/avahi-daemon -D

# install the packages salt-thin needs inside the container
zypper --non-interactive in tar gzip python3
# psutil is called python3-psutil on SLE 15, but python313-psutil on Leap 16 and SLE 16
python_abi=$(python3 -c 'import sys; print("%d%d" % sys.version_info[:2])')
psutil_package="python${python_abi}-psutil"
zypper --non-interactive search --match-exact "${psutil_package}" | grep -q "${psutil_package}" || psutil_package="python3-psutil"
zypper --non-interactive in "${psutil_package}"

# re-enable normal repo and remove helper repo
zypper mr --enable Fake-RPM-SUSE-Channel || :
zypper rr sles15sp4

# do the real test
zypper --non-interactive --gpg-auto-import-keys ref
zypper --non-interactive in hoag-dummy orion-dummy
zypper --non-interactive up milkyway-dummy

# kill avahi
/usr/sbin/avahi-daemon -k
