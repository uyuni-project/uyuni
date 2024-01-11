#!/bin/bash
set -e

# Prepare
cp -r /manager/schema /tmp
cd /tmp/schema/spacewalk

# Build the schema
make -f Makefile.schema SCHEMA=susemanager-schema VERSION=5.0 RELEASE=testing

if [ -d /usr/share/susemanager/db/schema-upgrade ]; then
    # remove old migration directories before we install the new
    rm -r /usr/share/susemanager/db/schema-upgrade
fi

# Install directories
install -m 0755 -d /usr/share/susemanager/db
install -m 0755 -d /usr/share/susemanager/db/postgres
install -m 0755 -d /usr/share/susemanager/db/schema-upgrade

# Install sql files
install -m 0644 postgres/main.sql /usr/share/susemanager/db/postgres
install -m 0644 postgres/end.sql /usr/share/susemanager/db/postgres/upgrade-end.sql

( cd upgrade && tar cf - --exclude='*.sql' . | ( cd /usr/share/susemanager/db/schema-upgrade && tar xf - ) )

