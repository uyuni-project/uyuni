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

CREATE OR REPLACE VIEW ScapScanReport AS
  SELECT XccdScan.mgm_id
            , XccdScan.scan_id
            , System.system_id
            , XccdScan.action_id
            , System.hostname
            , System.organization
            , SystemNetAddressV4.address AS ip_address
            , XccdScan.name
            , XccdScan.benchmark
            , XccdScan.benchmark_version
            , XccdScan.profile
            , XccdScan.profile_title
            , XccdScan.end_time
            , XccdScan.pass
            , XccdScan.fail
            , XccdScan.error
            , XccdScan.not_selected
            , XccdScan.informational
            , XccdScan.other
            , XccdScan.synced_date
    FROM XccdScan
            LEFT JOIN System ON ( XccdScan.mgm_id = System.mgm_id AND XccdScan.system_id = System.system_id )
            LEFT JOIN SystemNetInterface ON (System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND SystemNetInterface.primary_interface)
            LEFT JOIN SystemNetAddressV4 ON (System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id)
ORDER BY XccdScan.mgm_id
            , XccdScan.scan_id
            , System.system_id
            , XccdScan.end_time
;
