#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

. /usr/lib/entrypoint-lib.sh

# Abort early as with configured system we might not have mandatory variables that are checked later
check_current_installation

# Mandatory variables
: "${UYUNI_HOSTNAME}"
: "${MANAGER_PASS}"
: "${REPORT_DB_PASS}"
: "${ADMIN_PASS}"

# Optional variables
: "${MANAGER_USER:=spacewalk}"
: "${MANAGER_DB_HOST:=db}"
: "${MANAGER_DB_PORT:=5432}"
: "${MANAGER_DB_NAME:=susemanager}"
: "${MANAGER_DB_CA_CERT:=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT}"
: "${MANAGER_DB_SSL_ENABLED:=}"
: "${EXTERNALDB_PROVIDER:=}"

: "${REPORT_DB_USER:=pythia_susemanager}"
: "${REPORT_DB_HOST:=reportdb}"
: "${REPORT_DB_PORT:=5432}"
: "${REPORT_DB_NAME:=reportdb}"
: "${REPORT_DB_CA_CERT:=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT}"
: "${REPORT_DB_SSL_ENABLED:=}"

: "${ORG_NAME:=SUSE Test}"
: "${ADMIN_USER:=admin}"
: "${ADMIN_FIRST_NAME:=Admin}"
: "${ADMIN_LAST_NAME:=Admin}"
: "${MANAGER_ADMIN_EMAIL:=root@${UYUNI_HOSTNAME}}"
: "${MANAGER_MAIL_FROM:=notify@${UYUNI_HOSTNAME}}"

: "${SCC_USER:=}"
: "${SCC_PASS:=}"
: "${ISS_PARENT:=}"

: "${MANAGER_ENABLE_TFTP:=n}"

DEFAULT_RHN_CONF="/usr/share/rhn/config-defaults/rhn.conf"
TMPDIR="/var/spacewalk/tmp"

run_sql() {
    local DBNAME="${1}"
    shift
    local USER="${MANAGER_USER}"
    local PASS="${MANAGER_PASS}"
    local HOST="${MANAGER_DB_HOST}"
    local PORT="${MANAGER_DB_PORT}"
    if [ "${DBNAME}" = "${REPORT_DB_NAME}" ]; then
        USER="${REPORT_DB_USER}"
        PASS="${REPORT_DB_PASS}"
        HOST="${REPORT_DB_HOST}"
        PORT="${REPORT_DB_PORT}"
    fi

    # If -t (tuples only) is requested to capture stdout in subshells, only suppress stderr.
    # Otherwise, suppress both stdout and stderr to keep container setup logs clean.
    if [[ " ${*} " =~ " -t " ]]; then
        PGPASSWORD="${PASS}" psql -U "${USER}" -h "${HOST}" -p "${PORT}" -d "${DBNAME}" -v ON_STOP_ERROR=ON "${@}" 2> /dev/null
    else
        PGPASSWORD="${PASS}" psql -U "${USER}" -h "${HOST}" -p "${PORT}" -d "${DBNAME}" -v ON_STOP_ERROR=ON "${@}" > /dev/null 2>&1
    fi
}

# Helper to generate high-entropy sha256 secrets
generate_secret() {
    head -c 512 /dev/urandom | sha256sum | cut -d' ' -f1
}

# Helper to write configuration settings to both main and prep rhn.conf
update_rhn_conf() {
    local key="${1}"
    local value="${2}"
    local files=("/etc/rhn/rhn.conf" "/var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf")

    # Escape special characters for sed replacement to prevent syntax issues:
    # 1. Backslashes: \ -> \\
    local escaped_val="${value//\\/\\\\}"
    # 2. Ampersands:  & -> \&
    escaped_val="${escaped_val//&/\\&}"
    # 3. Pipe (our chosen sed delimiter): | -> \|
    escaped_val="${escaped_val//|/\\|}"

    for file in "${files[@]}"; do
        mkdir -p "$(dirname "${file}")"
        touch "${file}"

        if grep -q "^[[:space:]]*${key}[[:space:]]*=" "${file}"; then
            # Replace the existing key line (matching optional spaces around '=')
            sed -i "s|^[[:space:]]*${key}[[:space:]]*=.*|${key} = ${escaped_val}|" "${file}"
        else
            # Append the key-value pair if it doesn't exist
            echo "${key} = ${value}" >> "${file}"
        fi
    done
}

