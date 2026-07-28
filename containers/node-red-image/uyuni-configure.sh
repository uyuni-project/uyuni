#!/bin/sh
set -eu

SETTINGS_FILE=/data/settings.js
FLOWS_FILE=/data/flows.json

if [ -z "${NODERED_CREDENTIAL_SECRET:-}" ]; then
    echo "ERROR: NODERED_CREDENTIAL_SECRET must be set." >&2
    exit 1
fi

# Generate settings.js from template
sed \
    -e "s|@@CREDENTIAL_SECRET@@|${NODERED_CREDENTIAL_SECRET}|g" \
    -e "s|@@MQTT_BROKER_HOST@@|${MQTT_BROKER_HOST:-mosquitto}|g" \
    -e "s|@@MQTT_BROKER_PORT@@|${MQTT_BROKER_PORT:-1883}|g" \
    -e "s|@@UYUNI_SERVER_URL@@|${UYUNI_SERVER_URL:-https://uyuni-server}|g" \
    /opt/settings.js.template > "$SETTINGS_FILE"

# Create default empty flows file if it does not exist
if [ ! -f "$FLOWS_FILE" ]; then
    echo "[]" > "$FLOWS_FILE"
fi

exec node-red --userDir /data --settings "$SETTINGS_FILE"
