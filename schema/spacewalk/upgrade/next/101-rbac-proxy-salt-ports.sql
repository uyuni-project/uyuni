INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.get_salt_ports', 'R', 'Get the Salt ports the clients reach the given proxy or the server on')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.set_salt_ports', 'W', 'Set the Salt ports the proxy identified by the given client certificate is exposed on')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.getSaltPorts', '/manager/api/proxy/getSaltPorts', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.setSaltPorts', '/manager/api/proxy/setSaltPorts', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.get_salt_ports' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/getSaltPorts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.set_salt_ports' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/setSaltPorts' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN ('api.proxy.get_salt_ports', 'api.proxy.set_salt_ports')
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
