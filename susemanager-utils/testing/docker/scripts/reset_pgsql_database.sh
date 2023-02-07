#!/bin/bash

set -ex

[ -z $WORKDIR ] && WORKDIR=/manager

export PERLLIB=$WORKDIR/spacewalk/setup/lib/:$WORKDIR/web/modules/rhn/:$WORKDIR/web/modules/pxt/:$WORKDIR/schema/spacewalk/lib
export PATH=$WORKDIR/schema/spacewalk/:$WORKDIR/spacewalk/setup/bin/:$PATH

echo Going to reset PGSQL database

echo $PATH
echo $PERLLIB

./build-schema.sh
./build-reportdb-schema.sh

export SYSTEMD_NO_WRAP=1
sysctl -w kernel.shmmax=18446744073709551615 ||:
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl start"

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf
touch /etc/rhn/rhn.conf

ls -la /usr/bin/rhn-config-satellite.pl
ls -la /etc/rhn
find /var/lib/rhn

spacewalk-setup --clear-db --db-only --answer-file=clear-db-answers-pgsql.txt --external-postgresql --non-interactive || {
  cat /var/log/rhn/populate_db.log
  exit 1
}

# Create symlinks because uyuni-setup-reportdb uses absolute paths
ln -s $WORKDIR/schema/spacewalk/spacewalk-sql /usr/bin/spacewalk-sql
ln -s $WORKDIR/schema/spacewalk/spacewalk-schema-upgrade /usr/bin/spacewalk-schema-upgrade

/usr/bin/uyuni-setup-reportdb remove --db reportdb --user pythia ||:
bash -x /usr/bin/uyuni-setup-reportdb create --db reportdb --user pythia --password oracle --local

echo "Creating First Org"

echo "select create_new_org('Test Default Organization', '$RANDOM') from dual;" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
      'private-channel-family-1', 1);" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -

