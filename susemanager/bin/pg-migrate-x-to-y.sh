#!/bin/bash

NV=""
OV=""
FAST_UPGRADE=""

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
           echo "  $0 -s10 -d13"
           echo "  Will migrate from 10 to 13"
           echo "If you do not specify s or d, they will be infered from the running system."
           exit -1
           ;;
    esac
done
shift $((OPTIND-1))


NEW_VERSION=$(rpm -qi postgresql-server | grep Version | cut -d: -f2 | sed -e "s/ //g")
if [ $NEW_VERSION == "" ];then
    echo "`date +"%H:%M:%S"`    ERROR: There is no postgresql-server package installed"
    exit 1
fi

echo "`date +"%H:%M:%S"`    You have postgresql-server $NEW_VERSION installed."
OLD_VERSION=$(cat /var/lib/pgsql/data/PG_VERSION)
if [ $OLD_VERSION == "" ];then
    echo "`date +"%H:%M:%S"`    ERROR: There is no postgresql server configured"
    exit 1
fi
echo "`date +"%H:%M:%S"`    You have postgresql server $OLD_VERSION configured."

if [ "$NV" != "" ];then
    if [ "$NEW_VERSION" != "$NV" ];then
        echo "`date +"%H:%M:%S"`   ERROR: Latest installed version is $NEW_VERSION, which does not match $NV, the option you provided with -d."
        exit 1
    fi
fi

if [ "$OV" != "" ];then
    if [ "$OLD_VERSION" != "$OV" ];then
        echo "`date +"%H:%M:%S"`   ERROR: Configured version is $OLD_VERSION, which does not match $OV, the option you provided with -s."
        exit 1
    fi
fi

DIR=/var/lib/pgsql

if [ "$FAST_UPGRADE" !=  "" ]; then
    echo "`date +"%H:%M:%S"`   Performing fast upgrade..."
else
    echo -n "`date +"%H:%M:%S"`   Checking diskspace... "
    FREESPACE=`df $DIR | awk '{v=$4} END {print v}'`
    USEDSPACE=`du -s $DIR | awk '{v=$1} END {print v}'`

    # add 10 percent for safety
    NEEDSPACE=`expr $USEDSPACE + $USEDSPACE / 10`

    if [ $FREESPACE -lt $NEEDSPACE ]; then
        echo "failed!"
        NEEDSPACE=`expr $NEEDSPACE / 1024 / 1024`
        FREESPACE=`expr $FREESPACE / 1024 / 1024`

        echo
        echo "`date +"%H:%M:%S"`   Insufficient diskspace in $DIR ($NEEDSPACE GB needed, $FREESPACE GB available)!"
        echo
        echo "`date +"%H:%M:%S"`   A fast migration does not need this additional diskspace, because"
        echo "`date +"%H:%M:%S"`   database files will be hardlinked instead of copied."
        echo
        echo "`date +"%H:%M:%S"`   If you have a backup of the database, run \"$0 fast\""
        echo
        exit 1
    else
        echo "ok."
    fi
fi

echo "`date +"%H:%M:%S"`   Shut down spacewalk services..."
spacewalk-service stop
systemctl stop postgresql

echo "`date +"%H:%M:%S"`   Checking postgresql version..."
rpm -q postgresql$NEW_VERSION-server > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   postgresql $NEW_VERSION is already installed. Good."
else
    echo "`date +"%H:%M:%S"`   Installing postgresql $NEW_VERSION..."
    zypper --non-interactive in postgresql$NEW_VERSION postgresql$NEW_VERSION-contrib postgresql$NEW_VERSION-server

    if [ ! $? -eq 0 ]; then
        echo "`date +"%H:%M:%S"`   Installation of postgresql $NEW_VERSION failed!"
        exit 1
    fi
fi

echo "`date +"%H:%M:%S"`   Ensure postgresql $NEW_VERSION is being used as default..."
/usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql$NEW_VERSION
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully switched to new postgresql version $NEW_VERSION."
else
    echo "`date +"%H:%M:%S"`   Could not switch to new postgresql version $NEW_VERSION!"
    exit 1
fi

echo "`date +"%H:%M:%S"`   Create new database directory..."
mv /var/lib/pgsql/data /var/lib/pgsql/data-pg$OLD_VERSION
mkdir /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data

echo "`date +"%H:%M:%S"`   Initialize new postgresql $NEW_VERSION database..."
. /etc/sysconfig/postgresql
if [ -z $POSTGRES_LANG ]; then
    POSTGRES_LANG="en_US.UTF-8"
fi
su -s /bin/bash - postgres -c "initdb -D /var/lib/pgsql/data --locale=$POSTGRES_LANG"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully initialized new postgresql $NEW_VERSION database."
else
    echo "`date +"%H:%M:%S"`   Initialization of new postgresql $NEW_VERSION database failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg${OLD_VERSION} /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql${OLD_VERSION}
    exit 1
fi

echo "`date +"%H:%M:%S"`   Upgrade database to new version postgresql $NEW_VERSION..."
su -s /bin/bash - postgres -c "pg_upgrade --old-bindir=/usr/lib/postgresql$OLD_VERSION/bin --new-bindir=/usr/lib/postgresql$NEW_VERSION/bin --old-datadir=/var/lib/pgsql/data-pg$OLD_VERSION --new-datadir=/var/lib/pgsql/data $FAST_UPGRADE"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully upgraded database to postgresql $NEW_VERSION."
else
    echo "`date +"%H:%M:%S"`   Upgrading database to version $NEW_VERSION failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg$OLD_VERSION /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql$OLD_VERSION
    exit 1
fi

echo "`date +"%H:%M:%S"`   Tune new postgresql configuration..."
smdba system-check autotuning
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully tuned new postgresql configuration."
else
    echo "`date +"%H:%M:%S"`   Tuning of new postgresql configuration failed!"
    exit 1
fi

cp /var/lib/pgsql/data-pg$OLD_VERSION/pg_hba.conf /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data/*

echo "`date +"%H:%M:%S"`   Starting PostgreSQL service..."
systemctl start postgresql
echo "`date +"%H:%M:%S"`   Reindexing database. This may take a while, please do not cancel it!"
database=$(sed -n "s/^\s*db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)
spacewalk-sql --select-mode - <<<"REINDEX DATABASE ${database};"
if [ ${?} -ne 0 ]; then
    echo "`date +"%H:%M:%S"`   The reindexing failed. Please review the PostgreSQL logs at /var/lib/pgsql/data/log"
    exit 1
fi
echo "`date +"%H:%M:%S"`   Starting spacewalk services..."
spacewalk-service start
