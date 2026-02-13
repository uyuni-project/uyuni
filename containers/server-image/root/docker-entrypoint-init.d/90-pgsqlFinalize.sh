#!/bin/sh

if [ "${RUN_REINDEX}" = "true" ]; then
  # Reindexing may not be needed for every collation change, but better be on the safe side.
  echo "Reindexing database. This may take a while, please do not cancel it!"
  productdb=$(sed -n "s/^\s*db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)
  reportdb=$(sed -n "s/^\s*report_db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)

  # Replaced bash-only <<< with pipe
  echo "REINDEX DATABASE \"${productdb}\";" | spacewalk-sql --select-mode -

  # After reindexing, alter the collation version
  # Some databases like template0 may not accept changes and that's fine
  set +e
  echo "Refreshing collations"
  echo "ALTER DATABASE ${productdb} REFRESH COLLATION VERSION;" | spacewalk-sql -
  echo "ALTER DATABASE ${reportdb} REFRESH COLLATION VERSION;" | spacewalk-sql --reportdb -
  set -e
fi

if [ "${RUN_SCHEMA_UPDATE}" = "true" ]; then
  echo "Schema update..."
  /usr/sbin/spacewalk-startup-helper check-database
fi
