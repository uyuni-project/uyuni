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


CREATE TABLE rhnActionDupChannel
(
    action_dup_id       NUMBER NOT NULL
                            CONSTRAINT rhn_actdupchan_dupid_fk
                            REFERENCES rhnActionDup (id)
                            ON DELETE CASCADE,
    channel_id          NUMBER NOT NULL
                            CONSTRAINT rhn_actdupchan_chanid_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    task                CHAR(1)
                            DEFAULT ('S') NOT NULL
                            CONSTRAINT rhn_actdupchan_task_ck
                                CHECK (task in ('S','U')),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_actdupchan_aid_cid_uq
    ON rhnActionDupChannel (action_dup_id, channel_id)
    TABLESPACE [[4m_tbs]];

CREATE INDEX rhn_actdupchan_cid_idx
    ON rhnActionDupChannel (channel_id)
    TABLESPACE [[4m_tbs]];

