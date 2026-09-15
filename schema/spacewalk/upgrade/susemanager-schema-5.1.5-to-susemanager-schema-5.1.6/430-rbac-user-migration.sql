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

INSERT INTO access.userAccessGroup
SELECT user_id, ag.id
FROM rhnUserGroupMembers m
JOIN rhnUserGroup g ON m.user_group_id = g.id
JOIN rhnUserGroupType t ON g.group_type = t.id
JOIN access.accessGroup ag ON ag.label = t.label
ON CONFLICT DO NOTHING;

INSERT INTO access.userAccessGroup
SELECT wc.id, ag.id
FROM web_contact wc, access.accessGroup ag
WHERE ag.label = 'regular_user'
ON CONFLICT DO NOTHING;
