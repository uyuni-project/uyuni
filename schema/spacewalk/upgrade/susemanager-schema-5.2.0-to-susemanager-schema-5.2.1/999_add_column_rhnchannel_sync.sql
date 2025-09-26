ALTER TABLE rhnchannel ADD COLUMN IF NOT EXISTS
    auto_sync boolean default true not null;

--- insert new endpoints
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setAutoSync', '/manager/api/channel/software/setAutoSync', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/setAutoSync' AND http_method = 'POST');


INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isAutoSync', '/manager/api/channel/software/isAutoSync', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/isAutoSync' AND http_method = 'GET');

--- insert the new namespaces
INSERT INTO access.namespace (namespace, access_mode, description)
 SELECT 'api.channel.software.setAutoSync', 'W', 'Change channel automatic synchronization flag.'
  WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.setAutoSync' AND access_mode = 'W');

INSERT INTO access.namespace (namespace, access_mode, description)
 SELECT 'api.channel.software.isAutoSync', 'R', 'Get channel automatic synchronization flag status.'
  WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.isAutoSync' AND access_mode = 'R');

--- Link namespaces to endpoints

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.setAutoSync' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setAutoSync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.isAutoSync' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isAutoSync' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

-- assign the namespaces to the channel admin group

INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'channel_admin'
    AND ns.namespace IN (
        'api.channel.software.setAutoSync',
        'api.channel.software.isAutoSync'
    )
    ON CONFLICT DO NOTHING;