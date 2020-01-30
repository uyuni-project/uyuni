#!/bin/bash

test -s /etc/rc.status && . /etc/rc.status && rc_reset

if ! declare -f Echo > /dev/null ; then
  Echo() {
    echo "$@"
  }
fi


function register() {
    local mycert_file="RHN-ORG-TRUSTED-SSL-CERT"
    local mycert="/usr/share/rhn/$mycert_file"
    if ! /usr/bin/wget http://$SW_HOSTNAME/pub/$mycert_file -O $mycert; then
        return 1
    fi
    for f in /etc/sysconfig/rhn/*; do
        if [ -f "$f" ]; then
	    sed -i "s/RHNS-CA-CERT/$mycert_file/g" "$f"
        fi
    done
    if ! /usr/sbin/rhnreg_ks --serverUrl=https://$SW_HOSTNAME/XMLRPC --activationkey=$ACTIVATION_KEY --force; then
        return 1
    fi
    return 0
}

# fix hostname for postfix
REALHOSTNAME=`hostname -f`
if [ -z "$REALHOSTNAME" ]; then
	for i in `ip -f inet -o addr show scope global | awk '{print $4}' | awk -F \/ '{print $1}'`; do
		for j in `dig +noall +answer +time=2 +tries=1 -x $i | awk '{print $5}' | sed 's/\.$//'`; do
			if [ -n "$j" ]; then
				REALHOSTNAME=$j
				break 2
			fi
		done
	done
fi
if [ -n "$REALHOSTNAME" ]; then
	echo "$REALHOSTNAME" > /etc/hostname
fi

rm -f /etc/machine-id
mkdir -p /var/lib/dbus
rm -f /var/lib/dbus/machine-id
dbus-uuidgen --ensure
systemd-machine-id-setup

ACTIVATION_KEY=$(rc_cmdline spacewalk_activationkey)
ACTIVATION_KEY=${ACTIVATION_KEY#spacewalk_activationkey=}
SW_HOSTNAME=$(rc_cmdline spacewalk_hostname)
SW_HOSTNAME=${SW_HOSTNAME#spacewalk_hostname=}

READY=1
if [ -z "$ACTIVATION_KEY" ]; then
    Echo "Activation Key missing"
    sleep 10
    READY=0
fi
if [ -z "$SW_HOSTNAME" ]; then
    Echo "Spacewalk Hostname is missing"
    sleep 10
    READY=0
fi

if [ $READY -eq 1 ]; then
    if ! register; then
        sleep 10
    else
        Echo "Successfully registered!"
	sleep 2
    fi
fi

FINALLY=$(rc_cmdline spacewalk_finally)
FINALLY=${FINALLY#spacewalk_finally=}

case "$FINALLY" in
    running) Echo "keep instance running";;
    *) reboot --halt -p -f;;
esac

rc_status -v
rc_exit

