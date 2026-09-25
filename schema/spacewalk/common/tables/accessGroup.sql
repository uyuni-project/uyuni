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

CREATE TABLE access.accessGroup (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    org_id          BIGINT REFERENCES web_customer(id)
                        ON DELETE CASCADE,
    label           VARCHAR NOT NULL,
    description     VARCHAR NOT NULL,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);
COMMENT ON TABLE access.accessGroup IS 'Access groups (roles) for RBAC';

CREATE UNIQUE INDEX access_group_org_label_uq
ON access.accessGroup(org_id, label);
