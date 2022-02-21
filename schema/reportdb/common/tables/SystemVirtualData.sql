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

CREATE TABLE SystemVirtualData
(
    mgm_id                      NUMERIC NOT NULL,
    host_system_id              NUMERIC NULL,
    virtual_system_id           NUMERIC NULL,
    name                        VARCHAR(128),
    instance_type_name          VARCHAR(128),
    vcpus                       NUMERIC,
    memory_size                 NUMERIC,
    uuid                        VARCHAR(128),
    confirmed                   NUMERIC(1,0),
    state_name                  VARCHAR(128),
    synced_date                 TIMESTAMPTZ DEFAULT (current_timestamp)
);

