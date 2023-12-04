INSERT INTO rhnConfiguration (key, description, value, default_value)
SELECT 'system_checkin_threshold', 'Number of days before reporting a system as inactive', null, 1
WHERE NOT EXISTS (SELECT 1 FROM rhnConfiguration WHERE key = 'system_checkin_threshold');

INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data)
SELECT NEXTVAL('rhn_task_queue_id_seq'), id, 'upgrade_satellite_system_threshold_conf', 0
FROM web_customer
WHERE id = 1;
