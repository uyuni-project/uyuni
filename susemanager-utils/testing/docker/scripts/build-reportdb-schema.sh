#!/bin/bash
set -e

# Prepare
cp -r /manager/schema /tmp
cd /tmp/schema/reportdb

# Build the schema
make -f Makefile.schema SCHEMA=uyuni-reportdb-schema VERSION=5.0 RELEASE=testing

if [ -d /usr/share/susemanager/db/reportdb-schema-upgrade ]; then
    # remove old migration directories before we install the new
    rm -r /usr/share/susemanager/db/reportdb-schema-upgrade
fi

# Install directories
install -m 0755 -d /usr/share/susemanager/db
install -m 0755 -d /usr/share/susemanager/db/reportdb
install -m 0755 -d /usr/share/susemanager/db/reportdb-schema-upgrade

# Install sql files
install -m 0644 postgres/main.sql /usr/share/susemanager/db/reportdb
install -m 0644 postgres/end.sql /usr/share/susemanager/db/reportdb/upgrade-end.sql

( cd upgrade && tar cf - --exclude='*.sql' . | ( cd /usr/share/susemanager/db/reportdb-schema-upgrade && tar xf - ) )
