--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

CREATE TABLE suseContentEnvironmentDiff(
    id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    project_id		NUMERIC NOT NULL
                            CONSTRAINT suse_ced_pid_fk
                                REFERENCES suseContentProject(id)
                                ON DELETE CASCADE,
    env_id              NUMERIC NOT NULL
                            CONSTRAINT suse_ct_env_nid_fk
                                REFERENCES suseContentEnvironment(id)
                                ON DELETE CASCADE,
    channel_id		NUMERIC NOT NULL
                            CONSTRAINT suse_ct_env_cid_fk
			        REFERENCES rhnChannel(id)
				ON DELETE CASCADE,
    diff_action         VARCHAR(8) NOT NULL, -- + or - or x
    entry_id            NUMERIC NOT NULL,
    entry_type          VARCHAR(16) NOT NULL, -- PACKAGE, ERRATA
    entry_name          VARCHAR(256) NOT NULL,
    entry_description   VARCHAR NOT NULL,
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_ced_pid_evid_cid_eid_et_uq UNIQUE(project_id, env_id, channel_id, entry_id, entry_type)
);

CREATE INDEX suse_ced_tid_idx
    ON suseContentEnvironmentDiff(entry_type, entry_id);

CREATE INDEX suse_ced_ndt_idx
    ON suseContentEnvironmentDiff(entry_name, entry_description, entry_type);

CREATE INDEX suse_ced_cidta_idx
    ON suseContentEnvironmentDiff(channel_id, entry_type, diff_action);
