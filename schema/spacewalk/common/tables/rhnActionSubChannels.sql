--
-- Copyright (c) 2018 SUSE LLC
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

CREATE TABLE rhnActionSubChannels (
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrch_id_pk PRIMARY KEY,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actionsubscrch_aid_fk
                            REFERENCES rhnAction (id)
                            ON DELETE CASCADE,
    base_channel_id     NUMERIC
                            CONSTRAINT rhn_actionsubscrch_base_ch_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    created             TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE SEQUENCE RHN_ACT_SUBSCR_CHNLS_ID_SEQ;

CREATE INDEX rhn_action_sub_channels_action_id_idx
on rhnactionsubchannels (action_id);