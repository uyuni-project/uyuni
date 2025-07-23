INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.generateEnvironmentDifferences',
           '/manager/api/contentmanagement/generateEnvironmentDifferences', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/generateEnvironmentDifferences' AND http_method = 'POST');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.generate_environment_difference', 'W', 'Generate the difference between CLM environments.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.generate_environment_difference' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.generate_environment_difference' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/generateEnvironmentDifferences' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.generate_environment_difference'
    ON CONFLICT DO NOTHING;
