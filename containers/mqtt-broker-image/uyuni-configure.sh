#!/bin/sh
set -eu

PASSWD_FILE=/mosquitto/config/passwd
LISTENERS_FILE=/mosquitto/config/conf.d/listeners.conf
CERT_DIR=${MQTT_TLS_CERT_DIR:-/mosquitto/certs}

if [ -z "${MQTT_PUBLISHER_PASSWORD:-}" ] || [ -z "${MQTT_SUBSCRIBER_PASSWORD:-}" ]; then
    echo "ERROR: MQTT_PUBLISHER_PASSWORD and MQTT_SUBSCRIBER_PASSWORD must both be set." >&2
    echo "       This broker does not accept anonymous clients." >&2
    exit 1
fi

rm -f "$PASSWD_FILE"
mosquitto_passwd -c -b "$PASSWD_FILE" uyuni-publisher "$MQTT_PUBLISHER_PASSWORD"
mosquitto_passwd -b "$PASSWD_FILE" uyuni-subscriber "$MQTT_SUBSCRIBER_PASSWORD"
chmod 600 "$PASSWD_FILE"

# Listeners. The plain listener stays on unless it is switched off, so existing
# set-ups keep working. TLS is turned on by mounting a certificate and key.
mkdir -p "$(dirname "$LISTENERS_FILE")"
: > "$LISTENERS_FILE"

TLS=false
if [ -f "$CERT_DIR/server.crt" ] && [ -f "$CERT_DIR/server.key" ]; then
    TLS=true
elif [ -f "$CERT_DIR/server.crt" ] || [ -f "$CERT_DIR/server.key" ]; then
    echo "ERROR: $CERT_DIR must contain both server.crt and server.key to enable TLS." >&2
    exit 1
fi

if [ "${MQTT_PLAIN_LISTENER:-true}" = "true" ]; then
    echo "listener 1883" >> "$LISTENERS_FILE"
elif [ "$TLS" = "false" ]; then
    echo "ERROR: MQTT_PLAIN_LISTENER=false but no TLS certificate was found in $CERT_DIR," >&2
    echo "       so the broker would have no listener at all." >&2
    exit 1
fi

if [ "$TLS" = "true" ]; then
    {
        echo "listener 8883"
        echo "certfile $CERT_DIR/server.crt"
        echo "keyfile $CERT_DIR/server.key"
        echo "tls_version tlsv1.2"
    } >> "$LISTENERS_FILE"

    if [ "${MQTT_REQUIRE_CLIENT_CERT:-false}" = "true" ]; then
        if [ ! -f "$CERT_DIR/ca.crt" ]; then
            echo "ERROR: MQTT_REQUIRE_CLIENT_CERT=true needs the CA that signs client" >&2
            echo "       certificates in $CERT_DIR/ca.crt." >&2
            exit 1
        fi
        {
            echo "cafile $CERT_DIR/ca.crt"
            echo "require_certificate true"
        } >> "$LISTENERS_FILE"
    fi
fi

echo "Listeners:"
sed 's/^/    /' "$LISTENERS_FILE"
