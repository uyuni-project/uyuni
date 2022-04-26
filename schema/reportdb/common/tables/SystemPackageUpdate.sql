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

CREATE TABLE SystemPackageUpdate
(
    mgm_id              NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    package_id          NUMERIC NOT NULL,
    name                VARCHAR(256),
    epoch               VARCHAR(16),
    version             VARCHAR(512),
    release             VARCHAR(512),
    arch                VARCHAR(64),
    type                VARCHAR(10),
    is_latest           BOOLEAN NOT NULL DEFAULT FALSE,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemPackageUpdate
  ADD CONSTRAINT SystemPackageUpdate_pk PRIMARY KEY (mgm_id, system_id, package_id);

