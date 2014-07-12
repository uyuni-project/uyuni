--
-- Copyright (c) 2008--2014 Red Hat, Inc.
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


CREATE TABLE rhnUserGroupMembers
(
    user_id        NUMBER NOT NULL
                       CONSTRAINT rhn_ugmembers_uid_fk
                           REFERENCES web_contact (id)
                           ON DELETE CASCADE,
    user_group_id  NUMBER NOT NULL
                       CONSTRAINT rhn_ugmembers_ugid_fk
                           REFERENCES rhnUserGroup (id),
    temporary      CHAR(1)
                        DEFAULT ('N') NOT NULL
                        CONSTRAINT rhn_ugmembers_t_ck
                            CHECK (temporary in ('Y', 'N')),
    created        timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL,
    modified       timestamp with local time zone
                       DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_ugmembers_uid_ugid_temp_uq
    ON rhnUserGroupMembers (user_id, user_group_id, temporary)
    TABLESPACE [[8m_tbs]];

CREATE INDEX rhn_ugmembers_ugid_idx
    ON rhnUserGroupMembers (user_group_id)
    TABLESPACE [[8m_tbs]]
    NOLOGGING;

