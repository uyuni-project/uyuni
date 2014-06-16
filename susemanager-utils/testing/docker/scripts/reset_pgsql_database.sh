#!/bin/bash

set -e

export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/
export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:$PATH

echo Going to reset pgsql database

echo $PATH
echo $PERLLIB

./build-schema.sh

rcpostgresql restart

touch /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf

spacewalk-setup --skip-system-version-test --skip-selinux-test --skip-fqdn-test --skip-gpg-key-import --skip-ssl-cert-generation --skip-ssl-vhost-setup --skip-services-check --clear-db --answer-file=clear-db-answers.txt --non-interactive
