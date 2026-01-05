-- rename rhnServerGroupType entry
UPDATE rhnServerGroupType SET name = 'Proxy Entitled Servers' WHERE name = 'Proxy' AND label = 'proxy_entitled';

-- rename rhnServerGroup entry (do not rename normal groups named "Proxy" which have NULL group_type)
UPDATE rhnServerGroup SET name = 'Proxy Entitled Servers', description = 'Proxy Entitled Servers'
  WHERE group_type = (SELECT id from rhnServerGroupType WHERE label = 'proxy_entitled');

-- create rhnServerGroup entry if it does not exist
-- this is the case after conflict with normal group named "Proxy" - bsc#1250641
INSERT INTO rhnServerGroup ( id, name, description, group_type, org_id )
SELECT nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, org.id
FROM rhnServerGroupType sgt, web_customer org
WHERE sgt.label = 'proxy_entitled' AND org.id NOT IN (
    SELECT sg.org_id from rhnServerGroup sg
    WHERE sg.name = 'Proxy Entitled Servers'
);
