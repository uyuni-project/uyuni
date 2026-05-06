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

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'salt.remote_commands', 'W', 'Execute remote commands on systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'salt.remote_commands' AND access_mode = 'W');

DELETE FROM access.endpointNamespace
WHERE
    namespace_id = (SELECT id FROM access.namespace WHERE namespace = 'salt.keys' AND access_mode = 'W') AND
    endpoint_id = (SELECT id FROM access.endpoint WHERE endpoint = '/manager/systems/cmd' AND http_method = 'GET');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/cmd' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

-- Permit to all access groups
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'salt.remote_commands'
    AND ns.access_mode = 'W'
    ON CONFLICT DO NOTHING;
