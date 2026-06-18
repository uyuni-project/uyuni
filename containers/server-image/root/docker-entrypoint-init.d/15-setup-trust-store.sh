#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

TRUST_ANCHORS_DIR="/etc/rhn/ca"
MANAGER_COMPLETE="/var/spacewalk/.MANAGER_SETUP_COMPLETE"

mkdir -p "${TRUST_ANCHORS_DIR}"

if [ -f "${MANAGER_COMPLETE}" ]; then
    /usr/bin/salt-secrets-config.py

    # Copy uyuni CA to salt cert path and public download path (on PVCs)
    if [ -f "/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT" ]; then
        cp /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
           /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
        cp /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
           /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT
    fi

    # Regenerate the trust store to include the dynamically added CAs
    /usr/sbin/update-ca-certificates
fi
