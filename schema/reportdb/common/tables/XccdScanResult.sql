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

CREATE TABLE XccdScanResult
(
    mgm_id              NUMERIC NOT NULL,
    scan_id             NUMERIC NOT NULL,
    rule_id             NUMERIC NOT NULL,
    idref               VARCHAR(255),
    rulesystem          VARCHAR(80),
    system_id           NUMERIC NOT NULL,
    ident               VARCHAR(255),
    result              VARCHAR(16),
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE XccdScanResult
  ADD CONSTRAINT XccdScanResult_pk PRIMARY KEY (mgm_id, scan_id, rule_id);
