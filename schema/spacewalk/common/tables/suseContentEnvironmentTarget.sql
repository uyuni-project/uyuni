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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseContentEnvironmentTarget(
    id NUMBER NOT NULL
        CONSTRAINT suse_ct_env_tgt_id_pk PRIMARY KEY,

    env_id NUMBER
        CONSTRAINT suse_ct_env_tgt_eid_fk
            REFERENCES suseContentEnvironment(id)
            ON DELETE CASCADE,

    type VARCHAR2(16) NOT NULL,

    channel_id NUMBER
        CONSTRAINT suse_ct_env_tgt_chanid_fk
            REFERENCES rhnChannel(id)
            ON DELETE CASCADE,

    config_channel_id NUMBER
        CONSTRAINT suse_ct_env_tgt_conchanid_fk
            REFERENCES rhnConfigChannel(id)
            ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_ct_env_tgt_seq;

CREATE UNIQUE INDEX suse_ct_env_tgt_env_cid_uq
    ON suseContentEnvironmentTarget(env_id, channel_id);

CREATE UNIQUE INDEX suse_ct_env_tgt_env_confcid_uq
    ON suseContentEnvironmentTarget(env_id, config_channel_id);

