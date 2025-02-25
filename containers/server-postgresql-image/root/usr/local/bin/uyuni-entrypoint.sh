#!/usr/bin/env bash
set -e

chown postgres /etc/pki/tls/private/pg-spacewalk.key

exec /usr/local/bin/docker-entrypoint.sh "$@"
