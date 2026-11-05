#!/bin/sh
set -eu

PASSWD_FILE=/mosquitto/config/passwd

if [ -z "${MQTT_PUBLISHER_PASSWORD:-}" ] || [ -z "${MQTT_SUBSCRIBER_PASSWORD:-}" ]; then
    echo "ERROR: MQTT_PUBLISHER_PASSWORD and MQTT_SUBSCRIBER_PASSWORD must both be set." >&2
    echo "       This broker does not accept anonymous clients." >&2
    exit 1
fi

rm -f "$PASSWD_FILE"
mosquitto_passwd -c -b "$PASSWD_FILE" uyuni-publisher "$MQTT_PUBLISHER_PASSWORD"
mosquitto_passwd -b "$PASSWD_FILE" uyuni-subscriber "$MQTT_SUBSCRIBER_PASSWORD"
chmod 600 "$PASSWD_FILE"
