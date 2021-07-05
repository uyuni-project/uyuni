--
-- Copyright (c) 2012 Red Hat, Inc.
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


CREATE TABLE rhnActionScap
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_act_scap_id_pk PRIMARY KEY
                         ,
    action_id        NUMERIC NOT NULL
                         CONSTRAINT rhn_act_scap_act_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    path             VARCHAR(2048) NOT NULL,
    ovalfiles        VARCHAR(8192),
    parameters       BYTEA
)


;

CREATE UNIQUE INDEX rhn_act_scap_aid_idx
    ON rhnActionScap (action_id)
    
    ;

CREATE INDEX rhn_act_scap_path_idx
    ON rhnActionScap (path)
    
    ;

CREATE SEQUENCE rhn_act_scap_id_seq;
