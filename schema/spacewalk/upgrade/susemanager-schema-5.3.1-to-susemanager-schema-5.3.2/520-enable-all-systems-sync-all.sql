-- Ensure existing minions receive the boot_time custom grain after upgrade.
-- Avoid duplicate pending tasks when this migration is reapplied.
INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data)
SELECT nextval('rhn_task_queue_id_seq'), wc.id,
       'upgrade_satellite_all_systems_sync_all', 0
FROM web_customer wc
WHERE wc.id = 1
  AND NOT EXISTS (
      SELECT 1
      FROM rhnTaskQueue tq
      WHERE tq.org_id = wc.id
        AND tq.task_name = 'upgrade_satellite_all_systems_sync_all'
        AND tq.task_data = 0
  );
