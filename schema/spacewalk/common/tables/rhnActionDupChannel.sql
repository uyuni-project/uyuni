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
-- SPDX-License-Identifier: GPL-2.0-only
--


CREATE TABLE rhnActionDupChannel
(
    action_dup_id       NUMERIC NOT NULL
                            CONSTRAINT rhn_actdupchan_dupid_fk
                            REFERENCES rhnActionDup (id)
                            ON DELETE CASCADE,
    channel_id          NUMERIC NOT NULL
                            CONSTRAINT rhn_actdupchan_chanid_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    task                CHAR(1)
                            DEFAULT ('S') NOT NULL
                            CONSTRAINT rhn_actdupchan_task_ck
                                CHECK (task in ('S','U')),
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_actdupchan_aid_cid_uq
    ON rhnActionDupChannel (action_dup_id, channel_id)
    ;

CREATE INDEX rhn_actdupchan_cid_idx
    ON rhnActionDupChannel (channel_id)
    ;