initialize_rhn_conf() {
    local files=("/etc/rhn/rhn.conf" "/var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf")

    for file in "${files[@]}"; do
        if [ ! -s "${file}" ]; then
            echo "Initializing ${file}"
            mkdir -p "$(dirname "${file}")"
            cat > "${file}" << 'EOF'
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

######################################################################
# Uyuni/Spacewalk Main Configuration File
######################################################################

# Traceback Email Address for notifications
traceback_mail =

# Storage Mount Points
mount_point = /var/spacewalk
kickstart_mount_point = /var/spacewalk
repomd_cache_mount_point = /var/cache

# Proxy Settings
# Use proxy FQDN, or FQDN:port
server.satellite.http_proxy =
server.satellite.http_proxy_username =
server.satellite.http_proxy_password =
# no_proxy is a comma-separated list of domains or IP addresses
server.satellite.no_proxy =

# Inter-Server Sync Settings
# Completely disable ISS.
# If set to 1, then no slave will be able to sync from this server
# this option does not affect ability to sync to this server from
# another spacewalk (or hosted).
disable_iss = 0

# Database Configuration
db_backend = postgresql
db_host =
db_port =
db_name =
db_user =
db_password =
db_ssl_enabled =
db_sslrootcert =

# Report Database Configuration
report_db_backend = postgresql
report_db_host =
report_db_port =
report_db_name =
report_db_user =
report_db_password =
report_db_ssl_enabled =
report_db_sslrootcert =

# Localization Settings
server.nls_lang = english.UTF8

# Web / Satellite configuration
web.satellite = 1
web.satellite_install =

# Session & High-Entropy Secrets
web.session_swap_secret_1 =
web.session_swap_secret_2 =
web.session_swap_secret_3 =
web.session_swap_secret_4 =

session_secret_1 =
session_secret_2 =
session_secret_3 =
session_secret_4 =

server.secret_key =

# Security Settings
encrypted_passwords = 1
web.restrict_mail_domains =
pam_auth_service = susemanager

# System Snapshots Enabled
enable_snapshots = 1

# Cobbler Integration
cobbler.host = localhost

# Hostnames
hostname =
java.hostname =

# Mail Configuration
web.default_mail_from =

# TFTP Configuration
enable_tftp =

# Product Information
product_name =

# Extended reposync filters to use the entire NEVRA
server.satellite.reposync_nevra_filter = 0
EOF
        fi
    done

    update_rhn_conf "db_backend" "postgresql"
    update_rhn_conf "db_host" "${MANAGER_DB_HOST}"
    update_rhn_conf "db_port" "${MANAGER_DB_PORT}"
    update_rhn_conf "db_name" "${MANAGER_DB_NAME}"
    update_rhn_conf "db_user" "${MANAGER_USER}"
    update_rhn_conf "db_password" "${MANAGER_PASS}"
    update_rhn_conf "db_ssl_enabled" "${MANAGER_DB_SSL_ENABLED}"
    update_rhn_conf "db_sslrootcert" "${MANAGER_DB_CA_CERT}"

    update_rhn_conf "report_db_backend" "postgresql"
    update_rhn_conf "report_db_host" "${REPORT_DB_HOST}"
    update_rhn_conf "report_db_port" "${REPORT_DB_PORT}"
    update_rhn_conf "report_db_name" "${REPORT_DB_NAME}"
    update_rhn_conf "report_db_user" "${REPORT_DB_USER}"
    update_rhn_conf "report_db_password" "${REPORT_DB_PASS}"
    update_rhn_conf "report_db_ssl_enabled" "${REPORT_DB_SSL_ENABLED}"
    update_rhn_conf "report_db_sslrootcert" "${REPORT_DB_CA_CERT}"

    update_rhn_conf "traceback_mail" "${MANAGER_ADMIN_EMAIL}"
    update_rhn_conf "java.hostname" "${UYUNI_HOSTNAME}"
    update_rhn_conf "hostname" "${UYUNI_HOSTNAME}"
    update_rhn_conf "enable_tftp" "${MANAGER_ENABLE_TFTP}"
    update_rhn_conf "product_name" "${PRODUCT_NAME}"

    update_rhn_conf "mount_point" "/var/spacewalk"
    update_rhn_conf "kickstart_mount_point" "/var/spacewalk"
    update_rhn_conf "repomd_cache_mount_point" "/var/cache"
    update_rhn_conf "server.satellite.http_proxy" ""
    update_rhn_conf "server.satellite.http_proxy_username" ""
    update_rhn_conf "server.satellite.http_proxy_password" ""
    update_rhn_conf "server.satellite.no_proxy" ""
    update_rhn_conf "disable_iss" "0"
    update_rhn_conf "server.nls_lang" "english.UTF8"
    update_rhn_conf "web.satellite" "1"
    update_rhn_conf "web.satellite_install" ""
    update_rhn_conf "encrypted_passwords" "1"
    update_rhn_conf "web.restrict_mail_domains" ""
    update_rhn_conf "enable_snapshots" "1"
    update_rhn_conf "pam_auth_service" "susemanager"
    update_rhn_conf "server.satellite.reposync_nevra_filter" "0"

    # Generate high-entropy secrets
    for i in {1..4}; do
        update_rhn_conf "session_secret_${i}" "$(generate_secret)"
        update_rhn_conf "web.session_swap_secret_${i}" "$(generate_secret)"
    done
    update_rhn_conf "server.secret_key" "$(generate_secret)"

    # Cobbler host configuration
    update_rhn_conf "cobbler.host" "localhost"

    # Container-specific OCI SSL override
    if [ "${container:="unknown"}" = "oci" ]; then
        update_rhn_conf "server.no_ssl" "1"
    fi

    # Mail From
    if [ -z "${MANAGER_MAIL_FROM}" ]; then
        MANAGER_MAIL_FROM="${PRODUCT_NAME} (${UYUNI_HOSTNAME}) <root@${UYUNI_HOSTNAME}>"
    fi
    update_rhn_conf "web.default_mail_from" "${MANAGER_MAIL_FROM}"

    # Also update report db configuration in satellite-local-rules.conf (legacy fallback)
    mkdir -p /var/lib/rhn/rhn-satellite-prep
    cat > /var/lib/rhn/rhn-satellite-prep/satellite-local-rules.conf << EOF
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

report_db_backend=postgresql
report_db_host=${REPORT_DB_HOST}
report_db_port=${REPORT_DB_PORT}
report_db_name=${REPORT_DB_NAME}
report_db_user=${REPORT_DB_USER}
report_db_password=${REPORT_DB_PASS}
report_db_ssl_enabled=${REPORT_DB_SSL_ENABLED}
report_db_sslrootcert=${REPORT_DB_CA_CERT}
EOF
}

