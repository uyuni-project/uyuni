--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- LDAP Setup Wizard view + API endpoints (admin.config W), mirroring PAYG

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/ldap', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/ldap' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/ldap/create', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/ldap/create' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/admin/setup/ldap/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/admin/setup/ldap/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id', 'PUT', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id' AND http_method = 'PUT');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id', 'DELETE', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id/test-connection', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id/test-connection' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id/test-user-lookup', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id/test-user-lookup' AND http_method = 'POST');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/admin/config/ldap/:id/test-group-resolution', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/admin/config/ldap/:id/test-group-resolution' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/ldap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/ldap/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/admin/setup/ldap/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id/test-connection' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id/test-user-lookup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'admin.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/config/ldap/:id/test-group-resolution' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
