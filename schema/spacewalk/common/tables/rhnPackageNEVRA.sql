--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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


CREATE TABLE rhnPackageNEVRA
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_pkgnevra_id_pk PRIMARY KEY
                         ,
    name_id          NUMERIC NOT NULL
                         CONSTRAINT rhn_pkgnevra_nid_fk
                             REFERENCES rhnPackageName (id),
    evr_id           NUMERIC NOT NULL
                         CONSTRAINT rhn_pkgnevra_eid_fk
                             REFERENCES rhnPackageEVR (id),
    package_arch_id  NUMERIC
                         CONSTRAINT rhn_pkgnevra_paid_fk
                             REFERENCES rhnPackageArch (id)
)

;

CREATE SEQUENCE rhn_pkgnevra_id_seq;

ALTER TABLE rhnPackageNEVRA
    ADD CONSTRAINT rhn_pkgnevra_nid_eid_paid_uq UNIQUE (name_id, evr_id, package_arch_id);

