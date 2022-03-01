#! /bin/bash
#
# only use for testing
#
set -e

DBNAME="reportdb"
DBUSER="reportuser"
DBPASS="secret"


zypper install postgresql postgresql-server
systemctl --quiet enable postgresql

. /etc/sysconfig/postgresql
if [ -z $POSTGRES_LANG ]; then
    grep "^POSTGRES_LANG" /etc/sysconfig/postgresql > /dev/null 2>&1
    if [ $? = 0 ]; then
        sed -i -e "s/^POSTGRES_LANG.*$/POSTGRES_LANG=\"en_US.UTF-8\"/" /etc/sysconfig/postgresql
    else
        echo "POSTGRES_LANG=\"en_US.UTF-8\"" >> /etc/sysconfig/postgresql
    fi
fi
systemctl start postgresql

runuser - postgres -c "createdb -E UTF8 '$DBNAME'"
runuser - postgres -c "echo \"CREATE ROLE $DBUSER PASSWORD '$DBPASS' SUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;\" | psql"

echo "listen_addresses = '*'" >> /var/lib/pgsql/data/postgresql.conf

echo "
local   $DBNAME        $DBUSER      md5
host    $DBNAME        $DBUSER      0.0.0.0/0      md5
host    $DBNAME        $DBUSER      ::/0           md5
" > /var/lib/pgsql/data/pg_hba.conf.tmp
cat /var/lib/pgsql/data/pg_hba.conf >> /var/lib/pgsql/data/pg_hba.conf.tmp
mv /var/lib/pgsql/data/pg_hba.conf.tmp /var/lib/pgsql/data/pg_hba.conf

systemctl restart postgresql.service

# psql -h server.domain.top -W -f common/tables/System.sql ReportDb reportuser

