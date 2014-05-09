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
--

CREATE TABLE
susePackageEula
(
    package_id        number
                      CONSTRAINT susepackageeula_pkg_id_fk
                      REFERENCES rhnPackage (id)
                      On DELETE CASCADE,
    eula_id           number
                      CONSTRAINT susepackageeula_id_fk
                      REFERENCES suseEulas (id),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX susepackageeula_pkg_eula_uq
ON susePackageEula (package_id, eula_id)
TABLESPACE [[64k_tbs]];

CREATE INDEX susepackageeula_pkg_idx
ON susePackageEula (package_id)
TABLESPACE [[64k_tbs]];

