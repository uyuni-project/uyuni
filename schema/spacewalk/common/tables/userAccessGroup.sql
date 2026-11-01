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

CREATE TABLE access.userAccessGroup (
    user_id         NUMERIC NOT NULL
                        REFERENCES public.web_contact(id)
                        ON DELETE CASCADE,
    group_id        BIGINT NOT NULL
                        REFERENCES access.accessGroup(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.userAccessGroup IS 'User access group memberships';

CREATE UNIQUE INDEX userAccessGroup_uid_gid_uq
ON access.userAccessGroup(user_id, group_id);
