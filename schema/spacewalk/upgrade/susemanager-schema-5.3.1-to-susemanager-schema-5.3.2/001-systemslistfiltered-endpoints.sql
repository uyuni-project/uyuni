INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_systems_filtered', 'R', 'List systems using a filter.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_systems_filtered' AND access_mode = 'R');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsFiltered', '/manager/api/system/listSystemsFiltered', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/listSystemsFiltered' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_filtered' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsFiltered' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
