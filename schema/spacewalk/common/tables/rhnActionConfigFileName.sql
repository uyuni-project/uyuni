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


CREATE TABLE rhnActionConfigFileName
(
    action_id            NUMERIC NOT NULL,
    server_id            NUMERIC NOT NULL,
    config_file_name_id  NUMERIC NOT NULL
                             CONSTRAINT rhn_actioncf_name_cfnid_fk
                                 REFERENCES rhnConfigFileName (id),
    config_revision_id   NUMERIC
                             CONSTRAINT rhn_actioncf_name_crid_fk
                                 REFERENCES rhnConfigRevision (id)
                                 ON DELETE SET NULL,
    failure_id           NUMERIC
                             CONSTRAINT rhn_actioncf_failure_id_fk
                                 REFERENCES rhnConfigFileFailure (id),
    created              TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_actioncf_name_asc_uq
    ON rhnActionConfigFileName (action_id, server_id, config_file_name_id)
    ;

CREATE INDEX rhn_actioncf_name_sid_idx
    ON rhnActionConfigFileName (server_id)
    ;

CREATE INDEX rhn_act_cnfg_fn_crid_idx
    ON rhnActionConfigFileName (config_revision_id)
    ;

ALTER TABLE rhnActionConfigFileName
    ADD CONSTRAINT rhn_actioncf_name_aid_sid_fk FOREIGN KEY (server_id, action_id)
    REFERENCES rhnServerAction (server_id, action_id)
        ON DELETE CASCADE;

