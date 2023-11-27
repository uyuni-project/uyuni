INSERT INTO rhnTaskQueue (org_id, task_name, task_data)
SELECT id, 'upgrade_satellite_refresh_custom_sls_files', 0
FROM web_customer
WHERE id = 1;
