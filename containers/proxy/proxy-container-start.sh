#!/bin/bash
set -x

if [ ! -d /config ]; then
	echo "Config directory '/config' not found"
	exit 1
fi

if [ -z $UYUNI_MASTER ]; then
	echo "Unknown master. Please set UYUNI_MASTER as environment variable"
	exit 1
fi
if [ -z $UYUNI_PROXY_MINION_ID ]; then
	$UYUNI_PROXY_MINION_ID=$HOSTNAME
fi
if [ -n $UYUNI_MACHINE_ID ]; then
	echo "$UYUNI_MACHINE_ID" > /etc/machine-id
elif [ ! -s /etc/machine-id ]; then
	systemd-machine-id-setup
fi
MINION_ID_FILE="/etc/salt/minion_id"
SUSEMANAGER_MASTER_FILE=/etc/salt/minion.d/susemanager.conf
UP2DATE_FILE=/etc/sysconfig/rhn/up2date
SYSTEMID_PATH=$(awk -F '=[[:space:]]*' '/^[[:space:]]*systemIdPath[[:space:]]*=/ {print $2}' $UP2DATE_FILE)

echo "$UYUNI_PROXY_MINION_ID" > "$MINION_ID_FILE"

echo "master: $UYUNI_MASTER" > "$SUSEMANAGER_MASTER_FILE"
echo "log_level: debug" >> "$SUSEMANAGER_MASTER_FILE"
echo "server_id_use_crc: adler32" >> "$SUSEMANAGER_MASTER_FILE"
echo "enable_legacy_startup_events: False" >> "$SUSEMANAGER_MASTER_FILE"
echo "enable_fqdns_grains: False" >> "$SUSEMANAGER_MASTER_FILE"
echo "start_event_grains: [machine_id, saltboot_initrd, susemanager]" >> "$SUSEMANAGER_MASTER_FILE"
echo "mine_enabled: False" >> "$SUSEMANAGER_MASTER_FILE"

if [ -n "$UYUNI_ACTIVATION_KEY" ] ; then
	cat <<EOF >>"$SUSEMANAGER_MASTER_FILE"
grains:
    susemanager:
        activation_key: "$(echo $ACTIVATION_KEYS | cut -d, -f1)"
EOF
fi

/usr/bin/salt-minion -d
if [ $? -ne 0 ]; then
	echo "salt-minion not running"
	exit 1
fi

/bin/bash
