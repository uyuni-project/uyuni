#!/bin/bash

# Runs Java tests for Postgres on a SLES11SP3 host

TARGET_HOST=s390vsl106.suse.de # make sure to have passwordless root SSH account
GIT_BRANCH=Manager-s390-junit-tests
DOCKER_BASE_DIR=/manager/susemanager-utils/testing/docker/2.1/suma-2.1-base
DOCKER_PG_DIR=/manager/susemanager-utils/testing/docker/2.1/suma-2.1-pgsql
GITHUB_TOKEN=${GITHUB_TOKEN:-}

ssh root@$TARGET_HOST << EOF
  # copy sources from github
  mkdir /manager
  curl -u $GITHUB_TOKEN:x-oauth-basic -L https://api.github.com/repos/SUSE/spacewalk/tarball/$GIT_BRANCH \
    | tar xz --strip-components 1 --directory /manager

  # mimic suma-2.1-base/Dockerfile
  sh $DOCKER_BASE_DIR/add_repositories.sh
  sh $DOCKER_BASE_DIR/add_packages.sh
  sh $DOCKER_BASE_DIR/create_fake_filesystem_entries.sh
  useradd tomcat
  cp $DOCKER_BASE_DIR/spacewalk-public.cert /usr/share/spacewalk/setup/spacewalk-public.cert
  cp -r $DOCKER_BASE_DIR/gnupg /.gnupg
  cp $DOCKER_BASE_DIR/webapp-keyring.gpg /etc/webapp-keyring.gpg
  export PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/suseRegisterInfo:/manager/client/rhel/rhn-client-tools/src
  export PERLLIB=/manager/spacewalk/setup/lib/:/manager/web/modules/rhn/:/manager/web/modules/pxt/:/manager/schema/spacewalk/lib
  export PATH=/manager/schema/spacewalk/:/manager/spacewalk/setup/bin/:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

  # mimic suma-2.1-pgsql/Dockerfile
  sh $DOCKER_PG_DIR/add_packages.sh
  sh $DOCKER_PG_DIR/setup-db-postgres.sh
  sed -i 's/^max_connections =.*$/max_connections = 20/g' /var/lib/pgsql/data/postgresql.conf
  cp $DOCKER_PG_DIR/rhn.conf /root/rhn.conf

  # run the actual test script
  cd /manager/java/scripts
  sh docker-testing-pgsql.sh | tee test_output.txt
EOF
