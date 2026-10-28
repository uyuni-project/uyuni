#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

set -eo pipefail

TRUST_ANCHORS_DIR="/etc/rhn/ca"
MANAGER_COMPLETE="/var/spacewalk/.MANAGER_SETUP_COMPLETE"
RHN_CONF="/etc/rhn/rhn.conf"

# The database connection settings live in rhn.conf once the server has been
# configured. The setup-time environment variables (MANAGER_USER, MANAGER_PASS,
# ...) are unset by the entrypoint after setup and are not present on restart.
get_rhn_conf() {
    sed -n "s/^\s*${1}\s*=\s*\([^ ]*\)\s*$/\1/p" "${RHN_CONF}"
}

mkdir -p "${TRUST_ANCHORS_DIR}"

# Required on Kubernetes to list the CA in /pub/
PUB_CA="/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT"
if [ -d "$(dirname "${PUB_CA}")" ] && [ ! -e "${PUB_CA}" ] && [ ! -L "${PUB_CA}" ]; then
    ln -s /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT "${PUB_CA}"
fi

if [ -f "${MANAGER_COMPLETE}" ]; then
    /usr/bin/salt-secrets-config.py

    DB_NAME="$(get_rhn_conf db_name)"
    DB_USER="$(get_rhn_conf db_user)"
    DB_PASS="$(get_rhn_conf db_password)"
    DB_HOST="$(get_rhn_conf db_host)"
    DB_PORT="$(get_rhn_conf db_port)"

    run_sql() {
        local DBNAME="${1}"
        shift
        PGPASSWORD="${DB_PASS}" psql -U "${DB_USER}" \
            -h "${DB_HOST}" -p "${DB_PORT}" \
            -d "${DBNAME}" -t -A "${@}" 2>/dev/null
    }

    # Sync peripheral and hub server root CA certificates from the database
    run_sql "${DB_NAME}" \
        -c "SELECT fqdn, root_ca FROM suseISSPeripheral WHERE root_ca IS NOT NULL" \
        | while IFS='|' read -r fqdn root_ca; do
            [ -z "${fqdn}" ] && continue
            printf '%s\n' "${root_ca}" > "${TRUST_ANCHORS_DIR}/peripheral_${fqdn}_root_ca.pem"
        done

    run_sql "${DB_NAME}" \
        -c "SELECT fqdn, root_ca FROM suseISSHub WHERE root_ca IS NOT NULL" \
        | while IFS='|' read -r fqdn root_ca; do
            [ -z "${fqdn}" ] && continue
            printf '%s\n' "${root_ca}" > "${TRUST_ANCHORS_DIR}/hub_${fqdn}_root_ca.pem"
        done

    # Regenerate the trust store to include the dynamically added CAs
    /usr/sbin/update-ca-certificates
fi
