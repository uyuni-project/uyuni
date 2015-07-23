#!/bin/bash
#
# Copyright (C) 2014 Novell, Inc.
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

LOGFILE="/var/log/susemanager-ora2pg.log"
# set -x
exec > >(tee -a $LOGFILE) 2>&1

echo "################################################"
date
echo "################################################"

if [ $UID -ne 0 ]; then
    echo "You must run this as root."
    exit
fi

TIMESTAMP=`date "+%Y%m%d%H%M%S"`
DUMPFILE="/var/tmp/oracle-db.dump"

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

exists_db() {
    EXISTS=$(su - postgres -c 'psql -t -c "select datname from pg_database where datname='"'$DBNAME'"';"')
    if [ "x$EXISTS" == "x $DBNAME" ] ; then
        return 0
    else
        return 1
    fi
}

exists_plpgsql() {
    EXISTS=$(su - postgres -c 'psql -At -c "select lanname from pg_catalog.pg_language where lanname='"'plpgsql'"';"'" $DBNAME")
    if [ "x$EXISTS" == "xplpgsql" ] ; then
        return 0
    else
        return 1
    fi
}

exists_pltclu() {
    EXISTS=$(su - postgres -c 'psql -At -c "select lanname from pg_catalog.pg_language where lanname='"'pltclu'"';"'" $DBNAME")
    if [ "x$EXISTS" == "xpltclu" ] ; then
        return 0
    else
        return 1
    fi
}

exists_user() {
    EXISTS=$(su - postgres -c 'psql -t -c "select usename from pg_user where usename='"'$DBUSER'"';"')
    if [ "x$EXISTS" == "x $DBUSER" ] ; then
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
        zypper --non-interactive in $pkg
        if [ "$?" != "0" ]; then
            echo "Failed to install '$pkg'"
            exit 1
        fi
    else
        zypper --non-interactive up $pkg
    fi
}

upgrade_schema() {
    if [ -s "$DUMPFILE" ]; then
        echo "schema dump exists. Skipping upgrade."
        return 0
    fi
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
    if [ ! -s "$DUMPFILE" ]; then
        perl -CSAD /usr/bin/spacewalk-dump-schema \
             --db="$DBNAME" --user="$DBUSER" --password="$DBPASS" > $DUMPFILE
        if [ "$?" != "0" ]; then
            echo "Failed to dump the schema."
            exit 1
        fi
    else
        echo "Using existing schema dump ($DUMPFILE)"
    fi
}

import_schema() {
    if [ ! -s "$DUMPFILE" ]; then
        echo "Unable to import schema. $DUMPFILE does not exist or is empty."
        exit 1
    fi
    PGPASSWORD="$DBPASS" psql -h localhost -U "$DBUSER" "$DBNAME" < $DUMPFILE
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
    zypper --non-interactive in \
              +spacewalk-postgresql +spacewalk-java-postgresql +spacewalk-backend-sql-postgresql \
              -spacewalk-oracle -spacewalk-java-oracle -spacewalk-backend-sql-oracle
    if [ "$?" != "0" ]; then
        echo "Failed to switch SUSE Manager packages."
        exit 1
    fi
}

setup_postgres() {

    insserv postgresql
    rcpostgresql start
    if [ "$?" != "0" ]; then
        echo "Failed to start postgresql database."
        exit 1
    fi

    if ! exists_db ; then
            su - postgres -c "createdb -E UTF8 '$DBNAME'"
    fi
    if ! exists_plpgsql ; then
            su - postgres -c "createlang plpgsql '$DBNAME'"
    fi
    if ! exists_pltclu ; then
            su - postgres -c "createlang pltclu '$DBNAME'"
    fi
    if ! exists_user ; then
            su - postgres -c "yes '$DBPASS' | createuser -P -sDR '$DBUSER'" # 2>/dev/null
    fi

    echo "
host	$DBNAME	$DBUSER	0.0.0.0/0	md5
local	$DBNAME	$DBUSER	md5
host	$DBNAME	$DBUSER	127.0.0.1/8	md5
host	$DBNAME	$DBUSER	::1/128	md5
local	all	all	peer
host	all	all	127.0.0.1/32	ident
host	all	all	::1/128	ident
local	replication	postgres	peer
" > /var/lib/pgsql/data/pg_hba.conf

    change_value db_backend postgresql

    smdba system-check autotuning
    rcpostgresql restart
}

configure_suma() {
    echo "
db-backend=postgresql
db-user=$DBUSER
db-password=$DBPASS
db-name=$DBNAME
db-host=$DBHOST
db-port=5432
" > "/root/answer.txt.$TIMESTAMP"

    cp /etc/rhn/rhn.conf "/etc/rhn/rhn.conf.$TIMESTAMP"

    spacewalk-setup --db-only --external-postgresql --answer-file="/root/answer.txt.$TIMESTAMP"
    if [ "$?" != "0" ]; then
        echo "Failed to setup spacewalk with db-only."
        exit 1
    fi
    rm "/root/answer.txt.$TIMESTAMP"

    cp "/etc/rhn/rhn.conf.$TIMESTAMP" /etc/rhn/rhn.conf
    change_value db_backend postgresql
    change_value db_name $DBNAME
    change_value db_port 5432
    change_value hibernate.dialect org.hibernate.dialect.PostgreSQLDialect
    change_value hibernate.connection.driver_class org.postgresql.Driver
    change_value hibernate.connection.driver_proto jdbc:postgresql
}

DBBACKEND=`read_value db_backend`
DBHOST=`read_value db_host`
DBUSER=`read_value db_user`
DBPASS=`read_value db_password`
# oracle DB has: //localhost:1521/susemanager
DBSID=`read_value db_name`
if echo "$DBSID" | grep '/' >/dev/null; then
    DBNAME=`echo "$DBSID" | sed 's/.*\///'`
else
    DBNAME="$DBSID"
fi

if [ "$DBBACKEND" != "oracle" ]; then
    echo "Database backend must be oracle."
    exit 1
fi

# dump repo configuration in the log
# before and after a refresh
zypper --no-refresh --non-interactive lr -u
zypper --non-interactive ref -s
zypper --no-refresh --non-interactive lr -u

install_latest spacewalk-utils
install_latest susemanager-schema

upgrade_schema

spacewalk-service stop

change_value db_backend_target postgresql

dump_schema

switch_oracle2postgres

setup_postgres

configure_suma

import_schema

spacewalk-service start
