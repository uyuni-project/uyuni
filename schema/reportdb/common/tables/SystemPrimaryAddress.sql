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

CREATE TABLE SystemPrimaryAddress
(
    mgm_id                  NUMERIC NOT NULL,
    system_id               NUMERIC NOT NULL,
    ip4_addr                VARCHAR(64),
    ip4_netmask             VARCHAR(64),
    ip4_broadcast           VARCHAR(64),
    ip6_addr                VARCHAR(45),
    ip6_netmask             VARCHAR(49),
    ip6_scope               VARCHAR(64),
    synced_date             TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemPrimaryAddress
  ADD CONSTRAINT SystemPrimaryAddress_pk PRIMARY KEY (mgm_id, system_id);
