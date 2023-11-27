--
-- Copyright (c) 2014 Red Hat, Inc.
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


CREATE TABLE rhnOrgExtGroupMapping
(
    id        NUMERIC NOT NULL
                  CONSTRAINT rhn_orgextgroupmap_id_pk PRIMARY KEY
                  ,
    ext_group_id       NUMERIC NOT NULL
                  constraint rhn_orgextgroupmap_eg_fk
                  references rhnUserExtGroup(id)
                  on delete cascade,
    server_group_id    NUMERIC NOT NULL
                  constraint rhn_orgextgroupmap_sg_fk
                  references rhnServerGroup(id)
                  on delete cascade,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_orgextgroupmap_es_uq
    ON rhnOrgExtGroupMapping (ext_group_id, server_group_id)
    ;

CREATE SEQUENCE rhn_orgextgroupmap_seq;
