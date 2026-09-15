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

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/configuration/GlobalConfigChannelList.do', 'POST', 'W', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/configuration/GlobalConfigChannelList.do' AND http_method = 'POST');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/GlobalConfigChannelList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
