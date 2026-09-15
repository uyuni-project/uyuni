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


CREATE TABLE rhnActionConfigRevision
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_actioncr_id_pk PRIMARY KEY
                            ,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actioncr_aid_fk
                                REFERENCES rhnAction (id)
                                ON DELETE CASCADE,
    server_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actioncr_sid_fk
                                REFERENCES rhnServer (id),
    config_revision_id  NUMERIC NOT NULL
                            CONSTRAINT rhn_actioncr_crid_fk
                                REFERENCES rhnConfigRevision (id)
                                ON DELETE CASCADE,
    failure_id          NUMERIC
                            CONSTRAINT rhn_actioncr_failid_fk
                                REFERENCES rhnConfigFileFailure (id),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_actioncr_aid_sid_crid_uq
    ON rhnActionConfigRevision (action_id, server_id, config_revision_id)
    ;

CREATE INDEX rhn_actioncr_sid_idx
    ON rhnActionConfigRevision (server_id)
    ;

CREATE INDEX rhn_act_cr_crid_idx
    ON rhnActionConfigRevision (config_revision_id)
    ;

CREATE SEQUENCE rhn_actioncr_id_seq;

