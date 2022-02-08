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

CREATE TABLE Errata
(
    mgm_id              NUMERIC NOT NULL,
    errata_id           NUMERIC NOT NULL,
    advisory_name       VARCHAR(100),
    advisory_type       VARCHAR(32),
    issue_date          TIMESTAMPTZ,
    update_date         TIMESTAMPTZ,
    product             VARCHAR(64),
    cve                 VARCHAR(20),
    synopsis            VARCHAR(4000),
    channel_label       VARCHAR(128),
    organization        VARCHAR(128),
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE Errata
  ADD CONSTRAINT Errata_pk PRIMARY KEY (mgm_id, errata_id);
