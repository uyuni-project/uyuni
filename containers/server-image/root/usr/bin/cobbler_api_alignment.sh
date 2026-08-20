#!/bin/bash

CONFIG_FILE="/etc/apache2/conf.d/cobbler.conf"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Configuration file $CONFIG_FILE not found."
    exit 1
fi

# Check if the Location block for /cobbler_api already exists to ensure idempotency
if grep -q -E '<Location\s+"?/cobbler_api"?\s*>' "$CONFIG_FILE"; then
    # Cobbler API access is already restricted to localhost.
    exit 0
fi

cp "$CONFIG_FILE" "${CONFIG_FILE}.bak"

# We need to limit access to cobbler_api just to a localhost, so let's remove the unrestricted rule
sed -i '/ProxyPass.*\/cobbler_api/d' "$CONFIG_FILE"

# and add location restricted.
cat << 'EOF' >> "$CONFIG_FILE"

<Location "/cobbler_api">
    Require ip 127.0.0.1 ::1
    ProxyPass http://127.0.0.1:25151/
    ProxyPassReverse http://127.0.0.1:25151/
</Location>
EOF

# Above is assuming cobbler conf is not wrapped in VirtualHost, etc, which is true for 5.1 and 5.2 as we remove it in
# spacewalk/setup/bin/spacewalk-setup-cobbler