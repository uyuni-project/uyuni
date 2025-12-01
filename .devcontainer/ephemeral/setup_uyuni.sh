#!/bin/bash
set -e

echo ">>> Installing Podman and mgradm..."
sudo zypper --non-interactive install podman
# Add the Master Container Utils repo
sudo zypper --non-interactive ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/ContainerUtils/openSUSE_Tumbleweed/ uyuni-utils
sudo zypper --non-interactive ref
sudo zypper --non-interactive install mgradm

echo ">>> Deploying Uyuni Server from Master..."
# We use a dummy FQDN because Codespaces URLs are dynamic and complex
export FQDN="uyuni.local"

# Install the server container
# We disable SSL verification because we are in a dev container environment
sudo mgradm install podman \
  --image registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server \
  --fqdn $FQDN \
  --admin-password "admin" \
  --email "admin@example.com" \
  --organization "UyuniTest" \
  --force

echo ">>> Done! Uyuni is running."
echo ">>> Access it via the PORTS tab in VS Code."
