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

-- endpoint
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.lookupAction', '/manager/api/schedule/lookupAction', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/schedule/lookupAction' AND http_method = 'GET');

-- namespace
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.lookup_action', 'R', 'Lookup details of an action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.lookup_action' AND access_mode = 'R');

-- endpointNamespace
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.lookup_action' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/lookupAction' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
