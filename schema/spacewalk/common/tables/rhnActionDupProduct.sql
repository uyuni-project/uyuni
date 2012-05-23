--
-- Copyright (c) 2012, Novell Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--


CREATE TABLE rhnActionDupProduct
(
    action_dup_id       NUMBER NOT NULL
                            CONSTRAINT rhn_actdupchanprof_dupid_fk
                            REFERENCES rhnActionDup (id)
                            ON DELETE CASCADE,
    from_pdid           NUMBER NOT NULL
                            CONSTRAINT rhn_actdupchanprod_fpdid_fk
                            REFERENCES suseProducts (id)
                            ON DELETE CASCADE,
    to_pdid             NUMBER NOT NULL
                            CONSTRAINT rhn_actdupchanprod_tpdid_fk
                            REFERENCES suseProducts (id)
                            ON DELETE CASCADE,
    created             DATE
                            DEFAULT (sysdate) NOT NULL,
    modified            DATE
                            DEFAULT (sysdate) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_actdupchan_aid_pdids_uq
    ON rhnActionDupProduct (action_dup_id, from_pdid, to_pdid)
    TABLESPACE [[4m_tbs]];

