#!/bin/bash
set -e

# Prepare
cp -r /manager/schema /tmp
cd /tmp/schema/reportdb
find . -name '*.91' | while read i ; do mv $i ${i%%.91} ; done

# Build the schema
make -f Makefile.schema SCHEMA=uyuni-reportdb-schema VERSION=4.3 RELEASE=testing

if [ -d /etc/sysconfig/rhn/reportdb-schema-upgrade ]; then
    # remove old migration directories before we install the new
    rm -r /etc/sysconfig/rhn/reportdb-schema-upgrade
fi

# Install directories
install -m 0755 -d /etc/sysconfig/rhn
install -m 0755 -d /etc/sysconfig/rhn/reportdb
install -m 0755 -d /etc/sysconfig/rhn/reportdb-schema-upgrade

# Install sql files
install -m 0644 postgres/main.sql /etc/sysconfig/rhn/reportdb
install -m 0644 postgres/end.sql /etc/sysconfig/rhn/reportdb/upgrade-end.sql

( cd upgrade && tar cf - --exclude='*.sql' . | ( cd /etc/sysconfig/rhn/reportdb-schema-upgrade && tar xf - ) )
