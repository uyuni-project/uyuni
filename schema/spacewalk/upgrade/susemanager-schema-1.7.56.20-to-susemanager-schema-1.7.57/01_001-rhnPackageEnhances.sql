--
-- Copyright (c) 2013 Novell
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
--


CREATE TABLE rhnPackageEnhances
(
    package_id     NUMERIC NOT NULL
                       CONSTRAINT rhn_pkg_enh_package_fk
                           REFERENCES rhnPackage (id)
                           ON DELETE CASCADE,
    capability_id  NUMERIC NOT NULL
                       CONSTRAINT rhn_pkg_enh_capability_fk
                           REFERENCES rhnPackageCapability (id),
    sense          NUMERIC
                       DEFAULT (0) NOT NULL,
    created        timestamptz default (current_timestamp) not null,
    modified       timestamptz default (current_timestamp) not null
)
;

CREATE UNIQUE INDEX rhn_pkg_enh_pid_cid_s_uq
    ON rhnPackageEnhances (package_id, capability_id, sense);

CREATE INDEX rhn_pkg_enh_cid_idx
    ON rhnPackageEnhances (capability_id);

