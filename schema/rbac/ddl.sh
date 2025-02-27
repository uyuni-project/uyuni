#!/bin/bash
#
# Deploy 'access' schema for RBAC into a container server via mgrctl
# or drop existing schema if '-d' option is provided

if [[ "$@" == *"-d"* ]]; then
  # Remove the schema if '-d' option is used
  echo "DROP SCHEMA access CASCADE;" | mgrctl exec -i -- spacewalk-sql -i \
      && echo "RBAC schema dropped."

else
  # Original operation
  cat ../spacewalk/upgrade/susemanager-schema-5.1.3-to-susemanager-schema-5.1.4/200-rbac.sql ddl.sql \
    | mgrctl exec -i -- spacewalk-sql -i \
    && echo "RBAC schema created."
fi
