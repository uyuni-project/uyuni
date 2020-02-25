INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'recurring-state-apply-bunch', 'Applies salt state to minion/group/org', null
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch');

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'recurring-state-apply', 'com.redhat.rhn.taskomatic.task.RecurringStateApplyJob'
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTask WHERE name = 'recurring-state-apply');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'), ( SELECT id FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch' ), ( SELECT id FROM rhnTaskoTask WHERE name = 'recurring-state-apply' ), 0, null
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTemplate WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'recurring-state-apply-bunch' ) );
