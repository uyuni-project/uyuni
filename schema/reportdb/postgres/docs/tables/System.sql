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

COMMENT ON TABLE System
  IS 'The list of systems managed by a SUSE Manager instance';

COMMENT ON COLUMN System.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN System.system_id
  IS 'The id of the system';
COMMENT ON COLUMN System.profile_name
  IS 'The unique descriptive name of the system';
COMMENT ON COLUMN System.hostname
  IS 'The hostname that identifies this system';
COMMENT ON COLUMN System.minion_id
  IS 'The identifier of the minion, if the system is a Salt Minion';
COMMENT ON COLUMN System.minion_os_family
  IS 'The family of the operating system, if the system is a Salt Minion';
COMMENT ON COLUMN System.minion_kernel_live_version
  IS 'The current live kernel version, if the system is a Salt Minion';
COMMENT ON COLUMN System.machine_id
  IS 'The identifier of the machine';
COMMENT ON COLUMN System.registered_by
  IS 'The user account who onboarded this system';
COMMENT ON COLUMN System.registration_time
  IS 'When this system was onboarded';
COMMENT ON COLUMN System.last_checkin_time
  IS 'When this system was visible and reachable last time';
COMMENT ON COLUMN System.kernel_version
  IS 'The version of the kernel installed on this system';
COMMENT ON COLUMN System.architecture
  IS 'The architecture of the system';
COMMENT ON COLUMN System.is_proxy
  IS 'True if this system is a SUSE Manager Proxy instance';
COMMENT ON COLUMN System.proxy_system_id
  IS 'The id of the SUSE Manager proxy that this system is connected to, if any';
COMMENT ON COLUMN System.is_mgr_server
  IS 'True of this system is a SUSE Manager instance';
COMMENT ON COLUMN System.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN System.hardware
  IS 'A brief description of the hardware specification of this system';
COMMENT ON COLUMN System.machine
  IS 'The machine on which this system is located';
COMMENT ON COLUMN System.rack
  IS 'The rack on which this system is located';
COMMENT ON COLUMN System.room
  IS 'The room where this system is located';
COMMENT ON COLUMN System.building
  IS 'The building where this system is located';
COMMENT ON COLUMN System.address1
  IS 'The first row of the address where this system is located';
COMMENT ON COLUMN System.address2
  IS 'The second row of the address where this system is located';
COMMENT ON COLUMN System.city
  IS 'The city where this system is located';
COMMENT ON COLUMN System.state
  IS 'The state where this system is located';
COMMENT ON COLUMN System.country
  IS 'The country where this system is located';
COMMENT ON COLUMN System.synced_date
  IS 'The timestamp of when this data was last refreshed.';
