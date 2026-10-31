--
-- Copyright (c) 2020 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

CREATE TABLE suseMaintenanceSchedule
(
  id          NUMERIC NOT NULL
              CONSTRAINT suse_mtsched_id_pk PRIMARY KEY,
  org_id      NUMERIC NOT NULL
              CONSTRAINT suse_mtsched_oid_fk
              REFERENCES web_customer(id)
              ON DELETE CASCADE,
  name        VARCHAR(128) NOT NULL,
  sched_type  VARCHAR(10) NOT NULL,
  ical_id     NUMERIC
              CONSTRAINT suse_mtsched_icid_fk
              REFERENCES suseMaintenanceCalendar(id)
              ON DELETE SET NULL,
  created     TIMESTAMPTZ
              DEFAULT (current_timestamp) NOT NULL,
  modified    TIMESTAMPTZ
              DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_mtsched_id_seq;

CREATE UNIQUE INDEX suse_mtsched_oid_name_uq
  ON suseMaintenanceSchedule(org_id, name);

