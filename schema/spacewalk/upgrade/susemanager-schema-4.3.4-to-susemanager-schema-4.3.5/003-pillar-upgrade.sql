INSERT INTO rhnTaskQueue (org_id, task_name, task_data)
SELECT id, 'upgrade_satellite_pillars_from_files', 0
FROM web_customer
WHERE id = 1;

