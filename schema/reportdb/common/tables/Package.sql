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

CREATE TABLE Package
(
    mgm_id              NUMERIC NOT NULL,
    package_id          NUMERIC NOT NULL,
    name                VARCHAR(256),
    epoch               VARCHAR(16),
    version             VARCHAR(512),
    release             VARCHAR(512),
    arch                VARCHAR(64),
    type                VARCHAR(10),
    package_size        NUMERIC,
    payload_size        NUMERIC,
    installed_size      NUMERIC,
    vendor              VARCHAR(64),
    organization        VARCHAR(128),
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE Package
  ADD CONSTRAINT Package_pk PRIMARY KEY (mgm_id, package_id);
