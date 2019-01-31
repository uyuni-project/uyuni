#!/bin/bash

set -ex

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset PGSQL database

echo $PATH
echo $PERLLIB

./build-schema.sh

export SYSTEMD_NO_WRAP=1
sysctl -w kernel.shmmax=18446744073709551615
su - postgres -c "/usr/lib/postgresql10/bin/pg_ctl stop" ||:
su - postgres -c "/usr/lib/postgresql10/bin/pg_ctl start"

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf
touch /etc/rhn/rhn.conf

spacewalk-setup --clear-db --db-only --answer-file=clear-db-answers-pgsql.txt --external-postgresql --non-interactive || {
  cat /var/log/rhn/populate_db.log
  exit 1
}

echo "Creating First Org"

echo "select create_new_org('Test Default Organization', '$RANDOM') from dual;" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
      'private-channel-family-1', 1);" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -

