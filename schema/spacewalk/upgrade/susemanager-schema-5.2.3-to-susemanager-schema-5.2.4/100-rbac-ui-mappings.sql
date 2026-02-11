-- Set POST instead of GET
UPDATE access.endpoint SET http_method = 'POST' WHERE endpoint = '/manager/api/oidcLogin';

-- Rename 'create' to 'create-access-group'
UPDATE access.endpoint SET endpoint = '/manager/admin/access-control/create-access-group' WHERE endpoint = '/manager/admin/access-control/create';

-- Remove leading '/rhn' from endpoints
UPDATE access.endpoint SET endpoint = '/manager/systems/ssm/appstreams' WHERE endpoint = '/rhn/manager/systems/ssm/appstreams';
UPDATE access.endpoint SET endpoint = '/manager/systems/ssm/appstreams/configure/:channelId' WHERE endpoint = '/rhn/manager/systems/ssm/appstreams/configure/:channelId';
UPDATE access.endpoint SET endpoint = '/manager/api/ssm/appstreams/save' WHERE endpoint = '/rhn/manager/api/ssm/appstreams/save';
UPDATE access.endpoint SET endpoint = '/manager/api/system/appstreams/ssmEnable' WHERE endpoint = '/rhn/manager/api/system/appstreams/ssmEnable';
UPDATE access.endpoint SET endpoint = '/manager/api/system/appstreams/ssmDisable' WHERE endpoint = '/rhn/manager/api/system/appstreams/ssmDisable';

-- Add namespaces for API endpoints
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.appstreams.ssm_enable', 'W', 'Schedule enabling of module streams from a given modular channel for SSM'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.appstreams.ssm_enable' AND access_mode = 'W');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.appstreams.ssm_disable', 'W', 'Schedule disabling of module streams from a given modular channel for SSM'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.appstreams.ssm_disable' AND access_mode = 'W');

UPDATE access.endpointNamespace
SET namespace_id = (SELECT id FROM access.namespace WHERE namespace = 'api.system.appstreams.ssm_enable')
WHERE endpoint_id = (SELECT id FROM access.endpoint WHERE endpoint = '/manager/api/system/appstreams/ssmEnable');

UPDATE access.endpointNamespace
SET namespace_id = (SELECT id FROM access.namespace WHERE namespace = 'api.system.appstreams.ssm_disable')
WHERE endpoint_id = (SELECT id FROM access.endpoint WHERE endpoint = '/manager/api/system/appstreams/ssmDisable');

-- Permit to all access groups
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.appstreams.ssm_enable'
    ON CONFLICT DO NOTHING;
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.appstreams.ssm_disable'
    ON CONFLICT DO NOTHING;

-- Add missing hub API endpoint
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT
    'com.redhat.rhn.frontend.xmlrpc.iss.HubHandler.scheduleUpdateTask',
    '/manager/api/sync/hub/scheduleUpdateTask',
    'POST', 'A', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/manager/api/sync/hub/scheduleUpdateTask' AND http_method = 'POST'
);

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.schedule_update_task', 'W', 'Schedules mgr-sync refresh with reposync on peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.schedule_update_task' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.schedule_update_task' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/scheduleUpdateTask' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

