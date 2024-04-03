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

CREATE TABLE suseCoCoAttestationResult
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

CREATE SEQUENCE suse_cocoatt_res_id_seq;

CREATE UNIQUE INDEX suse_cocoatt_res_rid_rt_uq
  ON suseCoCoAttestationResult (report_id, result_type);

CREATE INDEX suse_cocoatt_res_rt_st_idx
  ON suseCoCoAttestationResult (result_type, status);
