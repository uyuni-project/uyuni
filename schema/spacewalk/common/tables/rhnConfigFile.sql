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


CREATE TABLE rhnConfigFile
(
    id                         NUMERIC NOT NULL
                                   CONSTRAINT rhn_conffile_id_pk PRIMARY KEY
                                   ,
    config_channel_id          NUMERIC NOT NULL
                                   CONSTRAINT rhn_conffile_ccid_fk
                                       REFERENCES rhnConfigChannel (id),
    config_file_name_id        NUMERIC NOT NULL
                                   CONSTRAINT rhn_conffile_cfnid_fk
                                       REFERENCES rhnConfigFileName (id),
    latest_config_revision_id  NUMERIC,
    state_id                   NUMERIC NOT NULL
                                   CONSTRAINT rhn_conffile_sid_fk
                                       REFERENCES rhnConfigFileState (id),
    created                    TIMESTAMPTZ
                                   DEFAULT (current_timestamp) NOT NULL,
    modified                   TIMESTAMPTZ
                                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_conffile_cc_cfn_s_idx
    ON rhnConfigFile (config_channel_id, config_file_name_id, state_id)
    ;

CREATE INDEX rhn_cnf_fl_lcrid_idx
    ON rhnConfigFile (latest_config_revision_id)
    ;

CREATE SEQUENCE rhn_conffile_id_seq;

ALTER TABLE rhnConfigFile
    ADD CONSTRAINT rhn_conffile_ccid_cfnid_uq UNIQUE (config_channel_id, config_file_name_id);

