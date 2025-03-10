#!/usr/bin/env bash
set -e

chown postgres /etc/pki/tls/private/pg-spacewalk.key

pgdata="/var/lib/postgresql/data"
pgconf="$pgdata/postgresql.conf"

if [ ! -f $pgdata/PG_VERSION ]; then
  if [ ! -f $pgdata; then
    mkdir -p $pgdata
    chown postgres:postgres $pgdata
  fi
  echo "logging_collector = off" > "$pgconf"
  echo "log_destination = 'stderr'" >> "$pgconf"
fi

exec /usr/local/bin/docker-entrypoint.sh "$@"
