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

CREATE TABLE suseCVEServerChannel
(
    server_id    NUMERIC NOT NULL
                     CONSTRAINT suse_cvesc_sid_fk
                         REFERENCES rhnServer (id),
    channel_id   NUMERIC NOT NULL
                     CONSTRAINT suse_cvesc_cid_fk
                         REFERENCES rhnChannel (id),
    channel_rank NUMERIC NOT NULL,
    created      TIMESTAMPTZ
                     DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified     TIMESTAMPTZ
                     DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE UNIQUE INDEX suse_cvesc_sid_cid_uq
    ON suseCVEServerChannel (server_id, channel_id)
    ;

CREATE INDEX suse_cvesc_cid_idx
    ON suseCVEServerChannel (channel_id)
    
    ;

