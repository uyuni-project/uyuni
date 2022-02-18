#!/bin/bash

NV=""
OV=""
FAST_UPGRADE=""

timestamp () {
  echo $(date +"%H:%M:%S")
}

while getopts "s:d:f" o;do
    case "${o}" in
        s)
            OV=${OPTARG}
            ;;
        d)  
            NV=${OPTARG}
            ;;
       f)
           FAST_UPGRADE=" --link"
           ;;   
       *)
           echo "Usage: $0 [-s initial postgresql to migrate from] [-d destination postgresql to migrate to] [-f fast upgrade]"
           echo "For example:"
           echo "  $0 -s 10 -d 13"
           echo "  Will migrate from 10 to 13"
           echo "If you do not specify s or d, they will be infered from the running system."
           exit -1
           ;;
    esac
done
shift $((OPTIND-1))


NEW_VERSION=$(rpm -qa --qf '%{VERSION}\n' 'name=postgresql[0-8][0-9]-server'  | cut -d. -f1 | sort -n | tail -1)
if [ $NEW_VERSION == "" ];then
    echo "$(timestamp)    ERROR: There is no postgresql-server package installed"
    exit 1
fi

echo "$(timestamp)    You have postgresql-server $NEW_VERSION installed."
OLD_VERSION=$(cat /var/lib/pgsql/data/PG_VERSION)
if [ $OLD_VERSION == "" ];then
    echo "$(timestamp)    ERROR: There is no postgresql server configured"
    exit 1
fi
echo "$(timestamp)    You have postgresql server $OLD_VERSION configured."

if [ "$NV" != "" ];then
    if [ "$NEW_VERSION" != "$NV" ];then
        echo "$(timestamp)   ERROR: Latest installed version is $NEW_VERSION, which does not match $NV, the option you provided with -d."
        exit 1
    fi
fi

if [ "$OV" != "" ];then
    if [ "$OLD_VERSION" != "$OV" ];then
        echo "$(timestamp)   ERROR: Configured version is $OLD_VERSION, which does not match $OV, the option you provided with -s."
        exit 1
    fi
fi

if [ -d "/var/lib/pgsql/data-new-failed" ]; then
    echo "$(timestamp)   /var/lib/pgsql/data-new-failed already exists!"
    echo "$(timestamp)   Most likely this is the result of a previous failed migration."
    echo "$(timestamp)   Verify if you still need this directory and remove it otherwise."
    exit 1
fi

if [ -d "/var/lib/pgsql/data-pg${OLD_VERSION}" ]; then
    echo "$(timestamp)   /var/lib/pgsql/data-pg${OLD_VERSION} already exists!"
    echo "$(timestamp)   Most likely this is the result of a previous failed migration and a failed rollback!"
    echo "$(timestamp)   It is strongly recommend that you a restore backup before trying again!"
    exit 1
fi

DIR=/var/lib/pgsql

if [ $(grep data_directory ${DIR}/data/postgresql.conf) ]; then
    echo "$(timestamp) data_directory is configured in ${DIR}/data/postgresql.conf"
    echo "$(timestamp) For the migration to work, data_directory should not be defined explicetely"
    exit 1
fi

if [ "$FAST_UPGRADE" !=  "" ]; then
    echo "$(timestamp)   Performing fast upgrade..."
else
    echo -n "$(timestamp)   Checking diskspace... "
    FREESPACE=`df $DIR | awk '{v=$4} END {print v}'`
    USEDSPACE=`du -s $DIR | awk '{v=$1} END {print v}'`

    # add 10 percent for safety
    NEEDSPACE=`expr $USEDSPACE + $USEDSPACE / 10`

    if [ $FREESPACE -lt $NEEDSPACE ]; then
        echo "failed!"
        NEEDSPACE=`expr $NEEDSPACE / 1024 / 1024`
        FREESPACE=`expr $FREESPACE / 1024 / 1024`

        echo
        echo "$(timestamp)   Insufficient diskspace in $DIR ($NEEDSPACE GB needed, $FREESPACE GB available)!"
        echo
        echo "$(timestamp)   A fast migration does not need this additional diskspace, because"
        echo "$(timestamp)   database files will be hardlinked instead of copied."
        echo
        echo "$(timestamp)   If you have a backup of the database, run \"$0 fast\""
        echo
        exit 1
    else
        echo "ok."
    fi
fi