setup_reportdb() {
    if command -v db_schema_exists > /dev/null 2>&1 && db_schema_exists "${REPORT_DB_NAME}"; then
        echo "Clearing the report database"
        for schema in $(echo "SELECT nspname FROM pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE 'information_schema';" | run_sql "${REPORT_DB_NAME}" -t); do
            echo "DROP SCHEMA IF EXISTS ${schema} CASCADE;" | run_sql "${REPORT_DB_NAME}"
        done
    fi

    echo "Populating the report database"
    run_sql "${REPORT_DB_NAME}" < /usr/share/susemanager/db/reportdb/main.sql
    echo "Report database set up and populated"
}

setup_db_postgres() {
    if command -v db_schema_exists > /dev/null 2>&1 && db_schema_exists "${MANAGER_DB_NAME}"; then
        echo "Clearing the database"
        for schema in $(echo "SELECT nspname FROM pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE 'information_schema';" | run_sql "${MANAGER_DB_NAME}" -t); do
            echo "DROP SCHEMA IF EXISTS ${schema} CASCADE;" | run_sql "${MANAGER_DB_NAME}"
        done
    fi

    echo "Populating the database"
    PGPASSWORD="${MANAGER_PASS}" PGOPTIONS='--client-min-messages=error -c standard_conforming_strings=on' \
        psql -U "${MANAGER_USER}" -p "${MANAGER_DB_PORT}" -d "${MANAGER_DB_NAME}" -h "${MANAGER_DB_HOST}" -v ON_STOP_ERROR=ON -q -b < /usr/share/susemanager/db/postgres/main.sql > /dev/null 2>&1
}

