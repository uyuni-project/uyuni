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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnServerGroupType
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_servergrouptype_id_pk PRIMARY KEY
                   ,
    label      VARCHAR(32) NOT NULL,
    name       VARCHAR(64) NOT NULL,
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    permanent  CHAR
                   DEFAULT ('Y') NOT NULL
                   CONSTRAINT rhn_servergrouptype_perm_ck
                       CHECK (permanent in ('Y','N')),
    is_base    CHAR
                   DEFAULT ('Y') NOT NULL
                   CONSTRAINT rhn_servergrouptype_isbase_ck
                       CHECK (is_base in ('Y','N'))
)

;

CREATE UNIQUE INDEX rhn_servergrouptype_label_uq
    ON rhnServerGroupType (label)
    ;

CREATE SEQUENCE rhn_servergroup_type_seq;

