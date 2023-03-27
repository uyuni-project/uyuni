--
-- Copyright (c) 2023 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
CREATE TABLE suseRecurringStateConfig
(
  id                NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_state_config_id_pk PRIMARY KEY,
  rec_id            NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_state_config_state_id_fk
                      REFERENCES suseRecurringState(rec_id)
                      ON DELETE CASCADE,
  state_id          NUMERIC
                    CONSTRAINT suse_recurring_state_intstate_id_fk
                      REFERENCES suseInternalState(id)
                      ON DELETE CASCADE,
  confchan_id       NUMERIC
                    CONSTRAINT suse_recurring_state_confchan_id_fk
                      REFERENCES rhnConfigChannel(id)
                      ON DELETE CASCADE,
  position          NUMERIC NOT NULL,
  CONSTRAINT suse_rec_state_conf_rec_id_pos_uq UNIQUE (rec_id, position)
);

CREATE SEQUENCE suse_recurring_state_config_id_seq;

CREATE UNIQUE INDEX suse_rec_state_conf_rec_id_state_id_uq
  ON suseRecurringStateConfig(rec_id, state_id)
  WHERE confchan_id IS NULL AND state_id IS NOT NULL;

CREATE UNIQUE INDEX suse_rec_state_conf_rec_id_confchan_id_uq
  ON suseRecurringStateConfig(rec_id, confchan_id)
  WHERE state_id IS NULL AND confchan_id IS NOT NULL;
