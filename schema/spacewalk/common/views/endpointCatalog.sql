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

-- endpointCatalog view
-- Convenience view for browsing endpoints and namespaces
CREATE VIEW access.endpointCatalog AS
SELECT n.namespace, n.access_mode, e.endpoint, e.http_method, e.scope
FROM access.endpoint e
JOIN access.endpointNamespace en ON e.id = en.endpoint_id
JOIN access.namespace n ON en.namespace_id = n.id;
