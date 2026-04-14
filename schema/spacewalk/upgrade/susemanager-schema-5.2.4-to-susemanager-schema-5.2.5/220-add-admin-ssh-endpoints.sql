INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.ssh.remove_known_host', 'W', 'Remove host from known list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.ssh.remove_known_host' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.admin.ssh.AdminSshHandler.removeKnownHost', '/manager/api/admin/ssh/removeKnownHost', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/ssh/removeKnownHost' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.ssh.remove_known_host' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/ssh/removeKnownHost' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
