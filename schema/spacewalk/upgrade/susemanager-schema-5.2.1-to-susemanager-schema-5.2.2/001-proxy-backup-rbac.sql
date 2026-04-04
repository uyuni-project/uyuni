INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.backup_configuration', 'W', 'Saves the configuration of a proxy to the server for later conversion'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.backup_configuration' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.backupConfiguration', '/manager/api/proxy/backupConfiguration', 'POST', 'A', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/proxy/backupConfiguration' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.backup_configuration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/backupConfiguration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
