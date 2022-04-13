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

COMMENT ON VIEW InventoryReport
  IS 'List of all registered systems, together with hardware and software information.';

COMMENT ON COLUMN InventoryReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN InventoryReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN InventoryReport.profile_name
  IS 'The unique descriptive name of the system';
COMMENT ON COLUMN InventoryReport.hostname
  IS 'The hostname that identifies this system';
COMMENT ON COLUMN InventoryReport.minion_id
  IS 'The identifier of the minion, if the system is a Salt Minion';
COMMENT ON COLUMN InventoryReport.machine_id
  IS 'The identifier of the machine';
COMMENT ON COLUMN InventoryReport.registered_by
  IS 'The user account who onboarded this system';
COMMENT ON COLUMN InventoryReport.registration_time
  IS 'When this system was onboarded';
COMMENT ON COLUMN InventoryReport.last_checkin_time
  IS 'When this system was visible and reachable last time';
COMMENT ON COLUMN InventoryReport.kernel_version
  IS 'The version of the kernel installed on this system';
COMMENT ON COLUMN InventoryReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN InventoryReport.architecture
  IS 'The architecture of the system';
COMMENT ON COLUMN InventoryReport.hardware
  IS 'A brief description of the hardware specification of this system';
COMMENT ON COLUMN InventoryReport.primary_interface
  IS 'The name of the system primary network interface';
COMMENT ON COLUMN InventoryReport.hardware_address
  IS 'The MAC address of the network interface';
COMMENT ON COLUMN InventoryReport.ip_address
  IS 'The IPv4 address of the primary network interface of the system';
COMMENT ON COLUMN InventoryReport.ip6_addresses
  IS 'The list of IPv6 addresses and their scopes of the primary network interface of the system, separated by ;';
COMMENT ON COLUMN InventoryReport.configuration_channels
  IS 'The list of configuration channels the system is subscribed to, separated by ;';
COMMENT ON COLUMN InventoryReport.entitlements
  IS 'The list of entitlements of the system, separated by ;';
COMMENT ON COLUMN InventoryReport.system_groups
  IS 'The list of groups of the system, separated by ;';
COMMENT ON COLUMN InventoryReport.virtual_host
  IS 'The id of the host of the system, if any';
COMMENT ON COLUMN InventoryReport.is_virtualized
  IS 'True if the system is virtualized';
COMMENT ON COLUMN InventoryReport.virt_type
  IS 'The type of virtualization, if the system is virualized';
COMMENT ON COLUMN InventoryReport.software_channels
  IS 'THe list of software channels the system is subscribed to, separated by ;';
COMMENT ON COLUMN InventoryReport.packages_out_of_date
  IS 'The number of packages installed on the system that can be updated';
COMMENT ON COLUMN InventoryReport.errata_out_of_date
  IS 'The number of patches that can be applied to the system';
COMMENT ON COLUMN InventoryReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
