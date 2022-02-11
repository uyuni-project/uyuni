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
 SELECT System.system_id,
            System.profile_name,
            System.hostname,
            System.minion_id,
            btrim((SystemPrimaryAddress.ip4_addr)::text) AS ip_address,
            btrim((SystemPrimaryAddress.ip6_addr)::text) AS ipv6_address,
            System.machine_id,
            System.registered_by,
            System.registration_time,
            System.last_checkin_time,
            System.kernel_version,
            SystemConfigChannel.name AS configuration_channel,
            SystemConfigChannel.config_channel_id AS configuration_channel_id,
            SystemEntitlement.name AS entitlements,
            SystemEntitlement.system_group_id AS entitlements_system_group_id,
            SystemGroup.name AS system_group,
            SystemGroup.system_group_id AS system_group_system_group_id,
            System.organization,
            SystemVirtualdata.host_system_id AS virtual_host,
            System.architecture,
                CASE
                    WHEN (SystemVirtualdata.virtual_system_id IS NULL) THEN 'No'::text
                    ELSE 'Yes'::text
                END AS is_virtualized,
            SystemVirtualdata.instance_type_name AS virt_type,
            System.hardware,
            SystemChannel.name AS software_channel,
            SystemChannel.parent_channel_id AS parent_channel,
            SystemChannel.channel_id,
            COALESCE(SystemOutdated.packages_out_of_date, (0)::bigint) AS packages_out_of_date,
            COALESCE(SystemOutdated.errata_out_of_date, (0)::bigint) AS errata_out_of_date,
            System.synced_date
   FROM System
            LEFT JOIN SystemPrimaryaddress ON ( System.mgm_id = SystemPrimaryAddress.mgm_id AND System.system_id = SystemPrimaryAddress.system_id )
            LEFT JOIN SystemEntitlement ON ( System.mgm_id = SystemEntitlement.mgm_id AND System.system_id = SystemEntitlement.system_id )
            LEFT JOIN SystemGroup ON ( System.mgm_id = SystemGroup.mgm_id AND System.system_id = SystemGroup.system_id )
            LEFT JOIN SystemVirtualdata ON ( System.mgm_id = SystemVirtualdata.mgm_id AND System.system_id = SystemVirtualdata.virtual_system_id )
            LEFT JOIN SystemConfigChannel ON ( System.mgm_id = SystemConfigChannel.mgm_id AND System.system_id = SystemConfigChannel.system_id )
            LEFT JOIN SystemChannel ON ( System.mgm_id = SystemChannel.mgm_id AND System.system_id = SystemChannel.system_id )
            LEFT JOIN SystemOutdated ON ( System.mgm_id = SystemOutdated.mgm_id AND System.system_id = SystemOutdated.system_id )
  ORDER BY System.system_id
           , SystemChannel.parent_channel_id NULLS FIRST
           , SystemChannel.channel_id
           , SystemConfigChannel.config_channel_id
           , SystemEntitlement.system_group_id
           , SystemGroup.system_group_id
           , SystemVirtualdata.host_system_id
;
