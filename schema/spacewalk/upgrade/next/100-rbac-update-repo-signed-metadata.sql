-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.update_repo_signed_metadata', 'W', 'Updates whether a repository has signed metadata')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoSignedMetadata', '/manager/api/channel/software/updateRepoSignedMetadata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_signed_metadata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoSignedMetadata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

-- Like the other repository update endpoints, permit to all access groups
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.channel.software.update_repo_signed_metadata' AND ns.access_mode = 'W'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
