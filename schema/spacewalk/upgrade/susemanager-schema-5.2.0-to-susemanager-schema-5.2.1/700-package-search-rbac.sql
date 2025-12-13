INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/software/packages/NameOverview.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/software/packages/NameOverview.do' AND http_method = 'GET');

-- POST is for JSP table controls
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/software/packages/NameOverview.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/software/packages/NameOverview.do' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/NameOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/packages/NameOverview.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
