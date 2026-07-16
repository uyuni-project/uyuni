--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseTransactionalActionHistory
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_transactional_action_history_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,
    action_id        NUMERIC NOT NULL
                         CONSTRAINT suse_transactional_action_history_aid_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    created          TIMESTAMPTZ NOT NULL,
    prereq_status    VARCHAR(32) NOT NULL,
    prereq_at        TIMESTAMPTZ,
    reboot_required  BOOLEAN NOT NULL DEFAULT FALSE,
    reboot_status    VARCHAR(32) NOT NULL,
    reboot_at        TIMESTAMPTZ,
    post_status      VARCHAR(32) NOT NULL,
    post_at          TIMESTAMPTZ,

    CONSTRAINT suse_transactional_action_history_pk PRIMARY KEY (minion_server_id, action_id)
);
