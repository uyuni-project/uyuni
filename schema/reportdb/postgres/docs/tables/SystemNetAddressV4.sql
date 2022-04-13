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

COMMENT ON TABLE SystemNetAddressV4
  IS 'The list of IPv4 address associated to a system';

COMMENT ON COLUMN SystemNetAddressV4.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemNetAddressV4.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemNetAddressV4.interface_id
  IS 'The id of the network interface';
COMMENT ON COLUMN SystemNetAddressV4.address
  IS 'The IPv4 address of the system';
COMMENT ON COLUMN SystemNetAddressV4.netmask
  IS 'The netmask associated to this address';
COMMENT ON COLUMN SystemNetAddressV4.broadcast
  IS 'The broadcast address associated to the network of this IPv4 host address';
COMMENT ON COLUMN SystemNetAddressV4.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemNetAddressV4
    ADD CONSTRAINT SystemNetAddressV4_interface_fkey FOREIGN KEY (mgm_id, system_id, interface_id) REFERENCES SystemNetInterface(mgm_id, system_id, interface_id);
