
DELETE FROM rhnUserExtGroupMapping WHERE int_group_type_id IN (SELECT id FROM rhnusergrouptype WHERE label = 'cluster_admin');
DELETE FROM rhnUserGroupMembers WHERE user_group_id IN (SELECT G.id FROM rhnusergroup G, rhnusergrouptype GT WHERE G.group_type = GT.id AND GT.label = 'cluster_admin');
DELETE FROM rhnUserGroup WHERE group_type IN (SELECT id FROM rhnusergrouptype WHERE label = 'cluster_admin');
DELETE FROM rhnUserGroupType WHERE label = 'cluster_admin';
