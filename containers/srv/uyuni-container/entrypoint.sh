#!/bin/bash

export LC_ALL=en_US.UTF-8

su postgres -c 'pg_ctl start -D /var/lib/pgsql/data -l /tmp/log & >/dev/null 2>&1' && \
# && wait for postgres to start before calling migration
/usr/lib/susemanager/bin/migration.sh -l /var/log/susemanager_setup.log -s
/usr/bin/salt-secrets-config.py &

su - tomcat -c '/usr/lib/tomcat/server start &'
/usr/sbin/taskomatic & > /dev/null 2>&1
/usr/sbin/start_apache2 -k start >/dev/null 2>&1
/usr/bin/salt-api &
/usr/bin/salt-master &
/bin/bash
