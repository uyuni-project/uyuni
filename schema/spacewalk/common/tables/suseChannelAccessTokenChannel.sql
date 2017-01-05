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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseChannelAccessTokenChannel
(
    token_id    NUMBER NOT NULL
                    CONSTRAINT suse_catc_tid_fk
                        REFERENCES suseChannelAccessToken (id),
    channel_id  NUMBER NOT NULL
                    CONSTRAINT suse_catc_cid_fk
                        REFERENCES rhnChannel (id),
    created     timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL,
    modified    timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_catc_tid_cid_uq
    ON suseChannelAccessTokenChannel (token_id, channel_id)
    TABLESPACE [[8m_tbs]];
