--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE IF NOT EXISTS CoCoAttestationResult
(
    mgm_id              NUMERIC NOT NULL,
    report_id           NUMERIC NOT NULL,
    result_type_id      NUMERIC NOT NULL,
    result_type         VARCHAR(128),
    result_status       VARCHAR(32),
    description         VARCHAR(256),
    details             TEXT,
    attestation_time    TIMESTAMPTZ,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT CoCoAttestationResult_pk PRIMARY KEY (mgm_id, report_id, result_type_id)
);
