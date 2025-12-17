-- Namespaces
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.access', 'R', 'List and detail custom access groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.access' AND access_mode = 'R');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.access', 'W', 'Create, modify and delete custom access groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.access' AND access_mode = 'W');

-- Endpoints
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/access-control', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/access-control' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/access-control/create', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/access-control/create' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/access-control/show-access-group/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/access-control/show-access-group/:id' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/list_custom', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/list_custom' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/list_namespaces', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/list_namespaces' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/organizations/:orgId/users', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/users' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/organizations', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/organizations' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/save', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/save' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/access-control/access-group/delete/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/access-control/access-group/delete/:id' AND http_method = 'DELETE');


-- Map endpoints to namespaces (read)
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/access-control' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/list_custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

-- Map endpoints to namespaces (write)
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/access-control/show-access-group/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/access-control/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/list_namespaces' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/users' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/organizations/:orgId/access-groups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/access-control/access-group/delete/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
