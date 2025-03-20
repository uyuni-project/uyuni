INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'activation_key_admin', 'Activation Key Administrator'
FROM web_customer;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'image_admin', 'Image Administrator'
FROM web_customer;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'config_admin', 'Configuration Administrator'
FROM web_customer;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'channel_admin', 'Channel Administrator'
FROM web_customer;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'system_group_admin', 'System Group Administrator'
FROM web_customer;
