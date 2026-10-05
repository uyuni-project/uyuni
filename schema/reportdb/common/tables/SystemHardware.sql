--
-- Copyright (c) 2025 SUSE LLC
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

CREATE TABLE SystemHardware
(
    mgm_id                        NUMERIC NOT NULL,
    system_id                     NUMERIC NOT NULL,
    machine_id                    VARCHAR(256),
    architecture                  VARCHAR(64),
    cpu_bogomips                  VARCHAR(16),
    cpu_cache                     VARCHAR(16),
    cpu_family                    VARCHAR(32),
    cpu_MHz                       VARCHAR(16),
    cpu_stepping                  VARCHAR(16),
    cpu_flags                     VARCHAR(2048),
    cpu_model                     VARCHAR(64),
    cpu_version                   VARCHAR(32),
    cpu_vendor                    VARCHAR(32),
    nrcpu                         NUMERIC DEFAULT (1),
    nrsocket                      NUMERIC DEFAULT (1),
    nrcore                        NUMERIC DEFAULT (1),
    nrthread                      NUMERIC DEFAULT (1),
    ram                           NUMERIC,
    swap                          NUMERIC,
    vendor                        VARCHAR(256),
    system                        VARCHAR(256),
    product                       VARCHAR(256),
    bios_vendor                   VARCHAR(256),
    bios_version                  VARCHAR(256),
    bios_release                  VARCHAR(256),
    asset                         VARCHAR(256),
    board                         VARCHAR(256),
    synced_date                   TIMESTAMPTZ DEFAULT (current_timestamp)
);

ALTER TABLE SystemHardware
  ADD CONSTRAINT SystemHardware_pk PRIMARY KEY (mgm_id, system_id);
