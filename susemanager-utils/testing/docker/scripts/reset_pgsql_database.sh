#!/bin/bash

set -ex

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/:/manager/schema/spacewalk/lib
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset PGSQL database

echo $PATH
echo $PERLLIB

./build-schema.sh
./build-reportdb-schema.sh

export SYSTEMD_NO_WRAP=1
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl stop" ||:
su - postgres -c "/usr/lib/postgresql/bin/pg_ctl start"

cat >>/etc/rhn/rhn.conf <<EOF
db_backend=postgresql
db_user=spacewalk
db_password=spacewalk
db_name=susemanager
db_host=
db_port=
EOF

spacewalk-sql /usr/share/susemanager/db/postgres/main.sql

cat >>/etc/rhn/rhn.conf <<EOF
report_db_backend=postgresql
report_db_user=pythia
report_db_password=oracle
report_db_name=reportdb
report_db_host=
report_db_port=
EOF

# Create symlinks because uyuni-setup-reportdb uses absolute paths
ln -s /manager/schema/spacewalk/spacewalk-sql /usr/bin/spacewalk-sql
ln -s /manager/schema/spacewalk/spacewalk-schema-upgrade /usr/bin/spacewalk-schema-upgrade

/usr/bin/spacewalk-sql --reportdb /usr/share/susemanager/db/reportdb/main.sql

echo "Creating First Org"

echo "select create_new_org('Test Default Organization', '$RANDOM') from dual;" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
      'private-channel-family-1', 1);" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -

