--
-- Copyright (c) 2013 SUSE
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

CREATE TABLE suseCVEServerChannel
(
    server_id    NUMBER NOT NULL
                     CONSTRAINT suse_cvesc_sid_fk
                         REFERENCES rhnServer (id)
                         ON DELETE CASCADE,
    channel_id   NUMBER NOT NULL
                     CONSTRAINT suse_cvesc_cid_fk
                         REFERENCES rhnChannel (id)
                         ON DELETE CASCADE,
    channel_rank NUMBER NOT NULL,
    created      DATE
                     DEFAULT (sysdate) NOT NULL,
    modified     DATE
                     DEFAULT (sysdate) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_cvesc_sid_cid_uq
    ON suseCVEServerChannel (server_id, channel_id)
    TABLESPACE [[8m_tbs]];

CREATE INDEX suse_cvesc_cid_idx
    ON suseCVEServerChannel (channel_id)
    TABLESPACE [[8m_tbs]]
    NOLOGGING;

