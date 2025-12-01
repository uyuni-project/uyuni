#!/bin/bash
set -e

# 1. Install Dependencies (mgradm, podman, etc.)
echo ">>> Installing Uyuni Tools..."
zypper --non-interactive ref
zypper --non-interactive in git iproute2 hostname

# Add the Container Utils Master Repo
zypper --non-interactive ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/ContainerUtils/openSUSE_Tumbleweed/ uyuni-utils
zypper --non-interactive ref
zypper --non-interactive in mgradm podman

# 2. Configure Network/FQDN
# Uyuni is picky about FQDNs. We set a dummy one mapped to the local IP.
export MY_IP=$(ip route get 1 | awk '{print $7;exit}')
export FQDN="uyuni-ephemeral.local"
echo "$MY_IP $FQDN" >> /etc/hosts
hostnamectl set-hostname $FQDN

# 3. Deploy Uyuni from Master
# We use the --image flag to pull the specific master build
echo ">>> Deploying Uyuni Server (Master)..."

mgradm install podman \
  --image registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server \
  --fqdn $FQDN \
  --admin-password "admin" \
  --email "admin@example.com" \
  --organization "UyuniTest" \
  --force \
  --debug

echo ">>> Deployment Complete!"
echo ">>> Access the UI at https://localhost (Accept the self-signed cert)"
