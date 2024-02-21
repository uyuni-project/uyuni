--
-- Copyright (c) 2024 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE IF NOT EXISTS suseServerCoCoAttestationConfig
(
        id        NUMERIC NOT NULL
                    CONSTRAINT suse_srvcocoatt_cnf_id_pk PRIMARY KEY,
        server_id NUMERIC NOT NULL
                    CONSTRAINT suse_srvcocoatt_cnf_sid_fk
                      REFERENCES rhnServer (id) ON DELETE CASCADE,
        enabled   BOOLEAN NOT NULL DEFAULT FALSE,
        env_type  NUMERIC NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_srvcocoatt_cnf_sid_uq
    ON suseServerCoCoAttestationConfig (server_id);

CREATE SEQUENCE IF NOT EXISTS suse_srvcocoatt_cnf_id_seq;


CREATE TABLE IF NOT EXISTS suseServerCoCoAttestationReport
(
        id          NUMERIC     NOT NULL
                      CONSTRAINT suse_srvcocoatt_rep_id_pk PRIMARY KEY,
        server_id   NUMERIC     NOT NULL
                      CONSTRAINT suse_srvcocoatt_rep_sid_fk REFERENCES rhnServer (id)
                      ON DELETE CASCADE,
        action_id   NUMERIC     NULL
                      CONSTRAINT suse_srvcocoatt_rep_aid_fk REFERENCES rhnAction (id)
                      ON DELETE SET NULL,
        env_type    NUMERIC     NOT NULL,
        status      VARCHAR(32) NOT NULL
                      CONSTRAINT suse_srvcocoatt_rep_st_ck
                        CHECK(status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
        in_data     JSONB NOT NULL, -- input data for the state.apply
        out_data    JSONB NOT NULL, -- output data from the state.apply
        created     TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
        modified    TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);


CREATE SEQUENCE IF NOT EXISTS suse_srvcocoatt_rep_id_seq;

CREATE INDEX IF NOT EXISTS suse_srvcocoatt_rep_sid_idx
  ON suseServerCoCoAttestationReport (server_id);

CREATE INDEX IF NOT EXISTS suse_srvcocoatt_rep_stenv_idx
  ON suseServerCoCoAttestationReport (status, env_type);

CREATE TABLE IF NOT EXISTS suseCoCoAttestationResult
(
        id          NUMERIC NOT NULL
                      CONSTRAINT suse_cocoatt_res_id_pk PRIMARY KEY,
        report_id   NUMERIC NOT NULL
                      CONSTRAINT suse_cocoatt_res_rid_fk
                        REFERENCES suseServerCoCoAttestationReport (id)
                        ON DELETE CASCADE,
        result_type NUMERIC     NOT NULL,
        status      VARCHAR(32) NOT NULL
                      CONSTRAINT suse_cocoatt_res_st_ck
                        CHECK(status IN ('PENDING', 'SUCCEEDED', 'FAILED')),
        description VARCHAR(256) NOT NULL,
        details     TEXT NULL,
        attested    TIMESTAMPTZ NULL
);

CREATE SEQUENCE IF NOT EXISTS suse_cocoatt_res_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_cocoatt_res_rid_rt_uq
  ON suseCoCoAttestationResult (report_id, result_type);

CREATE INDEX IF NOT EXISTS suse_cocoatt_res_rt_st_idx
  ON suseCoCoAttestationResult (result_type, status);
