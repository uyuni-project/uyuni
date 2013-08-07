#!/bin/bash
set -e

# Prepare
cp -r /manager/schema /tmp
cd /tmp/schema/spacewalk
find . -name '*.91' | while read i ; do mv $i ${i%%.91} ; done

# Build the schema
make -f Makefile.schema SCHEMA=susemanager-schema VERSION=1.7 RELEASE=testing

# Install directories
install -m 0755 -d /etc/sysconfig/rhn
install -m 0755 -d /etc/sysconfig/rhn/oracle
install -m 0755 -d /etc/sysconfig/rhn/postgres

# Install sql files
install -m 0644 oracle/main.sql /etc/sysconfig/rhn/oracle
install -m 0644 postgres/main.sql /etc/sysconfig/rhn/postgres
install -m 0644 oracle/end.sql /etc/sysconfig/rhn/oracle/upgrade-end.sql
install -m 0644 postgres/end.sql /etc/sysconfig/rhn/postgres/upgrade-end.sql

