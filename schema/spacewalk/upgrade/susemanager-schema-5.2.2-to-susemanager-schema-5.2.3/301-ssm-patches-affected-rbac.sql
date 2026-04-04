INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/ssm/ErrataSystemsAffected.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/systems/ssm/ErrataSystemsAffected.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ErrataSystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
