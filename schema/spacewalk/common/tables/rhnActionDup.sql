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


CREATE TABLE rhnActionDup
(
    id                  NUMBER NOT NULL
                            CONSTRAINT rhn_actiondup_id_pk PRIMARY KEY
                            USING INDEX TABLESPACE [[2m_tbs]],
    action_id           NUMBER NOT NULL
                            CONSTRAINT rhn_actiondup_aid_fk
                            REFERENCES rhnAction (id)
                            ON DELETE CASCADE,
    dry_run             CHAR(1)
                            DEFAULT ('N') NOT NULL
                            CONSTRAINT rhn_actiondup_dr_ck
                                CHECK (dry_run in ('Y','N')),
    full_update         CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_actiondup_fu_ck
                                CHECK (full_update in ('Y','N')),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_actiondup_aid_uq
ON rhnActionDup (action_id)
TABLESPACE [[8m_tbs]];

CREATE SEQUENCE rhn_actiondup_id_seq;
