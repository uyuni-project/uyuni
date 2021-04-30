-- Copyright (c) 2021 SUSE LLC
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

CREATE TABLE IF NOT EXISTS rhnActionPlaybook
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_action_playbook_id_pk
                                PRIMARY KEY,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_action_playbook_aid_fk
                                REFERENCES rhnAction (id)
                                ON DELETE CASCADE,
    playbook_path       VARCHAR(1024) NOT NULL,
    inventory_path      VARCHAR(1024),
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
)
;

CREATE UNIQUE INDEX IF NOT EXISTS rhn_act_playbook_aid_uq
    ON rhnActionPlaybook (action_id);

CREATE SEQUENCE IF NOT EXISTS rhn_act_playbook_id_seq;
