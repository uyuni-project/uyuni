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

-- userAccessTable view
-- User access rules on endpoints either permitted directly or through an access group
CREATE VIEW access.userAccessTable AS
WITH endpoints AS (
    SELECT e.*, en.namespace_id
    FROM access.endpoint e
    JOIN access.endpointNamespace en ON e.id = en.endpoint_id
)

-- Select endpoints permitted directly to the user
SELECT e.*, un.user_id
FROM endpoints e
JOIN access.userNamespace un ON e.namespace_id = un.namespace_id

UNION

-- Select endpoints permitted through an access group
SELECT e.*, ug.user_id
FROM endpoints e
JOIN access.accessGroupNamespace gn ON e.namespace_id = gn.namespace_id
JOIN access.userAccessGroup ug ON gn.group_id = ug.group_id
;
