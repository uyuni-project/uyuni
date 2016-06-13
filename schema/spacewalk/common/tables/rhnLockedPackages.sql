--
-- Copyright (c) 2013 SUSE.
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


CREATE TABLE rhnLockedPackages
(
    pkg_id           NUMBER NOT NULL
                         REFERENCES rhnPackage (id)
                             ON DELETE CASCADE,
    server_id        NUMBER NOT NULL
                         REFERENCES rhnServer (id)
                             ON DELETE CASCADE,
    name_id          NUMBER NOT NULL
                         REFERENCES rhnPackageName (id),
    evr_id           NUMBER NOT NULL
                         REFERENCES rhnPackageEVR (id),
    arch_id          NUMBER
                         REFERENCES rhnPackageArch (id),
    pending          CHAR(1) DEFAULT ('L')
                         CONSTRAINT rhn_lockedpackages_pending_ck
                           CHECK (pending in ('L', 'U'))
)
ENABLE ROW MOVEMENT
;

CREATE INDEX rhn_lp_pkg_id_idx
    ON rhnLockedPackages (pkg_id)
    TABLESPACE [[128m_tbs]]
    NOLOGGING;
CREATE UNIQUE INDEX rhn_lp_snep_uq
    ON rhnLockedPackages (server_id, name_id, evr_id, arch_id)
    TABLESPACE [[128m_tbs]]
    NOLOGGING;
