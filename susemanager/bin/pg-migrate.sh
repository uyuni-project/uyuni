#/!bin/bash

FAST_UPGRADE=""

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
fi

echo "`date +"%H:%M:%S"`   Shut down spacewalk services..."
spacewalk-service stop
systemctl stop postgresql

echo "`date +"%H:%M:%S"`   Checking postgresql version..."
rpm -q postgresql96-server > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   postgresql 9.6 is already installed. Good."
else
    echo "`date +"%H:%M:%S"`   Installing postgresql 9.6..."
    zypper --non-interactive in postgresql96 postgresql96-contrib postgresql96-server

    if [ ! $? -eq 0 ]; then
        echo "`date +"%H:%M:%S"`   Installation of postgresql 9.6 failed!"
        exit 1
    fi
fi

echo "`date +"%H:%M:%S"`   Ensure postgresql 9.6 is being used as default..."
/usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql96
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully switched to new postgresql version 9.6."
else
    echo "`date +"%H:%M:%S"`   Could not switch to new postgresql version 9.6!"
    exit 1
fi

echo "`date +"%H:%M:%S"`   Create new database directory..."
mv /var/lib/pgsql/data /var/lib/pgsql/data-pg94
mkdir /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data

echo "`date +"%H:%M:%S"`   Initialize new postgresql 9.6 database..."
su - postgres -c "initdb -D /var/lib/pgsql/data"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully initialized new postgresql 9.6 database."
else
    echo "`date +"%H:%M:%S"`   Initialization of new postgresql 9.6 database failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg94 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql94
    exit 1
fi

echo "`date +"%H:%M:%S"`   Upgrade database to new version postgresql 9.6..."
su - postgres -c "pg_upgrade --old-bindir=/usr/lib/postgresql94/bin --new-bindir=/usr/lib/postgresql96/bin --old-datadir=/var/lib/pgsql/data-pg94 --new-datadir=/var/lib/pgsql/data --retain $FAST_UPGRADE"
if [ $? -eq 0 ]; then
    echo "`date +"%H:%M:%S"`   Successfully upgraded database to postgresql 9.6."
else
    echo "`date +"%H:%M:%S"`   Initialization of new postgresql 9.6 database failed!"
    echo "`date +"%H:%M:%S"`   Trying to restore previous state..."
    mv /var/lib/pgsql/data /var/lib/pgsql/data-new-failed
    mv /var/lib/pgsql/data-pg94 /var/lib/pgsql/data
    /usr/sbin/update-alternatives --set postgresql /usr/lib/postgresql94
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

cp /var/lib/pgsql/data-pg94/pg_hba.conf /var/lib/pgsql/data
chown postgres:postgres /var/lib/pgsql/data/*

echo "`date +"%H:%M:%S"`   Starting spacewalk services..."
systemctl start postgresql
spacewalk-service start
