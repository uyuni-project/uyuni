INSERT INTO access.namespace (namespace, access_mode, description)
SELECT 'systems.appstreams', 'W', 'Manage AppStreams on systems (SSM)'
WHERE NOT EXISTS (
    SELECT 1 FROM access.namespace
    WHERE namespace = 'systems.appstreams' AND access_mode = 'W'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT '', '/rhn/manager/systems/ssm/appstreams', 'GET', 'W', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/rhn/manager/systems/ssm/appstreams' AND http_method = 'GET'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT '', '/rhn/manager/systems/ssm/appstreams/configure/:channelId', 'GET', 'W', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/rhn/manager/systems/ssm/appstreams/configure/:channelId' AND http_method = 'GET'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT '', '/rhn/manager/api/ssm/appstreams/save', 'POST', 'W', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/rhn/manager/api/ssm/appstreams/save' AND http_method = 'POST'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT
    'com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.ssmEnable',
    '/rhn/manager/api/system/appstreams/ssmEnable',
    'POST', 'A', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/rhn/manager/api/system/appstreams/ssmEnable' AND http_method = 'POST'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT
    'com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.ssmDisable',
    '/rhn/manager/api/system/appstreams/ssmDisable',
    'POST', 'A', True
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/rhn/manager/api/system/appstreams/ssmDisable' AND http_method = 'POST'
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
SELECT ns.id, ep.id
FROM access.namespace ns, access.endpoint ep
WHERE
    ns.namespace = 'systems.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/systems/ssm/appstreams' AND ep.http_method = 'GET'
AND NOT EXISTS (
    SELECT 1 FROM access.endpointNamespace en
    WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
SELECT ns.id, ep.id
FROM access.namespace ns, access.endpoint ep
WHERE
    ns.namespace = 'systems.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/systems/ssm/appstreams/configure/:channelId' AND ep.http_method = 'GET'
AND NOT EXISTS (
    SELECT 1 FROM access.endpointNamespace en
    WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
SELECT ns.id, ep.id
FROM access.namespace ns, access.endpoint ep
WHERE
    ns.namespace = 'systems.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/api/ssm/appstreams/save' AND ep.http_method = 'POST'
AND NOT EXISTS (
    SELECT 1 FROM access.endpointNamespace en
    WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
SELECT ns.id, ep.id
FROM access.namespace ns, access.endpoint ep
WHERE
    ns.namespace = 'system.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/system/api/appstreams/ssmEnable' AND ep.http_method = 'POST'
AND NOT EXISTS (
    SELECT 1 FROM access.endpointNamespace en
    WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
SELECT ns.id, ep.id
FROM access.namespace ns, access.endpoint ep
WHERE
    ns.namespace = 'system.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/rhn/manager/api/system/appstreams/ssmDisable' AND ep.http_method = 'POST'
AND NOT EXISTS (
    SELECT 1 FROM access.endpointNamespace en
    WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
);
