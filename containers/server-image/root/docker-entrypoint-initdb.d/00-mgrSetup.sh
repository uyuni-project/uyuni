#!/bin/bash

: "${UYUNI_FQDN:=}"
: "${NO_SSL:=N}"
: "${DEBUG_JAVA:=false}"

: "${MANAGER_DB_HOST:=db}"
: "${MANAGER_DB_PORT:=5432}"
: "${MANAGER_DB_NAME:=susemanager}"
: "${MANAGER_DB_CA_CERT:=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT}"
: "${EXTERNALDB_PROVIDER:=}"

: "${REPORT_DB_HOST:=reportdb}"
: "${REPORT_DB_PORT:=5432}"
: "${REPORT_DB_NAME:=reportdb}"
: "${REPORT_DB_CA_CERT:=/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT}"

: "${ORG_NAME:=SUSE Test}"
: "${ADMIN_USER:=admin}"
: "${ADMIN_PASS:=admin}"
: "${ADMIN_FIRST_NAME:=Admin}"
: "${ADMIN_LAST_NAME:=Admin}"
: "${MANAGER_ADMIN_EMAIL:=a@b.com}"
: "${MANAGER_MAIL_FROM:=a@b.com}"

: "${SCC_USER:=}"
: "${SCC_PASS:=}"
: "${ISS_PARENT:=}"

: "${MANAGER_ENABLE_TFTP:=n}"

DEFAULT_RHN_CONF="/usr/share/rhn/config-defaults/rhn.conf"
TMPDIR="/var/spacewalk/tmp"
MANAGER_COMPLETE="/root/.MANAGER_SETUP_COMPLETE"

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

  PGPASSWORD="${PASS}" psql -U "${USER}" -h "${HOST}" -p "${PORT}" -d "${DBNAME}" -v ON_STOP_ERROR=ON "${@}" > /dev/null 2>&1
}

setup_reportdb() {
  if command -v db_schema_exists >/dev/null 2>&1 && db_schema_exists "${REPORT_DB_NAME}"; then
    echo "Clearing the report database"
    for schema in $(echo "SELECT nspname FROM pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE 'information_schema';" | run_sql "${REPORT_DB_NAME}" -t); do
      echo "DROP SCHEMA IF EXISTS ${schema} CASCADE;" | run_sql "${REPORT_DB_NAME}";
    done
  fi

  # Some tools in the setup call spacewalk-sql and require the db to be defined in rhn.conf at an early stage
  cat >>/etc/rhn/rhn.conf <<EOF
report_db_backend=postgresql
report_db_host=${REPORT_DB_HOST}
report_db_port=${REPORT_DB_PORT}
report_db_name=${REPORT_DB_NAME}
report_db_user=${REPORT_DB_USER}
report_db_password=${REPORT_DB_PASS}
report_db_ssl_enabled=1
report_db_sslrootcert=${REPORT_DB_CA_CERT}
EOF

  # Can go away with ISSv1
  cat >>/var/lib/rhn/rhn-satellite-prep/satellite-local-rules.conf <<EOF
report_db_backend=postgresql
report_db_host=${REPORT_DB_HOST}
report_db_port=${REPORT_DB_PORT}
report_db_name=${REPORT_DB_NAME}
report_db_user=${REPORT_DB_USER}
report_db_password=${REPORT_DB_PASS}
report_db_ssl_enabled=1
report_db_sslrootcert=${REPORT_DB_CA_CERT}
EOF

  echo "Populating the report database"
  run_sql "${REPORT_DB_NAME}" </usr/share/susemanager/db/reportdb/main.sql
  echo "Report database set up and populated"
}

