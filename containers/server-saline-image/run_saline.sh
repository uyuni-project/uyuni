#!/bin/bash

if test -n "$TZ" -a -f "/usr/share/zoneinfo/$TZ"; then
    ln -sf "/usr/share/zoneinfo/$TZ" /etc/localtime
fi

cp -r /etc/saline.defaults/saline* /etc/salt/

mkdir --parents /run/saline/pki
chown --recursive salt:salt /run/saline
chmod 0750 /run/saline/pki

if [[ "$NOSSL" == "YES" ]]; then
    cat > /etc/salt/saline.d/restapi.conf <<EOL
restapi:
  host: 0.0.0.0
  disable_ssl: true
EOL
else
    cp /etc/pki/tls/certs/spacewalk.crt /run/saline/pki/saline.crt
    cp /etc/pki/tls/private/spacewalk.key /run/saline/pki/saline.key
    chown --recursive salt:salt /run/saline/pki
    cat > /etc/salt/saline.d/restapi.conf <<EOL
restapi:
  host: 0.0.0.0
  ssl_crt: /run/saline/pki/saline.crt
  ssl_key: /run/saline/pki/saline.key
EOL
fi

chown --recursive salt:salt /etc/salt/saline*

exec /usr/bin/salined -l info
