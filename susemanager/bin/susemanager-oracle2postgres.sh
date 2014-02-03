#!/bin/bash
#
# Copyright (C) 2014 Novell, Inc.
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

if [ $UID -ne 0 ]; then
    echo "You must run this as root."
    exit
fi

TIMESTAMP=`date "+%Y%m%d%H%M%S"`

read_value() {
    local key=$1
    local val=$( egrep -m1 "^$key[[:space:]]*=" /etc/rhn/rhn.conf | sed "s/^$key[[:space:]]*=[[:space:]]*\(.*\)/\1/" || echo "" )
    echo $val
    return 0
}

change_value() {
    local key=$1
    local val=$2
    if ! egrep -m1 "^$key[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
        echo "$key = $val" >> /etc/rhn/rhn.conf
    else
        sed -i "s/^$key[[:space:]]*=.*/$key = $val/" /etc/rhn/rhn.conf
    fi
}

is_embedded_db() {
    if [ "$DBHOST" = "localhost" ]; then
        return 0
    else
        return 1
    fi
}

install_latest() {
    local pkg=$1

    if [ -z "$pkg" ]; then
        echo "package name required"
        exit 1
    fi
    if ! rpm -q $pkg >/dev/null 2>&1 ; then
        zypper in $pkg
        if [ "$?" != "0" ]; then
            echo "Failed to install '$pkg'"
            exit 1
        fi
    else
        zypper up $pkg
    fi
}

upgrade_schema() {
    spacewalk-schema-upgrade
    if [ "$?" != "0" ]; then
        echo "Failed to upgrade the schema."
        exit 1
    fi
}

dump_schema() {
    if [ ! -e /usr/bin/spacewalk-dump-schema ]; then
        echo "Missing '/usr/bin/spacewalk-dump-schema'"
        exit 1
    fi
    local dumpfile=`mktemp --tmpdir=/var/tmp/ oracle-db.dump.XXXXXXXX`
    perl -CSAD /usr/bin/spacewalk-dump-schema \
     --db="$DBNAME" --user="$DBUSER" --password="$DBPASS" > $dumpfile
    if [ "$?" != "0" ]; then
        echo "Failed to dump the schema."
        return 1
    fi
    echo $dumpfile
    return 0
}

import_schema() {
    local dumpfile=$1
    PGPASSWORD="$DBPASS" psql -h localhost -U "$DBUSER" "$DBNAME" < $dumpfile
    if [ "$?" != "0" ]; then
        echo "Failed to load the schema."
        exit 1
    fi
}

switch_oracle2postgres() {
    /etc/init.d/oracle stop
    if [ "$?" != "0" ]; then
        echo "Failed to stop oracle DB."
        exit 1
    fi
    insserv -r oracle
    zypper in +spacewalk-postgresql +spacewalk-java-postgresql +spacewalk-backend-sql-postgresql \
              -spacewalk-oracle -spacewalk-java-oracle -spacewalk-backend-sql-oracle
    if [ "$?" != "0" ]; then
        echo "Failed to switch SUSE Manager packages."
        exit 1
    fi
    insserv postgresql
    rcpostgresql start
    if [ "$?" != "0" ]; then
        echo "Failed to start postgresql database."
        exit 1
    fi

    su - postgres -c "PGPASSWORD=$DBPASS; createdb -E UTF8 '$DBNAME';"
    su - postgres -c "yes '$DBPASS' | createuser -P -sDR '$DBUSER'"
    su - postgres -c "createlang plpgsql '$DBNAME';"
    su - postgres -c "createlang pltclu '$DBNAME';"

    cp /var/lib/pgsql/data/pg_hba.conf "/var/lib/pgsql/data/pg_hba.conf.$TIMESTAMP"
    echo "
local $DBNAME $DBUSER md5
host  $DBNAME $DBUSER 127.0.0.1/8 md5
host  $DBNAME $DBUSER ::1/128 md5
" > /var/lib/pgsql/data/pg_hba.conf
    cat "/var/lib/pgsql/data/pg_hba.conf.$TIMESTAMP" /var/lib/pgsql/data/pg_hba.conf

    smdba system-check autotuning

    echo "
db-backend=postgresql
db-user=$DBUSER
db-password=$DBPASS
db-name=$DBNAME
db-host=$DBHOST
db-port=5432
" > "/tmp/answer.txt.$TIMESTAMP"

   cp /etc/rhn/rhn.conf "/etc/rhn/rhn.conf.$TIMESTAMP"

    spacewalk-setup --db-only --answer-file="/tmp/answer.txt.$TIMESTAMP"
    if [ "$?" != "0" ]; then
        echo "Failed to setup spacewalk with db-only."
        exit 1
    fi

    cp "/etc/rhn/rhn.conf.$TIMESTAMP" /etc/rhn/rhn.conf
    change_value db_backend postgresql
    change_value hibernate.dialect org.hibernate.dialect.PostgreSQLDialect
    change_value hibernate.connection.driver_class org.postgresql.Driver
    change_value hibernate.connection.driver_proto jdbc:postgresql
}

DBBACKEND=`read_value db_backend`
DBNAME=`read_value db_name`
DBHOST=`read_value db_host`
DBUSER=`read_value db_user`
DBPASS=`read_value db_password`

if [ "$DBBACKEND" != "oracle" ]; then
    echo "Database backend must be oracle."
    exit 1
fi

install_latest spacewalk-utils
install_latest susemanager-schema

upgrade_schema

spacewalk-service stop

DUMPFILE=`dump_schema`
if [ "$?" != "0" ]; then
    echo "Failed to dump the schema."
    exit 1
fi

switch_oracle2postgres

import_schema $DUMPFILE

spacewalk-service start
