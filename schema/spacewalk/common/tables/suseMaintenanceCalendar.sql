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

CREATE TABLE suseMaintenanceCalendar
(
  id          NUMERIC NOT NULL
              CONSTRAINT suse_mtcal_id_pk PRIMARY KEY,
  org_id      NUMERIC NOT NULL
              CONSTRAINT suse_mtcal_oid_fk
              REFERENCES web_customer(id)
              ON DELETE CASCADE,
  label       VARCHAR(128) NOT NULL,
  url         VARCHAR(1024),
  ical        TEXT NOT NULL,
  created     TIMESTAMPTZ
              DEFAULT (current_timestamp) NOT NULL,
  modified    TIMESTAMPTZ
              DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_mtcal_id_seq;

CREATE UNIQUE INDEX suse_mtcal_oid_label_uq
  ON suseMaintenanceCalendar(org_id, label);

