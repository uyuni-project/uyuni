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

CREATE OR REPLACE VIEW ErrataSystemsReport AS
  WITH V6Addresses AS (
          SELECT mgm_id, system_id, interface_id, string_agg(address || ' (' || scope || ')', ';') AS ip6_addresses
            FROM SystemNetAddressV6
        GROUP BY mgm_id, system_id, interface_id
  )
  SELECT SystemErrata.mgm_id
              , SystemErrata.errata_id
              , SystemErrata.advisory_name
              , SystemErrata.system_id
              , System.profile_name
              , System.hostname
              , SystemNetAddressV4.address AS ip_address
              , V6Addresses.ip6_addresses
              , SystemErrata.synced_date
    FROM SystemErrata
            INNER JOIN System ON ( SystemErrata.mgm_id = System.mgm_id AND SystemErrata.system_id = System.system_id )
            LEFT JOIN SystemNetInterface ON ( System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND primary_interface )
            LEFT JOIN SystemNetAddressV4 ON ( System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id )
            LEFT JOIN V6Addresses ON ( System.mgm_id = V6Addresses.mgm_id AND System.system_id = V6Addresses.system_id AND SystemNetInterface.interface_id = V6Addresses.interface_id )
ORDER BY SystemErrata.mgm_id, SystemErrata.errata_id, SystemErrata.system_id
