#!/bin/bash
set -e

# 1. Install Dependencies
echo ">>> Installing Uyuni Master Tools..."
sudo zypper --non-interactive ref
# Install basic tools and Podman (if the feature missed anything specific for SUSE)
sudo zypper --non-interactive in git iproute2 hostname podman

# Add the Uyuni Master Container Utils repo
sudo zypper --non-interactive ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/ContainerUtils/openSUSE_Tumbleweed/ uyuni-utils
sudo zypper --non-interactive ref
sudo zypper --non-interactive in mgradm

# 2. Configure Fake FQDN
# Uyuni requires a valid FQDN. We map one to the local loopback.
export FQDN="uyuni-ephemeral.local"
echo "127.0.0.1 $FQDN" | sudo tee -a /etc/hosts
sudo hostnamectl set-hostname $FQDN

# 3. Deploy Uyuni Server
echo ">>> Deploying Uyuni Server from Master via Podman..."

# Note: mgradm tries to enable a systemd service. 
# In a devcontainer, systemd might not be PID 1.
# We use 'mgradm install' but if it fails on systemd, the container usually still runs.
# We explicitly set the backend to podman.

sudo mgradm install podman \
  --image registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server \
  --fqdn $FQDN \
  --admin-password "admin" \
  --email "admin@example.com" \
  --organization "UyuniTest" \
  --force \
  --debug

echo ">>> Deployment script finished."
echo ">>> If the container is running, access https://localhost (accept self-signed cert)."
