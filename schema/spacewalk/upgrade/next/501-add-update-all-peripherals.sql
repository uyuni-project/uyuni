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
-- SPDX-License-Identifier: GPL-2.0-only
--

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.suse.manager.xmlrpc.iss.HubHandler.updateAllPeripherals', '/manager/api/sync/hub/updateAllPeripherals', 'POST', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/sync/hub/updateAllPeripherals' AND http_method = 'POST');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.update_all_peripherals', 'W', 'Update all peripherals with hub GPG key and PQC certificate'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.update_all_peripherals' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.update_all_peripherals' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/updateAllPeripherals' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
