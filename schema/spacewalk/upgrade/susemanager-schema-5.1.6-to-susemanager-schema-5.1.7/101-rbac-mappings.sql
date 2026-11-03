--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

-- Bootstrap SSH
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/systems/bootstrap-ssh', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/systems/bootstrap-ssh' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/bootstrap-ssh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

-- Package list
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/channels/owned', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/channels/owned' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channels/owned' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

-- Image store search dropdown
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/api/cm/imagestores/find', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/cm/imagestores/find' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

-- Sync repos page
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/channels/manage/Sync.do', 'GET', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/channels/manage/Sync.do' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/channels/manage/Sync.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/channels/manage/Sync.do' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Sync.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Sync.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
