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

grep "^archive_command.*smdba-pgarchive" /var/lib/pgsql/data/postgresql.conf > /dev/null 2>&1
if [ $? -eq 0 ]; then
    BACKUP_CONFIGURED=1
else
    BACKUP_CONFIGURED=0
fi

echo "`date +"%H:%M:%S"`   Shut down spacewalk services..."
spacewalk-service stop
systemctl stop postgresql

echo "`date +"%H:%M:%S"`   Checking postgresql version..."
rpm -q postgresql10-server > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   postgresql 10 is already installed. Good."
else
    echo "`date +"%H:%M:%S"`   Installing postgresql 10..."
    zypper --non-interactive in postgresql10 postgresql10-contrib postgresql10-server

    if [ ! $? -eq 0 ]; then
        echo "`date +"%H:%M:%S"`   Installation of postgresql 10 failed!"
        exit 1
    fi
fi

echo "`date +"%H:%M:%S"`   Ensure postgresql 10 is being used as default..."
/usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql10
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully switched to new postgresql version 10."
else
    echo "`date +"%H:%M:%S"`   Could not switch to new postgresql version 10!"
    exit 1
fi

echo "`date +"%H:%M:%S"`   Create new database directory..."
mv /var/lib/pgsql/data /var/lib/pgsql/data-pg96
mkdir /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data

echo "`date +"%H:%M:%S"`   Initialize new postgresql 10 database..."
. /etc/sysconfig/postgresql
if [ -z $POSTGRES_LANG ]; then
    POSTGRES_LANG="en_US.UTF-8"
fi
su -s /bin/bash - postgres -c "initdb -D /var/lib/pgsql/data --locale=$POSTGRES_LANG"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully initialized new postgresql 10 database."
else
    echo "`date +"%H:%M:%S"`   Initialization of new postgresql 10 database failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg96 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql96
    exit 1
fi

echo "`date +"%H:%M:%S"`   Upgrade database to new version postgresql 10..."
su -s /bin/bash - postgres -c "pg_upgrade --old-bindir=/usr/lib/postgresql96/bin --new-bindir=/usr/lib/postgresql10/bin --old-datadir=/var/lib/pgsql/data-pg96 --new-datadir=/var/lib/pgsql/data $FAST_UPGRADE"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully upgraded database to postgresql 10."
else
    echo "`date +"%H:%M:%S"`   Upgrading database to version 10 failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg96 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql96
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

cp /var/lib/pgsql/data-pg96/pg_hba.conf /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data/*

echo "`date +"%H:%M:%S"`   Starting spacewalk services..."
systemctl start postgresql
spacewalk-service start

if [ $BACKUP_CONFIGURED -eq 1 ]; then
    echo
    echo "It seems database backups via smdba had been configured for postgresql 9.6."
    echo "Please re-configure backup for new database version!"
    echo
fi

