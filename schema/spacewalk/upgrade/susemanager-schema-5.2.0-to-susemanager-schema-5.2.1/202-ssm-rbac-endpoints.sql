
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/systems/ssm/product-migration', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/systems/ssm/product-migration' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/systems/ssm/product-migration/dry-run/:actionId', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/systems/ssm/product-migration/dry-run/:actionId' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/systems/migration/computeChannels', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/systems/migration/computeChannels' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/systems/migration/schedule', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/systems/migration/schedule' AND http_method = 'POST');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.migration', 'W', 'Perform product migration on systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.migration' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/product-migration' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/product-migration/dry-run/:actionId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/migration/computeChannels' AND http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/migration/schedule' AND http_method = 'POST'
    ON CONFLICT DO NOTHING;
