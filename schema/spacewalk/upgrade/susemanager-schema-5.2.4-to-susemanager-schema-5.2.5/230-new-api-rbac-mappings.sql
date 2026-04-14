-- Insert the endpoint
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listMigrationTargetsWithChannels', '/manager/api/system/listMigrationTargetsWithChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;     

-- Insert the namespace
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_migration_targets_with_channels', 'R', 'Lists the valid migration targets for a given server, including channel details')
    ON CONFLICT (namespace, access_mode) DO NOTHING;    

-- Insert the mapping between namespace and endpoint
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_migration_targets_with_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listMigrationTargetsWithChannels' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;   

-- This is inserting for all the accessgroup even for the custom ones.
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.list_migration_targets_with_channels' AND ns.access_mode = 'R'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;