#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

BACKUP_DIR="/tmp/testing/snapshots"
mkdir -p $BACKUP_DIR

$PODMAN_CMD exec uyuni-db pg_dump -U postgres susemanager > $BACKUP_DIR/db_dump.sql
$PODMAN_CMD exec server tar -czf - /root/ssl-build > $BACKUP_DIR/ssl_backup.tar.gz
$PODMAN_CMD exec server tar -czf - /etc/spacewalk /etc/rhn /var/spacewalk > $BACKUP_DIR/server_configs.tar.gz

echo "--- Backup completed in $BACKUP_DIR ---"
