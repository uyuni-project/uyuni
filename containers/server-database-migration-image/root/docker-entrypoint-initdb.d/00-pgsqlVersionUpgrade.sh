#!/bin/bash
set -e

echo "PostgreSQL version upgrade"
NEW_VERSION=18

PGSQL_DATA_SRC="/migration/source/"
PGSQL_DATA_DST="/migration/target/"

# Ensure the source directory actually exists
if [ ! -f "${PGSQL_DATA_SRC}/PG_VERSION" ]; then
  echo "Error: Cannot find PG_VERSION in ${PGSQL_DATA_SRC}. Check the volume used by your current pgsql."
  exit 1
fi

OLD_VERSION=$(cat "${PGSQL_DATA_SRC}/PG_VERSION")

echo "Detected Old Version: ${OLD_VERSION}"
echo "Target New Version: ${NEW_VERSION}"

# Check if binaries exist
OLD_BIN="/usr/lib/postgresql${OLD_VERSION}/bin"
NEW_BIN="/usr/lib/postgresql${NEW_VERSION}/bin"

if [ ! -d "${OLD_BIN}" ] || [ ! -d "${NEW_BIN}" ]; then
  echo "Error: Could not find postgres binaries at ${OLD_BIN} or ${NEW_BIN}"
  exit 1
fi

if [ -e /etc/pki/tls/private/pg-spacewalk.key ]; then
  echo "Enforce key permission"
  chown postgres:postgres /etc/pki/tls/private/pg-spacewalk.key
  chown postgres:postgres /etc/pki/tls/certs/spacewalk.crt
fi

# --- SETUP ---
echo "Initialize new postgresql ${NEW_VERSION} database..."

# Load SUSE postgres environment if available
if [ -f /etc/sysconfig/postgresql ]; then
  . /etc/sysconfig/postgresql
fi

# Determine Locale
if [ -z "${POSTGRES_LANG}" ]; then
  POSTGRES_LANG="en_US.UTF-8"
  [ -n "${LC_CTYPE}" ] && POSTGRES_LANG="${LC_CTYPE}"
fi


echo "Remove content of the destination folder."
rm -rf "${PGSQL_DATA_DST:?}"/*
# --- INITIALIZATION ---
echo "Running initdb using postgres user..."

chown -R postgres:postgres "${PGSQL_DATA_DST}"

su -s /bin/bash - postgres -c "initdb -D ${PGSQL_DATA_DST} --locale=${POSTGRES_LANG}"
su -s /bin/bash - postgres -c "pg_checksums --disable --pgdata ${PGSQL_DATA_DST}"

echo "Successfully initialized new postgresql ${NEW_VERSION} database."

# --- UPGRADE ---

echo "Creating temporary pg_hba.conf for upgrade..."
TEMP_HBA="/tmp/pg_hba_upgrade.conf"

cat > "$TEMP_HBA" <<EOF
local   all             all                                     trust
EOF

echo "Running pg_upgrade..."

su -s /bin/bash - postgres -c "pg_upgrade \
  --old-bindir=${OLD_BIN} \
  --new-bindir=${NEW_BIN} \
  --old-datadir=${PGSQL_DATA_SRC} \
  --new-datadir=${PGSQL_DATA_DST} \
  -o '-c hba_file=${TEMP_HBA}'"

# --- CHECKSUMS ---
echo "Enabling checksums on the NEW data directory..."
su -s /bin/bash - postgres -c "pg_checksums --enable --pgdata ${PGSQL_DATA_DST}"

# --- CONFIGURATION ---
echo "Migrating Configuration..."

cp "${PGSQL_DATA_SRC}/pg_hba.conf" "${PGSQL_DATA_DST}/pg_hba.conf"

cp "${PGSQL_DATA_SRC}/postgresql.conf" "${PGSQL_DATA_DST}/postgresql.conf"
if [ -e "${PGSQL_DATA_SRC}/postgresql.conf.d" ]; then
  cp -a "${PGSQL_DATA_SRC}/postgresql.conf.d" "${PGSQL_DATA_DST}/"
fi

echo "Check for deprecated parameters or paths that need updating."

echo "Upgrade Complete."
exit 0
