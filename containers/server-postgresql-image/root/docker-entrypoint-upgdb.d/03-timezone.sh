#!/bin/bash

POSTGRESQL=/var/lib/pgsql/data/postgresql.conf

postgres_reconfig() {
    echo "Setting $1 = $2"
    if test $(sed -n "/^$1[[:space:]]*=/p" $POSTGRESQL | wc -l) -ne 0; then
        sed -i "s|^$1[[:space:]]*=.*|$1 = $2|" $POSTGRESQL
    else
        echo "$1 = $2" >> $POSTGRESQL
    fi
}

if test -n "$TZ"; then
    postgres_reconfig "log_timezone" "$TZ"
fi
