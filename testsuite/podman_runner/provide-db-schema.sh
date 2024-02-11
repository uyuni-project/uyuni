#! /bin/bash

set -e

# we changed the schema dir, but we start with a schema which live still in the old location
# provide a symlink to make the tooling work
test -d /usr/share/susemanager/db || mkdir -p /usr/share/susemanager/db
if [ -d /etc/sysconfig/rhn/postgres -a ! -e /usr/share/susemanager/db/postgres ]; then
    ln -s /etc/sysconfig/rhn/postgres /usr/share/susemanager/db/postgres
fi
if [ -d /etc/sysconfig/rhn/reportdb -a ! -e /usr/share/susemanager/db/reportdb ]; then
    ln -s /etc/sysconfig/rhn/reportdb /usr/share/susemanager/db/reportdb
fi
