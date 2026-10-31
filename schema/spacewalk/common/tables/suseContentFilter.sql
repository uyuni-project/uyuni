--
-- Copyright (c) 2018 SUSE LLC
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseContentFilter(
    id       NUMERIC NOT NULL
                 CONSTRAINT suse_ct_filter_id_pk PRIMARY KEY,
    org_id   NUMERIC NOT NULL
                 CONSTRAINT suse_ct_project_oid_fk
                     REFERENCES web_customer(id)
                     ON DELETE CASCADE,
    type     VARCHAR(16) NOT NULL,
    rule     VARCHAR(16) NOT NULL,
    name     VARCHAR(128) NOT NULL,
    matcher  VARCHAR(32) NOT NULL,
    field    VARCHAR(32) NOT NULL,
    value    VARCHAR(128),
    created  TIMESTAMPTZ
                 DEFAULT (current_timestamp) NOT NULL,
    modified TIMESTAMPTZ
                 DEFAULT (current_timestamp) NOT NULL

)

;

CREATE SEQUENCE suse_ct_filter_seq;

CREATE UNIQUE INDEX suse_ct_filter_org_name_uq
    ON suseContentFilter(org_id, name);

CREATE INDEX suse_ct_filter_type
    ON suseContentFilter(type);
