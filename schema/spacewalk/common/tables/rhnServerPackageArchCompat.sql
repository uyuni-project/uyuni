--
-- Copyright (c) 2008--2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnServerPackageArchCompat
(
    server_arch_id   NUMERIC NOT NULL
                         CONSTRAINT rhn_sp_ac_said_fk
                             REFERENCES rhnServerArch (id),
    package_arch_id  NUMERIC NOT NULL
                         CONSTRAINT rhn_sp_ac_paid_fk
                             REFERENCES rhnPackageArch (id),
    preference       NUMERIC NOT NULL,
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_sp_ac_said_paid_pref
    ON rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference)
    ;

CREATE INDEX rhn_sp_ac_paid_pref
    ON rhnServerPackageArchCompat (package_arch_id)
    ;

ALTER TABLE rhnServerPackageArchCompat
    ADD CONSTRAINT rhn_sp_ac_said_paid_uq UNIQUE (server_arch_id, package_arch_id);

ALTER TABLE rhnServerPackageArchCompat
    ADD CONSTRAINT rhn_sp_ac_pref_said_uq UNIQUE (preference, server_arch_id)
    ;