setup_spacewalk() {
    # Deploy the SSL certificates
    local no_ssl=""
    if [ "${container:="unknown"}" = "oci" ]; then
        /usr/bin/spacewalk-setup-httpd --no-ssl
        no_ssl="y"
    else
        /usr/bin/spacewalk-setup-httpd
    fi
    /usr/sbin/update-ca-certificates

    # Validate hostname is lowercase
    if [ "${UYUNI_HOSTNAME}" != "$(echo "${UYUNI_HOSTNAME}" | tr '[:upper:]' '[:lower:]')" ]; then
        echo "ERROR: Hostname '${UYUNI_HOSTNAME}' contains uppercase letters." >&2
        echo "It can cause Proxy communications to fail." >&2
        exit 4
    fi

    echo "Configuring Spacewalk..."

    # Set up organization credentials if scc is requested
    if [ -n "${SCC_USER:-}" ] && [ -n "${SCC_PASS:-}" ]; then
        echo "Setting up SUSE Customer Center credentials..."
        local scc_pass_enc
        scc_pass_enc=$(echo -n "${SCC_PASS}" | base64 | tr -d '\n')
        local scc_url="https://scc.suse.com"
        if [ -f /etc/susemanager.conf ]; then
            local url_val
            url_val=$(grep "^scc_url" /etc/susemanager.conf | cut -d'=' -f2 | xargs)
            if [ -n "${url_val}" ]; then
                scc_url="${url_val}"
            fi
        fi

        # Insert credentials via database
        local insert_query="INSERT INTO suseCredentials (id, user_id, type, username, password, url) VALUES (sequence_nextval('suse_credentials_id_seq'), NULL, 'scc', '${SCC_USER}', '${scc_pass_enc}', '${scc_url}');"
        echo "${insert_query}" | run_sql "${MANAGER_DB_NAME}"
    fi

    # Update template hostname in database
    local chk_query="SELECT value FROM rhnTemplateString WHERE label = 'hostname';"
    local chk_val
    chk_val=$(echo "${chk_query}" | run_sql "${MANAGER_DB_NAME}" -t || true)
    if [ -z "${chk_val}" ]; then
        local ins_query="INSERT INTO rhnTemplateString (id, category_id, label, value, description) VALUES (sequence_nextval('rhn_template_str_id_seq'), (SELECT id FROM rhnTemplateCategory WHERE label = 'org_strings'), 'hostname', '${UYUNI_HOSTNAME}', 'Host name for the Red Hat Satellite');"
        echo "${ins_query}" | run_sql "${MANAGER_DB_NAME}"
    fi

    # Configure Cobbler
    /usr/bin/spacewalk-setup-cobbler --apache2-config-directory "/etc/apache2/conf.d" -f "${UYUNI_HOSTNAME}"

    # Check if cobblerd is running
    if pgrep -f cobblerd > /dev/null; then
        cobbler mkloaders
        cobbler sync
    fi

    if [ "${no_ssl}" = "y" ]; then
        sed '/ssl/Id' -i /etc/apache2/conf.d/zz-spacewalk-www.conf
        sed '/<IfDefine SSL/,/<\/IfDefine SSL/d' -i /etc/apache2/listen.conf
    fi

    # Enable Spacewalk services if command is available
    if [ -x /usr/sbin/spacewalk-service ]; then
        /usr/sbin/spacewalk-service --level 35 enable || echo "Warning: spacewalk-service enable failed"
    fi

    # The CA needs to be added to the database for Kickstart use.
    /usr/bin/rhn-ssl-dbstore --ca-cert /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT
}