setup_db_postgres() {
  if command -v db_schema_exists >/dev/null 2>&1 && db_schema_exists "${MANAGER_DB_NAME}"; then
    echo "Clearing the database"
    for schema in $(echo "SELECT nspname FROM pg_namespace WHERE nspname NOT LIKE 'pg_%' AND nspname NOT LIKE 'information_schema';" | run_sql "${MANAGER_DB_NAME}" -t); do
      echo "DROP SCHEMA IF EXISTS ${schema} CASCADE;" | run_sql "${MANAGER_DB_NAME}";
    done
  fi

  echo "Populating the database"
  PGPASSWORD="${MANAGER_PASS}" PGOPTIONS='--client-min-messages=error -c standard_conforming_strings=on' \
    psql -U "${MANAGER_USER}" -p "${MANAGER_DB_PORT}" -d "${MANAGER_DB_NAME}" -h "${MANAGER_DB_HOST}" -v ON_STOP_ERROR=ON -q -b </usr/share/susemanager/db/postgres/main.sql /dev/null 2>&1

  # Some tools in the setup call spacewalk-sql and require the db to be defined in rhn.conf at an early stage
  cat >>/etc/rhn/rhn.conf <<EOF 2>/dev/null
db_backend=postgresql
db_host=${MANAGER_DB_HOST}
db_port=${MANAGER_DB_PORT}
db_name=${MANAGER_DB_NAME}
db_user=${MANAGER_USER}
db_password=${MANAGER_PASS}
db_ssl_enabled=
EOF

}

setup_spacewalk() {
  # Deploy the SSL certificates
  if [ "${NO_SSL}" = "Y" ]; then
    /usr/bin/spacewalk-setup-httpd --no-ssl
  else
    /usr/bin/spacewalk-setup-httpd
  fi
  /usr/sbin/update-ca-certificates
  /usr/bin/rhn-ssl-dbstore --ca-cert /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT

  if [ ! -f /srv/susemanager/salt/images/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm ]; then
    /usr/sbin/mgr-package-rpm-certificate-osimage
  fi

  echo "admin-email = ${MANAGER_ADMIN_EMAIL}
ssl-config-sslvhost = Y
db-backend=postgresql
db-user=${MANAGER_USER}
db-password=${MANAGER_PASS}
db-name=${MANAGER_DB_NAME}
db-host=${MANAGER_DB_HOST}
db-port=${MANAGER_DB_PORT}
db-ca-cert=${MANAGER_DB_CA_CERT}
report-db-ca-cert=${REPORT_DB_CA_CERT}
externaldb-provider=${EXTERNALDB_PROVIDER}
report-db-backend=postgresql
report-db-name=${REPORT_DB_NAME}
report-db-host=${REPORT_DB_HOST}
report-db-port=${REPORT_DB_PORT}
report-db-user=${REPORT_DB_USER}
report-db-password=${REPORT_DB_PASS}
enable-tftp=${MANAGER_ENABLE_TFTP}
product_name=${PRODUCT_NAME}
hostname=${UYUNI_FQDN}
" > /root/spacewalk-answers

  if [ -n "${SCC_USER}" ]; then
    echo "scc-user = ${SCC_USER}
scc-pass = ${SCC_PASS}
" >> /root/spacewalk-answers
    PARAM_CC="--scc"
  elif [ -n "${ISS_PARENT}" ]; then
    PARAM_CC="--disconnected"
  fi

  if [ "${NO_SSL}" = "Y" ]; then
    echo "no-ssl = Y
" >>/root/spacewalk-answers
    sed '/ssl/Id' -i /etc/apache2/conf.d/zz-spacewalk-www.conf
    echo "server.no_ssl = 1" >>/etc/rhn/rhn.conf
    sed '/<IfDefine SSL/,/<\/IfDefine SSL/d' -i /etc/apache2/listen.conf
  fi

  /usr/bin/spacewalk-setup --clear-db ${PARAM_CC} --answer-file=/root/spacewalk-answers
  SWRET="${?}"
  if [ "x" = "x${MANAGER_MAIL_FROM}" ]; then
    MANAGER_MAIL_FROM="${PRODUCT_NAME} (${UYUNI_FQDN}) <root@${UYUNI_FQDN}>"
  fi
  if ! grep "^web.default_mail_from" /etc/rhn/rhn.conf > /dev/null; then
    echo "web.default_mail_from = ${MANAGER_MAIL_FROM}" >> /etc/rhn/rhn.conf
  fi

  # The CA needs to be added to the database for Kickstart use.
  /usr/bin/rhn-ssl-dbstore --ca-cert /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT

  # rm /root/spacewalk-answers
  if [ "${SWRET}" != "0" ]; then
    echo "ERROR: spacewalk-setup failed" >&2
    exit 1
  fi
}

