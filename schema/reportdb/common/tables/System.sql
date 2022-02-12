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

CREATE TABLE System
(
    mgm_id                        NUMERIC NOT NULL,
    system_id                     NUMERIC NOT NULL,
    profile_name                  VARCHAR(128),
    hostname                      VARCHAR(128),
    minion_id                     VARCHAR(256),
    minion_os_family              VARCHAR(32),
    minion_kernel_live_version    VARCHAR(255),
    machine_id                    VARCHAR(256),
    registered_by                 VARCHAR(64),
    registration_time             TIMESTAMPTZ,
    last_checkin_time             TIMESTAMPTZ,
    kernel_version                VARCHAR(64),
    architecture                  VARCHAR(64),
    organization                  VARCHAR(128),
    hardware                      TEXT,
    synced_date                   TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE System
  ADD CONSTRAINT System_pk PRIMARY KEY (mgm_id, system_id);

CREATE INDEX System_profile_name_idx
  ON System (profile_name);
