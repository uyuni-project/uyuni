--
-- Copyright (c) 2015 SUSE LLC
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

CREATE TABLE susePackageState
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_pkgstate_id_pk PRIMARY KEY
                         USING INDEX TABLESPACE [[8m_tbs]],
    name_id          NUMBER NOT NULL
                         CONSTRAINT suse_pkgstate_nid_fk
                             REFERENCES rhnPackageName (id),
    evr_id           NUMBER NOT NULL
                         CONSTRAINT suse_pkgstate_eid_fk
                             REFERENCES rhnPackageEVR (id),
    package_arch_id  NUMBER
                         CONSTRAINT suse_pkgstate_paid_fk
                             REFERENCES rhnPackageArch (id),
    state_id         NUMBER NOT NULL,
    package_state_id NUMBER NOT NULL
                         CONSTRAINT suse_pkgstate_psid_fk
                             REFERENCES susePackageStates (id),
    version_constraint_id NUMBER NOT NULL
                         CONSTRAINT suse_pkgstate_vcid_fk
                             REFERENCES suseVersionConstraints (id)
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_pkgstate_id_seq;

ALTER TABLE susePackageState
    ADD CONSTRAINT suse_pkgstate_nid_sid_uq UNIQUE (name_id, state_id);

ALTER TABLE susePackageState
    ADD CONSTRAINT suse_pkgstate_nid_eid_paid_fk FOREIGN KEY (name_id, evr_id, package_arch_id)
    REFERENCES rhnPackageNEVRA (name_id, evr_id, package_arch_id)
        ON DELETE CASCADE;