echo "$(timestamp)   Shut down spacewalk services..."
spacewalk-service stop
if [ ${?} -ne 0 ]; then
    echo "$(timestamp)   At least one spacewalk service failed to stop!"
    echo "$(timestamp)   Please check the logs at /var/log/rhn and /var/log/tomcat"
    exit 1
fi

systemctl stop postgresql
if [ ${?} -ne 0 ]; then
    echo "$(timestamp)   PostgresSQL failed to stop!"
    echo "$(timestamp)   Please check systemd logs for PostreSQL"
    exit 1
fi

echo "$(timestamp)   Checking postgresql version..."
rpm -q postgresql$NEW_VERSION-server > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "$(timestamp)   postgresql $NEW_VERSION is already installed. Good."
else
    echo "$(timestamp)   Installing postgresql $NEW_VERSION..."
    zypper --non-interactive in postgresql$NEW_VERSION postgresql$NEW_VERSION-contrib postgresql$NEW_VERSION-server

    if [ ! $? -eq 0 ]; then
        echo "$(timestamp)   Installation of postgresql $NEW_VERSION failed!"
        exit 1
    fi
fi

echo "$(timestamp)   Ensure postgresql $NEW_VERSION is being used as default..."
/usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql$NEW_VERSION
if [ $? -eq 0 ]; then
    echo "$(timestamp)   Successfully switched to new postgresql version $NEW_VERSION."
else
    echo "$(timestamp)   Could not switch to new postgresql version $NEW_VERSION!"
    exit 1
fi

echo "$(timestamp)   Create a backup at /var/lib/pgsql/data-pg$OLD_VERSION..."
mv /var/lib/pgsql/data /var/lib/pgsql/data-pg$OLD_VERSION
echo "$(timestamp)   Create new database directory..."
mkdir /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data

echo "$(timestamp)   Initialize new postgresql $NEW_VERSION database..."
. /etc/sysconfig/postgresql 2>/dev/null # Load locale for SUSE
PGHOME=$(getent passwd postgres | awk -F: '{print $6}')
. $PGHOME/.i18n 2>/dev/null # Load locale for Enterprise Linux
if [ -z $POSTGRES_LANG ]; then
    POSTGRES_LANG="en_US.UTF-8"
    [ ! -z $LC_CTYPE ] && POSTGRES_LANG=$LC_CTYPE
fi

su -s /bin/bash - postgres -c "initdb -D /var/lib/pgsql/data --locale=$POSTGRES_LANG"
if [ $? -eq 0 ]; then
    echo "$(timestamp)   Successfully initialized new postgresql $NEW_VERSION database."
else
    echo "$(timestamp)   Initialization of new postgresql $NEW_VERSION database failed!"
    echo "$(timestamp)   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg${OLD_VERSION} /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql${OLD_VERSION}
    exit 1
fi

echo "$(timestamp)   Upgrade database to new version postgresql $NEW_VERSION..."
su -s /bin/bash - postgres -c "pg_upgrade --old-bindir=/usr/lib/postgresql$OLD_VERSION/bin --new-bindir=/usr/lib/postgresql$NEW_VERSION/bin --old-datadir=/var/lib/pgsql/data-pg$OLD_VERSION --new-datadir=/var/lib/pgsql/data $FAST_UPGRADE"
if [ $? -eq 0 ]; then
    echo "$(timestamp)   Successfully upgraded database to postgresql $NEW_VERSION."
else
    echo "$(timestamp)   Upgrading database to version $NEW_VERSION failed!"
    echo "$(timestamp)   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg$OLD_VERSION /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql$OLD_VERSION
    exit 1
fi

echo "$(timestamp)   Tune new postgresql configuration..."
smdba system-check autotuning
if [ $? -eq 0 ]; then
    echo "$(timestamp)   Successfully tuned new postgresql configuration."
else
    echo "$(timestamp)   Tuning of new postgresql configuration failed!"
    exit 1
fi

cp /var/lib/pgsql/data-pg$OLD_VERSION/pg_hba.conf /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data/*

echo "$(timestamp)   Starting PostgreSQL service..."
systemctl start postgresql
echo "$(timestamp)   Reindexing database. This may take a while, please do not cancel it!"
database=$(sed -n "s/^\s*db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)
spacewalk-sql --select-mode - <<<"REINDEX DATABASE \"${database}\";"
if [ ${?} -ne 0 ]; then
    echo "$(timestamp)   The reindexing failed. Please review the PostgreSQL logs at /var/lib/pgsql/data/log"
    exit 1
fi
echo "$(timestamp)   Starting spacewalk services..."
spacewalk-service start
