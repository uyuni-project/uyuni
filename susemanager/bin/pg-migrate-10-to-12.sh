#!/bin/bash

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
rpm -q postgresql12-server > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   postgresql 12 is already installed. Good."
else
    echo "`date +"%H:%M:%S"`   Installing postgresql 10..."
    zypper --non-interactive in postgresql12 postgresql12-contrib postgresql12-server

    if [ ! $? -eq 0 ]; then
        echo "`date +"%H:%M:%S"`   Installation of postgresql 10 failed!"
        exit 1
    fi
fi

echo "`date +"%H:%M:%S"`   Ensure postgresql 12 is being used as default..."
/usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql12
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully switched to new postgresql version 12."
else
    echo "`date +"%H:%M:%S"`   Could not switch to new postgresql version 12!"
    exit 1
fi

echo "`date +"%H:%M:%S"`   Create new database directory..."
mv /var/lib/pgsql/data /var/lib/pgsql/data-pg10
mkdir /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data

echo "`date +"%H:%M:%S"`   Initialize new postgresql 12 database..."
. /etc/sysconfig/postgresql
if [ -z $POSTGRES_LANG ]; then
    POSTGRES_LANG="en_US.UTF-8"
fi
su -s /bin/bash - postgres -c "initdb -D /var/lib/pgsql/data --locale=$POSTGRES_LANG"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully initialized new postgresql 12 database."
else
    echo "`date +"%H:%M:%S"`   Initialization of new postgresql 12 database failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg10 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql10
    exit 1
fi

echo "`date +"%H:%M:%S"`   Upgrade database to new version postgresql 12..."
su -s /bin/bash - postgres -c "pg_upgrade --old-bindir=/usr/lib/postgresql10/bin --new-bindir=/usr/lib/postgresql12/bin --old-datadir=/var/lib/pgsql/data-pg10 --new-datadir=/var/lib/pgsql/data $FAST_UPGRADE"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully upgraded database to postgresql 12."
else
    echo "`date +"%H:%M:%S"`   Upgrading database to version 12 failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg10 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql10
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

cp /var/lib/pgsql/data-pg10/pg_hba.conf /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data/*

echo "`date +"%H:%M:%S"`   Starting spacewalk services..."
systemctl start postgresql
spacewalk-service start


