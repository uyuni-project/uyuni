INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/systems/details/support', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/systems/details/support' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/systems/:sid/details/uploadSupportData', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/systems/:sid/details/uploadSupportData' AND http_method = 'POST');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.support', 'R', 'Views for generating and uploading the support data'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.support' AND access_mode = 'R');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.support', 'W', 'Schedule the support data action on the system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.support' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.support' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/support' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.support' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/uploadSupportData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'systems.details.support'
    ON CONFLICT DO NOTHING;
