--
-- Copyright (c) 2021 SUSE LLC
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
    mgm_id		NUMERIC NOT NULL,
    server_id           NUMERIC NOT NULL,
    profile_name        VARCHAR,
    hostname            VARCHAR,
    registered_by       VARCHAR,
    registration_time   TIMESTAMPTZ,
    last_checkin_time   TIMESTAMPTZ,
    kernel_version      VARCHAR,
    organization        VARCHAR,
    machine_id          VARCHAR,
    modified            TIMESTAMPTZ DEFAULT (current_timestamp)
) ;

ALTER TABLE System
  ADD CONSTRAINT sys_mgm_srv_id_pk PRIMARY KEY (mgm_id, server_id);


CREATE INDEX sys_profile_name_idx
  ON System (profile_name);

