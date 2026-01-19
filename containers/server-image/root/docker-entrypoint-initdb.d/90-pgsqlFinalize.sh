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

if [ "${MIGRATION}" = "true" ]; then
  echo "Updating auto-installable distributions..."
  spacewalk-sql --select-mode - <<EOT
  SELECT MIN(CONCAT(org_id, '-', label)) AS target, base_path INTO TEMP TABLE dist_map FROM rhnKickstartableTree GROUP BY base_path;
  UPDATE rhnKickstartableTree SET base_path = CONCAT('/srv/www/distributions/', target)
    from dist_map WHERE dist_map.base_path = rhnKickstartableTree.base_path;
  DROP TABLE dist_map;
EOT

  echo "Schedule a system list update task..."
  spacewalk-sql --select-mode - <<EOT
  insert into rhnTaskQueue (id, org_id, task_name, task_data)
  SELECT nextval('rhn_task_queue_id_seq'), 1, 'update_system_overview', s.id
  from rhnserver s
  where not exists (select 1 from rhntaskorun r join rhntaskotemplate t on r.template_id = t.id
  join rhntaskobunch b on t.bunch_id = b.id where b.name='update-system-overview-bunch' limit 1);
EOT
fi
