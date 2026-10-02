--
-- Copyright (c) 2014 SUSE
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
--

CREATE TABLE rhnActionChain
(
    id          NUMERIC        NOT NULL
                    CONSTRAINT rhn_action_chain_id_pk PRIMARY KEY,
    label       VARCHAR(256) NOT NULL,
    user_id     NUMERIC        NOT NULL
                    CONSTRAINT rhn_actionchain_uid_fk
                        REFERENCES web_contact (id)
                        ON DELETE CASCADE,
    created     TIMESTAMPTZ          DEFAULT(CURRENT_TIMESTAMP) NOT NULL,
    modified    TIMESTAMPTZ          DEFAULT(CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE SEQUENCE rhn_actionchain_id_seq;
