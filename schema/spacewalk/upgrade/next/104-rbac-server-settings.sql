-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/config/server-settings', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/config/server-settings' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/server-settings', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/server-settings' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/server-settings', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/server-settings' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/server-settings/reset', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/server-settings/reset' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/config/server-settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/server-settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/server-settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/server-settings/reset' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
