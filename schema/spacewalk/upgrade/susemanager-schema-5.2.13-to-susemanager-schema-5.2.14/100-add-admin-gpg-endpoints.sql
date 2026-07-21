INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.gpg.upload_gpg_key', 'W', 'Upload and add a GPG key to the customer keyring.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.gpg.upload_gpg_key' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.admin.gpg.AdminGpgHandler.uploadGpgKey', '/manager/api/admin/gpg/uploadGpgKey', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/gpg/uploadGpgKey' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.gpg.upload_gpg_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/gpg/uploadGpgKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.gpg.list_gpg_keys', 'R', 'List all GPG keys from the customer keyring.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.gpg.list_gpg_keys' AND access_mode = 'R');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.admin.gpg.AdminGpgHandler.listGpgKeys', '/manager/api/admin/gpg/listGpgKeys', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/gpg/listGpgKeys' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.gpg.list_gpg_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/gpg/listGpgKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.gpg.remove_gpg_key', 'W', 'Remove a GPG key from the customer keyring.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.gpg.remove_gpg_key' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.admin.gpg.AdminGpgHandler.removeGpgKey', '/manager/api/admin/gpg/removeGpgKey', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/gpg/removeGpgKey' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.gpg.remove_gpg_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/gpg/removeGpgKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
