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

CREATE TABLE access.accessGroupNamespace (
    group_id        BIGINT NOT NULL
                        REFERENCES access.accessGroup(id)
                        ON DELETE CASCADE,
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.accessGroupNamespace IS 'Namespace permissions to access groups';

CREATE UNIQUE INDEX accessGroupNamespace_gid_nid_uq
ON access.accessGroupNamespace(group_id, namespace_id);
