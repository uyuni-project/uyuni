-- Bulk Repair Migration for Admin Roles
INSERT INTO access.userAccessGroup (user_id, group_id)
SELECT DISTINCT ugm.user_id, ag.id
FROM rhnUserGroupMembers ugm
JOIN rhnUserGroup ug ON ugm.user_group_id = ug.id
JOIN rhnUserGroupType ugt ON ug.group_type = ugt.id
CROSS JOIN access.accessGroup ag
WHERE ugt.label = 'org_admin'
  AND ag.org_id IS NULL
  AND ag.label IN ('channel_admin', 'config_admin', 'system_group_admin', 'activation_key_admin', 'image_admin', 'regular_user')
  AND NOT EXISTS (
    SELECT 1 FROM access.userAccessGroup
    WHERE user_id = ugm.user_id
      AND group_id = ag.id
);
