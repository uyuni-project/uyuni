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


CREATE TABLE rhnServerPackage
(
    server_id        NUMERIC NOT NULL
                         REFERENCES rhnServer (id)
                             ON DELETE CASCADE,
    name_id          NUMERIC NOT NULL
                         REFERENCES rhnPackageName (id),
    evr_id           NUMERIC NOT NULL
                         REFERENCES rhnPackageEVR (id),
    package_arch_id  NUMERIC
                         REFERENCES rhnPackageArch (id),
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    installtime      TIMESTAMPTZ
)


;

CREATE UNIQUE INDEX rhn_sp_snep_uq
    ON rhnServerPackage (server_id, name_id, evr_id, package_arch_id)
    
    ;

CREATE SEQUENCE rhn_server_package_id_seq;

