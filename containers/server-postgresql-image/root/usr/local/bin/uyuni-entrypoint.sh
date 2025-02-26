#!/usr/bin/env bash
set -e

chown postgres /etc/pki/tls/private/pg-spacewalk.key

/usr/local/bin/docker-entrypoint.sh "$@"
