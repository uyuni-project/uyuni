#! /bin/bash
#
# Copyright (C) 2014 Novell, Inc.
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

LOGFILE="/var/log/susemanager-upgrade.log"
# set -x
exec > >(tee -a $LOGFILE) 2>&1

if [ $UID -ne 0 ]; then
    echo "You must run this as root."
    exit
fi

read_value() {
    local key=$1
    local val=$( egrep -m1 "^$key[[:space:]]*=" /etc/rhn/rhn.conf | sed "s/^$key[[:space:]]*=[[:space:]]*\(.*\)/\1/" || echo "" )
    echo $val
    return 0

}

is_embedded_db() {
    if [ "$DBHOST" = "localhost" ]; then
        return 0
    else
        return 1
    fi
}

exists_pg_db() {
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
    if is_embedded_db ; then
        EXISTS=$(su - postgres -c 'psql -At -c "select lanname from pg_catalog.pg_language where lanname='"'pltclu'"';"'" $DBNAME")
    else
        EXISTS=$(echo "select lanname from pg_catalog.pg_language where lanname='pltclu';" | spacewalk-sql --select-mode - | grep pltclu | sed 's/^[[:space:]]*\(pltclu\)[[:space:]]*$/\1/')
    fi
    if [ "x$EXISTS" == "xpltclu" ] ; then
        return 0
    else
        return 1
    fi
}

upgrade_pg() {

    if ! db_exists ; then
        echo "Database does not exist or is not running"
        exit 1
    fi
    if ! is_embedded_db ; then
        if ! exists_pltclu ; then
            echo "SUSE Manager is not running with an embedded DB and your installation miss the 'pltclu' extension."
            echo "Please install it before you continue"
            exit 1
        fi
        return 0
    fi
    rcpostgresql status || rcpostgresql start
    if ! exists_pg_db ; then
        echo "Database does not exist."
        exit 1
    fi
    if ! exists_plpgsql ; then
            su - postgres -c "createlang plpgsql '$DBNAME'"
    fi
    if ! exists_pltclu ; then
            su - postgres -c "createlang pltclu '$DBNAME'"
    fi
}

db_exists() {
    EXISTS=$(echo "select label from rhnVersionInfo;" | spacewalk-sql --select-mode - | grep "schema")
    if [ -z "$EXISTS" ]; then
        return 1
    else
        return 0
    fi
}

upgrade_oracle() {

    if ! db_exists ; then
        echo "Database does not exist or is not running"
        exit 1
    fi
    if ! is_embedded_db ; then
        return 1
    fi

    # oracle DB has: //localhost:1521/susemanager
    if echo "$DBNAME" | grep '/' >/dev/null; then
        DBSID=`echo "$DBNAME" | sed 's/.*\///'`
    else
        DBSID="$DBNAME"
    fi

    echo "
grant connect to $DBUSER;
grant create table to $DBUSER;
grant create view to $DBUSER;
grant create type to $DBUSER;
grant create sequence to $DBUSER;
grant create procedure to $DBUSER;
grant create operator to $DBUSER;
grant create synonym to $DBUSER;
grant create trigger to $DBUSER;
grant create role to $DBUSER;
grant alter session to $DBUSER;
quit
" > /tmp/dbsetup.sql

    su -s /bin/bash - oracle -c "ORACLE_SID=$DBSID sqlplus / as sysdba @/tmp/dbsetup.sql;"
    rm -f /tmp/dbsetup.sql
}

upgrade_schema() {
    spacewalk-schema-upgrade -y || exit 1
}

upgrade_config() {
    local allowed_iss_slaves=`read_value allowed_iss_slaves`
    local iss_parent=`read_value iss_parent`
    local iss_ca_chain=`read_value iss_ca_chain`
    local timestamp=`date "+%Y%m%d%H%M%S"`
    local rhnconf="/etc/rhn/rhn.conf"
    local bak="/etc/rhn/rhn.conf.$timestamp"

    if [ -n "$iss_parent" -a -n "$iss_ca_chain" ]; then
        cp "$rhnconf" "$bak"
        echo "Convert slave configuration to database"
        echo "
        INSERT INTO rhnIssMaster (id, label, is_current_master, ca_cert)
        VALUES (sequence_nextval('rhn_issmaster_seq'), '$iss_parent', 'Y', '$iss_ca_chain');
        " | spacewalk-sql -
        sed -i 's/^iss_parent.*//' $rhnconf
    fi
    if [ -n "$allowed_iss_slaves" ]; then
        if [ ! -e $bak ]; then
            cp "$rhnconf" "$bak"
        fi

        echo "Convert master configuration to database"
        IFS=', '
        for SLAVE in $allowed_iss_slaves; do
            echo "
            INSERT INTO rhnISSSlave (id, slave)
            VALUES (sequence_nextval('rhn_issslave_seq'), '$SLAVE');
            " | spacewalk-sql -
        done
        sed -i 's/^allowed_iss_slaves.*//' $rhnconf
    fi
    /usr/bin/spacewalk-setup-tomcat
}

DBBACKEND=`read_value db_backend`
DBNAME=`read_value db_name`
DBHOST=`read_value db_host`
DBUSER=`read_value db_user`

if [ "$DBBACKEND" != "postgresql" -a "$DBBACKEND" != "oracle" ]; then
    echo "Unknown database backend '$DBBACKEND'"
    exit 1
fi

if [ "$DBBACKEND" = "postgresql" -a ! -e "/usr/bin/psql" ]; then
    echo "/usr/bin/psql not found"
    exit 1
fi

if [ "$DBBACKEND" = "oracle" ]; then
    SQLPLUS=`which sqlplus`
    if [ -z "$SQLPLUS" ]; then
        echo "sqlplus not found"
        exit 1
    fi
fi

rcapache2 status && spacewalk-service stop

if [ "$DBBACKEND" = "postgresql" ]; then
    upgrade_pg
else
    upgrade_oracle
fi

upgrade_schema

upgrade_config

spacewalk-service start
