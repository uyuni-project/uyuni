--
-- Copyright (c) 2017 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

CREATE TABLE rhnImageNeededCache
(
    image_id    NUMBER NOT NULL
                    CONSTRAINT rhn_inc_iid_fk
                        REFERENCES suseImageInfo (id)
                        ON DELETE CASCADE,
    errata_id   NUMBER
                    CONSTRAINT rhn_inc_eid_fk
                        REFERENCES rhnErrata (id)
                        ON DELETE CASCADE,
    package_id  NUMBER NOT NULL
                    CONSTRAINT rhn_inc_pid_fk
                        REFERENCES rhnPackage (id)
                        ON DELETE CASCADE,
    channel_id   NUMBER
                    CONSTRAINT rhn_inc_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE
)
ENABLE ROW MOVEMENT
LOGGING
;

CREATE INDEX rhn_inc_pid_idx
    ON rhnImageNeededCache (package_id)
    NOPARALLEL
    TABLESPACE [[128m_tbs]]
    NOLOGGING;

CREATE INDEX rhn_inc_eid_idx
    ON rhnImageNeededCache (errata_id)
    TABLESPACE [[128m_tbs]]
    NOLOGGING;

CREATE INDEX rhn_inc_cid_idx
    ON rhnImageNeededCache (channel_id)
    TABLESPACE [[128m_tbs]]
    NOLOGGING;

CREATE INDEX rhn_inc_ipeid_idx
    ON rhnImageNeededCache (image_id, package_id, errata_id)
    NOPARALLEL
    TABLESPACE [[128m_tbs]]
    NOLOGGING;
