--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

CREATE OR REPLACE VIEW SystemHardwareReport AS
    -- CTEs to group all one to many relationship joining values with ; as separator
    WITH V6Addresses AS (
        SELECT mgm_id, system_id, interface_id, string_agg(address || ' (' || scope || ')', ';') AS ip6_addresses
          FROM SystemNetAddressV6
      GROUP BY mgm_id, system_id, interface_id
    )
    SELECT System.mgm_id
              , System.system_id
              , System.profile_name
              , System.hostname
              , SystemHardware.machine_id
              , SystemHardware.architecture
              , SystemHardware.nrcpu AS cpus
              , SystemHardware.nrsocket AS sockets
              , SystemHardware.nrcore AS cores
              , SystemHardware.nrthread AS threads
              , SystemHardware.cpu_bogomips
              , SystemHardware.cpu_cache
              , SystemHardware.cpu_family
              , SystemHardware.cpu_MHz
              , SystemHardware.cpu_stepping
              , SystemHardware.cpu_flags
              , SystemHardware.cpu_model
              , SystemHardware.cpu_version
              , SystemHardware.cpu_vendor
              , SystemHardware.ram AS memory_size
              , SystemHardware.swap AS swap_size
              , SystemHardware.vendor
              , SystemHardware.system
              , SystemHardware.product
              , SystemHardware.bios_vendor
              , SystemHardware.bios_version
              , SystemHardware.bios_release
              , SystemHardware.asset
              , SystemHardware.board
              , SystemNetInterface.name  AS primary_interface
              , SystemNetInterface.hardware_address AS hardware_address
              , SystemNetAddressV4.address AS ip_address
              , V6Addresses.ip6_addresses
              , SystemVirtualdata.virtual_system_id IS NOT NULL AS is_virtualized
              , SystemHardware.synced_date
      FROM System
              LEFT JOIN SystemHardware ON ( System.mgm_id = SystemHardware.mgm_id AND System.system_id = SystemHardware.system_id)
              LEFT JOIN SystemVirtualdata ON ( System.mgm_id = SystemVirtualdata.mgm_id AND System.system_id = SystemVirtualdata.virtual_system_id )
              LEFT JOIN SystemNetInterface ON (System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND SystemNetInterface.primary_interface)
              LEFT JOIN SystemNetAddressV4 ON (System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id)
              LEFT JOIN V6Addresses ON (System.mgm_id = V6Addresses.mgm_id AND System.system_id = V6Addresses.system_id AND SystemNetInterface.interface_id = V6Addresses.interface_id)
  ORDER BY System.mgm_id, System.system_id
;
