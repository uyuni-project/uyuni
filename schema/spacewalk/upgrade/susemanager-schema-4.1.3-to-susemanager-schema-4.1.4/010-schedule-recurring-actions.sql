INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'recurring-state-apply-bunch', 'Applies salt state to minion/group/org', null
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch');

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'recurring-state-apply', 'com.redhat.rhn.taskomatic.task.RecurringStateApplyJob'
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTask WHERE name = 'recurring-state-apply');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'), ( SELECT id FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch' ), ( SELECT id FROM rhnTaskoTask WHERE name = 'recurring-state-apply' ), 0, null
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTemplate WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch' ) );

CREATE TABLE IF NOT EXISTS suseRecurringAction
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

CREATE SEQUENCE IF NOT EXISTS suse_recurring_action_id_seq;

CREATE INDEX IF NOT EXISTS suse_rec_action_type
    ON suseRecurringAction(target_type);

CREATE UNIQUE INDEX IF NOT EXISTS suse_rec_action_name_minion_uq
    ON suseRecurringAction(name, minion_id)
    WHERE group_id IS NULL AND org_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS suse_rec_action_name_grp_uq
    ON suseRecurringAction(name, group_id)
    WHERE minion_id IS NULL AND org_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS suse_rec_action_name_org_uq
    ON suseRecurringAction(name, org_id)
    WHERE minion_id IS NULL AND group_id IS NULL;

