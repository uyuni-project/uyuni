#!/bin/bash

set -e

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset ORACLE database

echo $PATH
echo $PERLLIB

./build-schema.sh

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf

spacewalk-setup --clear-db --db-only --answer-file=clear-db-answers-oracle.txt --external-oracle --non-interactive || {
  cat /var/log/rhn/populate_db.log
  exit 1
}

echo "Creating First Org"

echo "
VARIABLE x NUMBER
call create_new_org('Test Default Organization', '$RANDOM', :x);
" | spacewalk-sql --select-mode -

echo "INSERT INTO  rhnChannelFamily (id, name, label, org_id)
      VALUES (sequence_nextval('rhn_channel_family_id_seq'), 'Private Channel Family 1',
      'private-channel-family-1', 1);" | spacewalk-sql --select-mode -
echo "INSERT INTO  rhnPrivateChannelFamily (channel_family_id, org_id) VALUES  (1000, 1);" | spacewalk-sql --select-mode -
