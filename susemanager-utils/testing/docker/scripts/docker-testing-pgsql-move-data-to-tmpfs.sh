#!/bin/bash

# Move Postgres db data to a directory specified by first argument.
# - If the directory doesn't exists, the script will create a new directory and
# mounts it as a tmpfs.
# - If the directory exists and is NOT empty, the script will NOT move the db
# to it.

PG_TMPFS_DIR=$1
if [ -z $PG_TMPFS_DIR ]; then
    echo "Info: PG_TMPFS_DIR was not specified, keeping db directory untouched."
    exit 1
elif [ -n "$(ls -A $PG_TMPFS_DIR 2>/dev/null)" ]; then
    # We only work with clean dirs. Moving stuff to nonempty dir can be harmful.
    echo "Warn: Won't move database to directory $PG_TMPFS_DIR. It is not empty."
    exit 1
fi

if [ ! -e $PG_TMPFS_DIR ]; then
    echo "Info: Creating tmpfs for the database in $PG_TMPFS_DIR"
    mkdir $PG_TMPFS_DIR
    mount -t tmpfs -o size=1000m tmpfs $PG_TMPFS_DIR
fi

echo "Info: Moving database to $PG_TMPFS_DIR"
mv /var/lib/pgsql/data/* $PG_TMPFS_DIR
rmdir /var/lib/pgsql/data
ln -s $PG_TMPFS_DIR /var/lib/pgsql/data

# Fix ownership for directories
chown -R postgres:postgres $PG_TMPFS_DIR
chown -Rh postgres:postgres /var/lib/pgsql/data

# Make Postgres not complain about permissions
chmod go-rwx $PG_TMPFS_DIR

