INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.password_policy', 'R', 'View password policy configuration and requirements'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.password_policy' AND access_mode = 'R');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.password_policy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/config/password-policy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'home.account.password_policy'
    ON CONFLICT DO NOTHING;
