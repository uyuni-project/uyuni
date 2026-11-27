-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.clone_errata_into_hotfix_channel', 'W', 'Clone an errata into a hotfix channel, together with their packages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.clone_errata_into_hotfix_channel' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.cloneErrataIntoHotfixChannel', '/manager/api/contentmanagement/cloneErrataIntoHotfixChannel', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/cloneErrataIntoHotfixChannel' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.clone_errata_into_hotfix_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/cloneErrataIntoHotfixChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.clone_errata_into_hotfix_channel'
    AND ns.access_mode = 'W'
    ON CONFLICT DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.clone_errata_into_hotfix_channels', 'W', 'Clone an errata into multiple hotfix channels, together with their packages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.clone_errata_into_hotfix_channels' AND access_mode = 'W');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.cloneErrataIntoHotfixChannels', '/manager/api/contentmanagement/cloneErrataIntoHotfixChannels', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/cloneErrataIntoHotfixChannels' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.clone_errata_into_hotfix_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/cloneErrataIntoHotfixChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.clone_errata_into_hotfix_channels'
    AND ns.access_mode = 'W'
    ON CONFLICT DO NOTHING;
