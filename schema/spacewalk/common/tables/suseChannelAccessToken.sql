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

CREATE TABLE suseChannelAccessToken
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_chan_access_token_id_pk PRIMARY KEY,
    minion_id        NUMBER
                         CONSTRAINT suse_chan_access_token_mid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE SET NULL,
    token            varchar2(4000) NOT NULL,
    created          timestamp with local time zone NOT NULL,
    expiration       timestamp with local time zone NOT NULL,
    valid            CHAR(1) DEFAULT ('N') NOT NULL CHECK (valid in ('Y', 'N'))
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_chan_access_token_id_seq;

CREATE UNIQUE INDEX suse_channelaccesstoken_token_uq
    ON suseChannelAccessToken (token);
