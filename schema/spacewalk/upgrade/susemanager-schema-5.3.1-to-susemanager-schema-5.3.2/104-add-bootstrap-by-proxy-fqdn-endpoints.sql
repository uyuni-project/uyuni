INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.bootstrap_by_proxy_fqdn', 'W', 'Bootstrap a system for management via either Salt or Salt SSH using a proxy FQDN.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.bootstrap_by_proxy_fqdn' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrapByProxyFqdn', '/manager/api/system/bootstrapByProxyFqdn', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/bootstrapByProxyFqdn' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap_by_proxy_fqdn' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrapByProxyFqdn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.bootstrap_with_private_ssh_key_by_proxy_fqdn', 'W', 'Bootstrap a system for management via either Salt or Salt SSH with a private SSH key using a proxy FQDN.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.bootstrap_with_private_ssh_key_by_proxy_fqdn' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrapWithPrivateSshKeyByProxyFqdn', '/manager/api/system/bootstrapWithPrivateSshKeyByProxyFqdn', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/bootstrapWithPrivateSshKeyByProxyFqdn' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap_with_private_ssh_key_by_proxy_fqdn' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrapWithPrivateSshKeyByProxyFqdn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
