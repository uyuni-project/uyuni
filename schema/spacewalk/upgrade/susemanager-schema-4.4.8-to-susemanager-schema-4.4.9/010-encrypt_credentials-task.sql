INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data)
SELECT sequence_nextval('rhn_task_queue_id_seq'), id, 'upgrade_satellite_encrypt_credentials', 0
FROM web_customer
WHERE id = 1;
