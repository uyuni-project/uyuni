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

CREATE TABLE IF NOT EXISTS CoCoAttestation
(
    mgm_id              NUMERIC NOT NULL,
    report_id           NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    action_id           NUMERIC NOT NULL,
    environment_type    VARCHAR(120),
    status              VARCHAR(32),
    create_time         TIMESTAMPTZ,
    pass                NUMERIC,
    fail                NUMERIC,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT CoCoAttestation_pk PRIMARY KEY (mgm_id, report_id)
);
