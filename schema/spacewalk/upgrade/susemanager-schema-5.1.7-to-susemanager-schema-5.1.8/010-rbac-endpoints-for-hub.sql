-- endpoints
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/syncChannels', 'POST', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/syncChannels' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/sync/migrate/v1/deleteMaster', 'POST', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/sync/migrate/v1/deleteMaster' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/products/unscoped', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/products/unscoped' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/repositories', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/repositories' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/subscriptions', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/subscriptions' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/orders', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/orders' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/suma/product_tree.json', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/suma/product_tree.json' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/hub-details', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/hub-details' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals/register', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals/register' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals/migrate-from-v1', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals/migrate-from-v1' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals/migrate-from-v2', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals/migrate-from-v2' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/peripherals/:id/sync-channels', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/peripherals/:id/sync-channels' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/hub/access-tokens', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/hub/access-tokens' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id/root-ca', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id/root-ca', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id/credentials', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id/credentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id/sync-channels', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/peripherals/:id/sync-channels', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/:id/root-ca', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/:id/root-ca' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/:id/root-ca', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/:id/root-ca' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/migrate/v1', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/migrate/v1' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/migrate/v2', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/migrate/v2' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/access-tokens', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/access-tokens' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/access-tokens', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/access-tokens' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/access-tokens/:id/validity', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/access-tokens/:id/validity' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/access-tokens/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/access-tokens/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/hub/sync-bunch', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/hub/sync-bunch' AND http_method = 'POST');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.getAllPeripheralChannels', '/manager/api/sync/hub/getAllPeripheralChannels', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/getAllPeripheralChannels' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.getManagerInfo', '/manager/api/sync/hub/getManagerInfo', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/getManagerInfo' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.isISSPeripheral', '/manager/api/sync/hub/isISSPeripheral', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/isISSPeripheral' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.getAllPeripheralOrgs', '/manager/api/sync/hub/getAllPeripheralOrgs', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/getAllPeripheralOrgs' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.migrateFromISSv1', '/manager/api/sync/hub/migrateFromISSv1', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/migrateFromISSv1' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.syncPeripheralChannels', '/manager/api/sync/hub/syncPeripheralChannels', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/syncPeripheralChannels' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.migrateFromISSv2', '/manager/api/sync/hub/migrateFromISSv2', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/migrateFromISSv2' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.removePeripheralChannelsToSync', '/manager/api/sync/hub/removePeripheralChannelsToSync', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/removePeripheralChannelsToSync' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.addPeripheralChannelsToSync', '/manager/api/sync/hub/addPeripheralChannelsToSync', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/addPeripheralChannelsToSync' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.regenerateSCCCredentials', '/manager/api/sync/hub/regenerateSCCCredentials', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/regenerateSCCCredentials' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.listPeripheralChannelsToSync', '/manager/api/sync/hub/listPeripheralChannelsToSync', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/listPeripheralChannelsToSync' AND http_method = 'GET');

-- namespaces
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.hub', 'R', 'Browse Hub Online Synchronization pages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.hub' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'admin.hub', 'W', 'Modify and delete hub and peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'admin.hub' AND access_mode = 'W');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.get_all_peripheral_channels', 'R', 'List all peripheral channels'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.get_all_peripheral_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.get_manager_info', 'R', 'Get Manager Server Details'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.get_manager_info' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.is_iss_peripheral', 'R', 'Return if the server is a peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.is_iss_peripheral' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.get_all_peripheral_orgs', 'R', 'List all Organiazations of the peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.get_all_peripheral_orgs' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.migrate_from_iss_v1', 'W', 'Migrate ISSv1 to Hub environment'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.migrate_from_iss_v1' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.sync_peripheral_channels', 'W', 'Sync channels with the peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.sync_peripheral_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.migrate_from_iss_v2', 'W', 'Migrate ISSv2 to Hub environment'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.migrate_from_iss_v2' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.remove_peripheral_channels_to_sync', 'W', 'Remove channels from synchronization with a peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.remove_peripheral_channels_to_sync' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.add_peripheral_channels_to_sync', 'W', 'Add channels to synchronize with a peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.add_peripheral_channels_to_sync' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.regenerate_scc_credentials', 'W', 'Regenerate SCC credentials for Hub Synchronization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.regenerate_scc_credentials' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.list_peripheral_channels_to_sync', 'R', 'List channels which are configured to be synchronized with a peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.list_peripheral_channels_to_sync' AND access_mode = 'R');

-- endpointNamespace
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/hub-details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/register' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/migrate-from-v1' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/migrate-from-v2' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/admin/hub/access-tokens' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/root-ca' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/credentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/peripherals/:id/sync-channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id/root-ca' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/:id/root-ca' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/migrate/v1' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/migrate/v2' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens/:id/validity' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/access-tokens/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.hub' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/hub/sync-bunch' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_all_peripheral_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getAllPeripheralChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_manager_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getManagerInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.is_iss_peripheral' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/isISSPeripheral' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.get_all_peripheral_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/getAllPeripheralOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.list_peripheral_channels_to_sync' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/hub/listPeripheralChannelsToSync' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.migrate_from_iss_v1' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/migrateFromISSv1' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.sync_peripheral_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/syncPeripheralChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.migrate_from_iss_v2' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/migrateFromISSv2' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.remove_peripheral_channels_to_sync' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/removePeripheralChannelsToSync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.add_peripheral_channels_to_sync' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/addPeripheralChannelsToSync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.regenerate_scc_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/regenerateSCCCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
