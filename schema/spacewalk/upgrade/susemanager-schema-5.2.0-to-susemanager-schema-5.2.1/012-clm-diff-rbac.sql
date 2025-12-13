INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.generateProjectDifference',
           '/manager/api/contentmanagement/generateProjectDifference', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/generateProjectDifference' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.generateEnvironmentDifference',
           '/manager/api/contentmanagement/generateEnvironmentDifference', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/generateEnvironmentDifference' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listEnvironmentDifference',
           '/manager/api/contentmanagement/listEnvironmentDifference', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/listEnvironmentDifference' AND http_method = 'GET');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.generate_project_difference', 'W', 'Generate the difference for a CLM project.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.generate_project_difference' AND access_mode = 'W');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.generate_environment_difference', 'W', 'Generate the difference between CLM environments.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.generate_environment_difference' AND access_mode = 'W');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_environment_difference', 'R', 'List the difference of a Project Environment compared to its original'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_environment_difference' AND access_mode = 'R');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.generate_project_difference' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/generateProjectDifference' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.generate_environment_difference' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/generateEnvironmentDifference' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_environment_difference' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listEnvironmentDifference' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.generate_project_difference'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.generate_environment_difference'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.list_environment_difference'
    ON CONFLICT DO NOTHING;
