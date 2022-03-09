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

CREATE TABLE SystemNetAddressV4
(
    mgm_id                      NUMERIC NOT NULL,
    system_id                   NUMERIC NOT NULL,
    interface_id                NUMERIC NOT NULL,
    address                     VARCHAR(64) NOT NULL,
    netmask                     VARCHAR(64),
    broadcast                   VARCHAR(64),
    synced_date                 TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemNetAddressV4
  ADD CONSTRAINT SystemNetAddressV4_pk PRIMARY KEY (mgm_id, system_id, interface_id, address);
