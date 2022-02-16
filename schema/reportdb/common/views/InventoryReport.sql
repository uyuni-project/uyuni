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

CREATE OR REPLACE VIEW InventoryReport AS
    -- CTEs to group all one to many relationship joining values with ; as separator
    WITH Entitlements AS (
        SELECT mgm_id, system_id, string_agg(system_group_id || ' - ' || name, ';') AS entitlements
          FROM systementitlement
      GROUP BY mgm_id, system_id
    ), Groups AS (
        SELECT mgm_id, system_id, string_agg(system_group_id || ' - ' || name, ';') AS system_groups
          FROM SystemGroup
      GROUP BY mgm_id, system_id
    ), ConfigChannels AS (
        SELECT mgm_id, system_id, string_agg(config_channel_id || ' - ' || name, ';') AS configuration_channels
          FROM SystemConfigChannel
      GROUP BY mgm_id, system_id
    ), Channels AS (
        SELECT mgm_id, system_id, string_agg(channel_id || ' - ' || name, ';') AS software_channels
          FROM SystemChannel
      GROUP BY mgm_id, system_id
    ), V6Addresses AS (
        SELECT mgm_id, system_id, interface_id, string_agg(address || ' (' || scope || ')', ';') AS ip6_addresses
          FROM SystemNetAddressV6
      GROUP BY mgm_id, system_id, interface_id
    )
    SELECT System.mgm_id
              , System.system_id
              , System.profile_name
              , System.hostname
              , System.minion_id
              , System.machine_id
              , System.registered_by
              , System.registration_time
              , System.last_checkin_time
              , System.kernel_version
              , System.organization
              , System.architecture
              , System.hardware
              , SystemNetInterface.name  AS primary_interface
              , SystemNetInterface.hardware_address AS hardware_address
              , SystemNetAddressV4.address AS ip_address
              , V6Addresses.ip6_addresses
              , ConfigChannels.configuration_channels
              , Entitlements.entitlements
              , Groups.system_groups
              , SystemVirtualdata.host_system_id AS virtual_host
              , SystemVirtualdata.virtual_system_id IS NULL AS is_virtualized
              , SystemVirtualdata.instance_type_name AS virt_type
              , Channels.software_channels
              , COALESCE(SystemOutdated.packages_out_of_date, (0)::bigint) AS packages_out_of_date
              , COALESCE(SystemOutdated.errata_out_of_date, (0)::bigint) AS errata_out_of_date
              , System.synced_date
      FROM System
              LEFT JOIN SystemVirtualdata ON ( System.mgm_id = SystemVirtualdata.mgm_id AND System.system_id = SystemVirtualdata.virtual_system_id )
              LEFT JOIN SystemOutdated ON ( System.mgm_id = SystemOutdated.mgm_id AND System.system_id = SystemOutdated.system_id )
              LEFT JOIN SystemNetInterface ON (System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND SystemNetInterface.primary_interface)
              LEFT JOIN SystemNetAddressV4 ON (System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id)
              LEFT JOIN V6Addresses ON (System.mgm_id = V6Addresses.mgm_id AND System.system_id = V6Addresses.system_id AND SystemNetInterface.interface_id = V6Addresses.interface_id)
              LEFT JOIN Entitlements ON ( System.mgm_id = entitlements.mgm_id AND System.system_id = entitlements.system_id )
              LEFT JOIN Groups ON ( System.mgm_id = Groups.mgm_id AND System.system_id = Groups.system_id )
              LEFT JOIN ConfigChannels ON ( System.mgm_id = ConfigChannels.mgm_id AND System.system_id = ConfigChannels.system_id )
              LEFT JOIN Channels ON ( System.mgm_id = Channels.mgm_id AND System.system_id = Channels.system_id )
  ORDER BY System.mgm_id, System.system_id
;
