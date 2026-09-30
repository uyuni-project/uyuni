#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

set -eo pipefail

TRUST_ANCHORS_DIR="/etc/rhn/ca"
MANAGER_COMPLETE="/var/spacewalk/.MANAGER_SETUP_COMPLETE"

mkdir -p "${TRUST_ANCHORS_DIR}"

if [ -f "${MANAGER_COMPLETE}" ]; then
    /usr/bin/salt-secrets-config.py

    run_sql() {
        local DBNAME="${1}"
        shift
        PGPASSWORD="${MANAGER_PASS}" psql -U "${MANAGER_USER}" \
            -h "${MANAGER_DB_HOST:-db}" -p "${MANAGER_DB_PORT:-5432}" \
            -d "${DBNAME}" -t -A "${@}" 2>/dev/null
    }

    # Sync peripheral and hub server root CA certificates from the database
    run_sql "${MANAGER_DB_NAME:-susemanager}" \
        -c "SELECT fqdn, root_ca FROM suseISSPeripheral WHERE root_ca IS NOT NULL" \
        | while IFS='|' read -r fqdn root_ca; do
            [ -z "${fqdn}" ] && continue
            printf '%s\n' "${root_ca}" > "${TRUST_ANCHORS_DIR}/peripheral_${fqdn}_root_ca.pem"
        done

    run_sql "${MANAGER_DB_NAME:-susemanager}" \
        -c "SELECT fqdn, root_ca FROM suseISSHub WHERE root_ca IS NOT NULL" \
        | while IFS='|' read -r fqdn root_ca; do
            [ -z "${fqdn}" ] && continue
            printf '%s\n' "${root_ca}" > "${TRUST_ANCHORS_DIR}/hub_${fqdn}_root_ca.pem"
        done

    # Copy uyuni CA to salt cert path (the HTTP pub path is served via Apache Alias)
    if [ -f "/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT" ]; then
        cp /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
           /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
    fi

    # Regenerate the trust store to include the dynamically added CAs
    /usr/sbin/update-ca-certificates
fi
