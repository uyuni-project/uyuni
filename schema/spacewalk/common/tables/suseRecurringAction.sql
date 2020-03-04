--
-- Copyright (c) 2020 SUSE LLC
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

CREATE TABLE suseRecurringAction
(
  id                NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_action_id_pk PRIMARY KEY,
  target_type       VARCHAR(32) NOT NULL,
  name              VARCHAR(256) NOT NULL,
  cron_expr         VARCHAR(32) NOT NULL,
  minion_id         NUMERIC
                    CONSTRAINT suse_rec_action_minion_fk
                      REFERENCES suseMinionInfo(server_id)
                      ON DELETE CASCADE,
  group_id          NUMERIC
                    CONSTRAINT suse_rec_action_group_fk
                      REFERENCES rhnServerGroup(id)
                      ON DELETE CASCADE,
  org_id            NUMERIC
                    CONSTRAINT suse_rec_action_org_fk
                      REFERENCES web_customer(id)
                      ON DELETE CASCADE,
  creator_id        NUMERIC
                    CONSTRAINT suse_rec_action_creator_fk
                      REFERENCES web_contact(id)
                      ON DELETE CASCADE,
  active            CHAR(1) DEFAULT ('Y') NOT NULL,
  test_mode         CHAR(1) DEFAULT ('Y') NOT NULL,
  created           TIMESTAMP WITH TIME ZONE
                      DEFAULT (current_timestamp)
                      NOT NULL,
  modified          TIMESTAMP WITH TIME ZONE
                      DEFAULT (current_timestamp)
                      NOT NULL
);

CREATE SEQUENCE suse_recurring_action_id_seq;

CREATE INDEX suse_rec_action_type
    ON suseRecurringAction(target_type);