setup_admin_user() {
    if [ -n "${ADMIN_PASS}" ]; then
        if [ -f /usr/libexec/tomcat/server ]; then
            TOMCAT_INIT=/usr/libexec/tomcat/server
        elif [ -f /usr/lib/tomcat/server ]; then
            TOMCAT_INIT=/usr/lib/tomcat/server
        else
            echo "Error! Cannot find tomcat init command"
            exit 1
        fi
        echo "starting tomcat..."
        # Start in background
        (su -s /usr/bin/sh -g tomcat -G www -G susemanager tomcat ${TOMCAT_INIT} start) &

        echo "starting apache2..."
        /usr/sbin/start_apache2 -k start

        echo "Creating first user..."

        if [ "${container}" = "oci" ]; then
            CURL_SCHEME="http"
        else
            CURL_SCHEME="-L -k https"
        fi

        echo "Waiting for Tomcat..."
        curl -o /tmp/curl-retry -s --retry 7 ${CURL_SCHEME}://localhost/rhn/newlogin/CreateFirstUser.do

        HTTP_CODE=$(curl -o /dev/null -s -w '%{http_code}' ${CURL_SCHEME}://localhost/rhn/newlogin/CreateFirstUser.do)

        if [ "${HTTP_CODE}" = "200" ]; then
            echo "Creating administration user"

            curl -s -o /tmp/curl_out \
                --data-urlencode "orgName=${ORG_NAME}" \
                --data-urlencode "adminLogin=${ADMIN_USER}" \
                --data-urlencode "adminPassword=${ADMIN_PASS}" \
                --data-urlencode "firstName=${ADMIN_FIRST_NAME}" \
                --data-urlencode "lastName=${ADMIN_LAST_NAME}" \
                --data-urlencode "email=${MANAGER_ADMIN_EMAIL}" \
                ${CURL_SCHEME}://localhost/rhn/manager/api/org/createFirst

            if ! grep -q '^{"success":true' /tmp/curl_out; then
                echo "Failed to create the administration user"
                cat /tmp/curl_out
            fi
            rm -f /tmp/curl_out
        elif [ "${HTTP_CODE}" = "403" ]; then
            echo "Administration user already exists, reusing"
        else
            # Fail if we can't connect properly
            echo "Error contacting Tomcat: HTTP ${HTTP_CODE}"
            exit 1
        fi
        echo "Admin creation complete"

        /usr/sbin/start_apache2 -k stop
        su -s /usr/bin/sh -g tomcat -G www -G susemanager tomcat ${TOMCAT_INIT} stop
    fi
}

setup_product_name() {
    if [ -f "${DEFAULT_RHN_CONF}" ]; then
        while IFS=" = " read -r name value; do
            if [ "${name}" = "product_name" ]; then
                PRODUCT_NAME="${value}"
            fi
        done < "${DEFAULT_RHN_CONF}"
    fi

    if [ -z "${PRODUCT_NAME:-}" ]; then
        PRODUCT_NAME="Uyuni"
    fi
}

setup_product_name
initialize_rhn_conf
setup_db_postgres
setup_reportdb
setup_spacewalk
setup_admin_user
mark_installation_complete
