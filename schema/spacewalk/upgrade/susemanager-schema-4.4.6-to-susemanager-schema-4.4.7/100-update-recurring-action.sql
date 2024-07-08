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
BEGIN;

CREATE TABLE IF NOT EXISTS suseInternalState
(
  id                NUMERIC NOT NULL
                    CONSTRAINT suse_internal_state_id_pk PRIMARY KEY,
  name              VARCHAR(128) NOT NULL,
  label             VARCHAR(128) NOT NULL
);

INSERT INTO suseInternalState (id, name, label)
         VALUES (1, 'certs', 'Certificates')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (2, 'channels', 'Channels')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (3, 'hardware.profileupdate', 'Hardware Profile Update')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (4, 'packages', 'Packages')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (5, 'packages.profileupdate', 'Package Profile Update')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (6, 'uptodate', 'Update System')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (7, 'util.syncbeacons', 'Sync Beacons')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (8, 'util.syncall', 'Sync All')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (9, 'util.syncgrains', 'Sync Grains')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (10, 'util.syncmodules', 'Sync Modules')
         ON CONFLICT DO NOTHING;

INSERT INTO suseInternalState (id, name, label)
         VALUES (11, 'util.syncstates', 'Sync States')
         ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS suseRecurringHighstate
(
  rec_id            NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_highstate_id_pk PRIMARY KEY
                    CONSTRAINT suse_recurring_action_highstate_id_fk
                      REFERENCES suseRecurringAction(id)
                      ON DELETE CASCADE,
  test_mode         CHAR(1) NOT NULL
                    DEFAULT 'N'
);

ALTER TABLE suseRecurringAction ADD COLUMN IF NOT EXISTS test_mode CHAR(1) DEFAULT 'N' NOT NULL;
INSERT INTO suseRecurringHighstate (rec_id, test_mode)
         SELECT id, test_mode FROM suseRecurringAction
         WHERE NOT EXISTS (SELECT id FROM suseRecurringHighstate);
ALTER TABLE suseRecurringAction DROP COLUMN IF EXISTS test_mode;

ALTER TABLE suseRecurringAction ADD COLUMN IF NOT EXISTS action_type VARCHAR(32) NULL;
UPDATE suseRecurringAction SET action_type = 'HIGHSTATE' WHERE action_type IS NULL;
ALTER TABLE suseRecurringAction ALTER COLUMN action_type SET NOT NULL;

CREATE TABLE IF NOT EXISTS suseRecurringState
(
  rec_id            NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_state_id_pk PRIMARY KEY
                    CONSTRAINT suse_recurring_action_state_id_fk
                      REFERENCES suseRecurringAction(id)
                      ON DELETE CASCADE,
  test_mode         CHAR(1) NOT NULL
                    DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS suseRecurringStateConfig
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
  position          NUMERIC NOT NULL
);

ALTER TABLE suseRecurringStateConfig 
  DROP CONSTRAINT IF EXISTS suse_rec_state_conf_rec_id_pos_uq;

ALTER TABLE suseRecurringStateConfig
  ADD CONSTRAINT suse_rec_state_conf_rec_id_pos_uq UNIQUE (rec_id, position);

CREATE SEQUENCE IF NOT EXISTS suse_recurring_state_config_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_rec_state_conf_rec_id_state_id_uq
  ON suseRecurringStateConfig(rec_id, state_id)
  WHERE confchan_id IS NULL AND state_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS suse_rec_state_conf_rec_id_confchan_id_uq
  ON suseRecurringStateConfig(rec_id, confchan_id)
  WHERE state_id IS NULL AND confchan_id IS NOT NULL;

UPDATE rhnTaskoBunch SET description='Schedules actions for minion/group/org' 
  WHERE name='recurring-state-apply-bunch';
UPDATE rhnTaskoBunch SET name='recurring-action-executor-bunch'
  WHERE name='recurring-state-apply-bunch';

UPDATE rhnTaskoTask SET name='recurring-action-executor'
  WHERE name='recurring-state-apply';
UPDATE rhnTaskoTask SET class='com.redhat.rhn.taskomatic.task.RecurringActionJob'
  WHERE class='com.redhat.rhn.taskomatic.task.RecurringStateApplyJob';

COMMIT;

