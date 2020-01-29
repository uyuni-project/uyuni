#!/bin/bash

if ! declare -f Echo > /dev/null ; then
  Echo() {
    echo "$@"
  }
fi

if ! [ -s /etc/resolv.conf ] ; then
    Echo "No network, skipping saltboot..."
    exit 0
fi

IFCONFIG="$(compgen -G '/tmp/leaseinfo.*.dhcp.ipv*')"
if [ -f "$IFCONFIG" ]; then
    . "$IFCONFIG"
else
    Echo "No network details available, skipping saltboot";
    exit 0
fi

NEWROOT=${NEWROOT:-/mnt}
export NEWROOT

if [ -e /usr/bin/plymouth ] ; then
    mkfifo /progress
    bash -c 'while true ; do read msg < /progress ; plymouth message --text="$msg" ; done ' &
    PROGRESS_PID=$!
else
    mkfifo /progress
    bash -c 'while true ; do read msg < /progress ; echo -n -e "\033[2K$msg\015" >/dev/console ; done ' &
    PROGRESS_PID=$!
fi

rm /etc/machine-id
mkdir -p /var/lib/dbus
rm /var/lib/dbus/machine-id
dbus-uuidgen --ensure
systemd-machine-id-setup

DIG_OPTIONS="+short"
if dig -h | grep -q '\[no\]cookie'; then
    DIG_OPTIONS="+nocookie +short"
fi

[ -n "$PROGRESS_PID" ] && kill $PROGRESS_PID

/usr/sbin/preregister