setup_mirror() {
  # In the container case, we have the MIRROR_PATH environment variable at setup
  if [ -n "${MIRROR_PATH}" ]; then
    echo "server.susemanager.fromdir = ${MIRROR_PATH}" >> /etc/rhn/rhn.conf
  fi
}

setup_iss() {
  if [ -n "${ISS_PARENT}" ]; then
    certname=$(echo "MASTER-${ISS_PARENT}-TRUSTED-SSL-CERT" | sed 's/\./_/g')
    curl -s -S -o "/usr/share/rhn/${certname}" "http://${ISS_PARENT}/pub/RHN-ORG-TRUSTED-SSL-CERT"

    if [ -e "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT" ] && \
       cmp -s "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT" "/usr/share/rhn/${certname}" ; then
      # equal - use it
      rm -f "/usr/share/rhn/${certname}"
      certname=RHN-ORG-TRUSTED-SSL-CERT
    else
      /usr/share/rhn/certs/update-ca-cert-trust.sh "${certname}"
    fi

    echo "
    INSERT INTO rhnISSMaster (id, label, is_current_master, ca_cert)
    VALUES (sequence_nextval('rhn_issmaster_seq'), '${ISS_PARENT}', 'Y', '/usr/share/rhn/${certname}');
    " | spacewalk-sql -
  fi
}

setup_admin_user() {
  if [ -n "${ADMIN_PASS}" ]; then
    echo "starting tomcat..."
    # Start in background
    (su -s /usr/bin/sh -g tomcat -G www -G susemanager tomcat /usr/lib/tomcat/server start) &

    echo "starting apache2..."
    /usr/sbin/start_apache2 -k start

    echo "Creating first user..."

    if [ "${NO_SSL}" = "Y" ]; then
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

      if ! grep -q '^{"success":true' /tmp/curl_out ; then
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
  fi
}

setup_debug() {
  if [ "${DEBUG_JAVA}" = "true" ]; then
    # Note: $JAVA_OPTS inside single quotes is NOT expanded here. 
    # It assumes the target file is a shell script that will source this line later.
    echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8003,server=y,suspend=n" ' >> /etc/tomcat/conf.d/remote_debug.conf
    echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8001,server=y,suspend=n" ' >> /etc/rhn/taskomatic.conf
    echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8002,server=y,suspend=n" ' >> /usr/share/rhn/config-defaults/rhn_search_daemon.conf
  fi
}

setup_mail() {
  # setup mail
  postconf -e "myhostname=${UYUNI_FQDN}"
}

setup_timezone() {
  if [ -n "${TZ}" ]; then
    rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/${TZ}" /etc/localtime
  fi
}

setup_product_name() {
  if [ -f "${DEFAULT_RHN_CONF}" ]; then
    while IFS=" = " read -r name value
    do
      if [ "${name}" = "product_name" ]; then
        PRODUCT_NAME="${value}"
      fi
    done < "${DEFAULT_RHN_CONF}"
  fi

  if [ -z "${PRODUCT_NAME}" ]; then
    PRODUCT_NAME="Uyuni"
  fi
}

check_running_user_permission() {
  if [ ! "$(id -u)" -eq 0 ]; then
    echo "You need to be superuser (root) to run this script!"
    exit 1
  fi
}

check_current_installation() {
  if [ -e "${MANAGER_COMPLETE}" ]; then
    echo "Server appears to be already configured. Installation options may be ignored."
    exit 0
  fi
}

check_current_installation
setup_product_name
check_running_user_permission
setup_timezone
setup_mail
setup_debug
setup_db_postgres
setup_reportdb
setup_spacewalk
setup_mirror
setup_iss
setup_admin_user

exit 0
