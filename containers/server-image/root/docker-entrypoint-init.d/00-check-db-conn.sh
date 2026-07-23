#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

set -e

# Default database connection parameters if not already set
: "${MANAGER_DB_HOST:=db}"
: "${MANAGER_DB_PORT:=5432}"
: "${MANAGER_USER:=spacewalk}"
: "${MANAGER_DB_NAME:=susemanager}"

: "${REPORT_DB_HOST:=reportdb}"
: "${REPORT_DB_PORT:=5432}"
: "${REPORT_DB_USER:=pythia_susemanager}"
: "${REPORT_DB_NAME:=reportdb}"

# Connection check retry settings
: "${UYUNI_INIT_DB_CONN_RETRIES:=5}"
: "${UYUNI_INIT_DB_CONN_INTERVAL:=1}"

# Function to check database readiness
check_database() {
    local host="$1"
    local port="$2"
    local user="$3"
    local dbname="$4"
    local label="$5"

    echo "Checking connection to ${label} database..."
    echo "  Host: ${host}"
    echo "  Port: ${port}"
    echo "  User: ${user}"
    echo "  Database: ${dbname}"

    local attempt=1
    while [ "${attempt}" -le "${UYUNI_INIT_DB_CONN_RETRIES}" ]; do
        if pg_isready -h "${host}" -p "${port}" -U "${user}" -d "${dbname}" >/dev/null 2>&1; then
            echo "Successfully connected to ${label} database on attempt ${attempt}."
            return 0
        fi
        echo "  [Attempt ${attempt}/${UYUNI_INIT_DB_CONN_RETRIES}] Database is not ready yet. Retrying in ${UYUNI_INIT_DB_CONN_INTERVAL}s..."
        sleep "${UYUNI_INIT_DB_CONN_INTERVAL}"
        attempt=$((attempt + 1))
    done

    echo "Error: Failed to connect to ${label} database after ${UYUNI_INIT_DB_CONN_RETRIES} attempts." >&2
    return 1
}

# Perform readiness checks
check_database "${MANAGER_DB_HOST}" "${MANAGER_DB_PORT}" "${MANAGER_USER}" "${MANAGER_DB_NAME}" "Manager"

# Also check Report DB if they are distinct or configured separately
# Standard podman runner maps both 'db' and 'reportdb' network aliases to the same uyuni-db container.
# If they point to different hosts or if we want to ensure both alias connection works, we check both.
if [ "${MANAGER_DB_HOST}" != "${REPORT_DB_HOST}" ]; then
    check_database "${REPORT_DB_HOST}" "${REPORT_DB_PORT}" "${REPORT_DB_USER}" "${REPORT_DB_NAME}" "Report"
fi
