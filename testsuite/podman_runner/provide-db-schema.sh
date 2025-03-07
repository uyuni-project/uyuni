#! /bin/bash

set -xe

cp -r /manager/schema /tmp
cd /tmp/schema/spacewalk

# Build the schema
VERSION=`rpm -qf --qf '%{version}' /usr/share/susemanager/db/postgres/main.sql`
make -f Makefile.schema SCHEMA=susemanager-schema VERSION=${VERSION} RELEASE=0

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


# we changed the schema dir, but we start with a schema which live still in the old location
# provide a symlink to make the tooling work
test -d /usr/share/susemanager/db || mkdir -p /usr/share/susemanager/db
if [ -d /etc/sysconfig/rhn/postgres -a ! -e /usr/share/susemanager/db/postgres ]; then
    ln -s /etc/sysconfig/rhn/postgres /usr/share/susemanager/db/postgres
fi
if [ -d /etc/sysconfig/rhn/reportdb -a ! -e /usr/share/susemanager/db/reportdb ]; then
    ln -s /etc/sysconfig/rhn/reportdb /usr/share/susemanager/db/reportdb
fi
