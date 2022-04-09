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

CREATE OR REPLACE VIEW ScapScanResultReport AS
  SELECT XccdScanResult.mgm_id
            , XccdScanResult.scan_id
            , XccdScanResult.rule_id
            , XccdScanResult.idref
            , XccdScanResult.rulesystem
            , System.system_id
            , System.hostname
            , System.organization
            , XccdScanResult.ident
            , XccdScanResult.result
            , XccdScanResult.synced_date
    FROM XccdScanResult
            LEFT JOIN System ON ( XccdScanResult.mgm_id = System.mgm_id AND XccdScanResult.system_id = System.system_id )
ORDER BY mgm_id, scan_id, rule_id
;
