--
-- Copyright (c) 2010 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--


CREATE TABLE rhnPackageBreaks
(
    package_id     NUMERIC NOT NULL
                       CONSTRAINT rhn_pkg_brks_package_fk
                           REFERENCES rhnPackage (id)
                           ON DELETE CASCADE,
    capability_id  NUMERIC NOT NULL
                       CONSTRAINT rhn_pkg_brks_capability_fk
                           REFERENCES rhnPackageCapability (id),
    sense          NUMERIC
                       DEFAULT (0) NOT NULL,
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_pkg_brks_pid_cid_s_uq
    ON rhnPackageBreaks (package_id, capability_id, sense)
    ;

CREATE INDEX rhn_pkg_brks_cid_idx
    ON rhnPackageBreaks (capability_id)
    
    ;

