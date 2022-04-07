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
    is_proxy                      BOOLEAN NOT NULL DEFAULT FALSE,
    proxy_system_id               NUMERIC,
    is_mgr_server                 BOOLEAN NOT NULL DEFAULT FALSE,
    organization                  VARCHAR(128),
    hardware                      TEXT,
    machine                       VARCHAR(64),
    rack                          VARCHAR(64),
    room                          VARCHAR(32),
    building                      VARCHAR(128),
    address1                      VARCHAR(128),
    address2                      VARCHAR(128),
    city                          VARCHAR(128),
    state                         VARCHAR(60),
    country                       VARCHAR(2),
    synced_date                   TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE System
  ADD CONSTRAINT System_pk PRIMARY KEY (mgm_id, system_id);

CREATE INDEX System_profile_name_idx
  ON System (profile_name);
