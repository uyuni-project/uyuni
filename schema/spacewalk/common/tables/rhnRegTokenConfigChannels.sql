--
-- Copyright (c) 2008 Red Hat, Inc.
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnRegTokenConfigChannels
(
    token_id           NUMERIC NOT NULL
                           CONSTRAINT rhn_regtok_confchan_tid_fk
                               REFERENCES rhnRegToken (id)
                               ON DELETE CASCADE,
    config_channel_id  NUMERIC NOT NULL
                           CONSTRAINT rhn_regtok_confchan_ccid_fk
                               REFERENCES rhnConfigChannel (id)
                               ON DELETE CASCADE,
    position           NUMERIC NOT NULL
)

;

CREATE UNIQUE INDEX rhn_regtok_confchan_t_cc_uq
    ON rhnRegTokenConfigChannels (token_id, config_channel_id)
    ;

CREATE INDEX rhn_regtok_confchan_ccid_idx
    ON rhnRegTokenConfigChannels (config_channel_id)
    ;

