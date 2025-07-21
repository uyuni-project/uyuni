INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data)
SELECT nextval('rhn_task_queue_id_seq'), id, 'upgrade_satellite_migrate_cobbler', 0
FROM web_customer
WHERE id = 1;
