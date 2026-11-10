-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_compatible_hotfix_channel_labels_for_advisory', 'R', 'List the hotfix channels a given errata can be cloned into, grouped by content project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_compatible_hotfix_channel_labels_for_advisory' AND access_mode = 'R');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listCompatibleHotfixChannelLabelsForAdvisory', '/manager/api/contentmanagement/listCompatibleHotfixChannelLabelsForAdvisory', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/contentmanagement/listCompatibleHotfixChannelLabelsForAdvisory' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_compatible_hotfix_channel_labels_for_advisory' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listCompatibleHotfixChannelLabelsForAdvisory' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.contentmanagement.list_compatible_hotfix_channel_labels_for_advisory'
    AND ns.access_mode = 'R'
    ON CONFLICT DO NOTHING;
