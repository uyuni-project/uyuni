#!/bin/bash

NEW_VERSION=13
OLD_VERSION=12

FAST_UPGRADE=""
DIR=/var/lib/pgsql

if [ $# -gt 1 ]; then
    echo "`date +"%H:%M:%S"`   Usage: '$0' or '$0 fast'"
    exit 1
fi

if [ $# == 1 ]; then
    if [ $1 == "fast" ]; then
        echo "`date +"%H:%M:%S"`   Performing fast upgrade..."
        FAST_UPGRADE=" --link"
    else
        echo "`date +"%H:%M:%S"`   Unknown option '$1'"
        echo "`date +"%H:%M:%S"`   Usage: '$0' or '$0 fast'"
        exit 1
    fi
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

echo "`date +"%H:%M:%S"`   Starting spacewalk services..."
systemctl start postgresql
echo "Reindexing database. This may take a while, please do not cancel it!"
database=$(sed -n "s/^\s*db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)
spacewalk-sql --select-mode - <<<"REINDEX DATABASE ${database};"
spacewalk-service start

