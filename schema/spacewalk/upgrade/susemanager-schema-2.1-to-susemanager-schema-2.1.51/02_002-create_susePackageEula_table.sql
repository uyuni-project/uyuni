--
-- Copyright (c) 2014 SUSE LINUX Products GmbH, Nuernberg, Germany.
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

CREATE TABLE
susePackageEula
(
    package_id        NUMERIC
                      CONSTRAINT susepackageeula_pkg_id_fk
                      REFERENCES rhnPackage (id)
                      On DELETE CASCADE,
    eula_id           NUMERIC
                      CONSTRAINT susepackageeula_id_fk
                      REFERENCES suseEula (id),
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX susepackageeula_pkg_eula_uq
ON susePackageEula (package_id, eula_id)
;

CREATE INDEX susepackageeula_pkg_idx
ON susePackageEula (package_id)
;

