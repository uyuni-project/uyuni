--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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


CREATE TABLE rhnUserGroup
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_user_group_pk PRIMARY KEY
                         ,
    name             VARCHAR(64) NOT NULL,
    description      VARCHAR(1024) NOT NULL,
    max_members      NUMERIC,
    current_members  NUMERIC
                         DEFAULT (0) NOT NULL,
    group_type       NUMERIC NOT NULL
                         CONSTRAINT rhn_usergroup_type_fk
                             REFERENCES rhnUserGroupType (id),
    org_id           NUMERIC NOT NULL
                         CONSTRAINT rhn_user_group_org_fk
                             REFERENCES web_customer (id)
                             ON DELETE CASCADE,
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_ug_oid_name_uq
    ON rhnUserGroup (org_id, name)
    ;

CREATE INDEX rhn_ug_org_id_type_idx
    ON rhnUserGroup (group_type, id)
    
    ;

ALTER TABLE rhnUserGroup
ADD CONSTRAINT rhn_ug_oid_gt_uq
UNIQUE (org_id, group_type)
;

