-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.sync_errata_with_packages', 'W', 'Synchronize errata with their packages into a channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.sync_errata_with_packages' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.syncErrataWithPackages', '/manager/api/channel/software/syncErrataWithPackages', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channel/software/syncErrataWithPackages' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.sync_errata_with_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/syncErrataWithPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'channel_admin'
    AND ns.namespace = 'api.channel.software.sync_errata_with_packages' AND ns.access_mode = 'W'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
