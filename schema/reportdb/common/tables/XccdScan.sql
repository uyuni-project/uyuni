--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE XccdScan
(
    mgm_id              NUMERIC NOT NULL,
    scan_id             NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    action_id           NUMERIC NOT NULL,
    name                VARCHAR(120),
    benchmark           VARCHAR(120),
    benchmark_version   VARCHAR(80),
    profile             VARCHAR(120),
    profile_title       VARCHAR(120),
    end_time            TIMESTAMPTZ,
    pass                NUMERIC,
    fail                NUMERIC,
    error               NUMERIC,
    not_selected        NUMERIC,
    informational       NUMERIC,
    other               NUMERIC,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE XccdScan
  ADD CONSTRAINT XccdScan_pk PRIMARY KEY (mgm_id, scan_id);
