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

CREATE TABLE access.endpoint (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    class_method    VARCHAR NOT NULL,
    endpoint        VARCHAR NOT NULL,
    http_method     VARCHAR NOT NULL,
    scope           CHAR(1) NOT NULL
                        CHECK (scope in ('A', 'W')),
    auth_required   BOOLEAN NOT NULL DEFAULT true,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);
COMMENT ON TABLE access.endpoint IS 'Web endpoint mappings for RBAC';

CREATE UNIQUE INDEX endpoint_endpoint_http_method_uq
ON access.endpoint(endpoint, http_method);
