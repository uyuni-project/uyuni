INSERT INTO rhnTaskoTask (id, name, class)
  SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'clm-channel-diff', 'com.redhat.rhn.taskomatic.task.ClmChannelDiff' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTask WHERE name = 'clm-channel-diff');

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
  SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'compare-task-bunch', 'Schedules a comparison tasks for the whole system', null FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoBunch WHERE name = 'compare-task-bunch');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
  SELECT sequence_nextval('rhn_tasko_template_id_seq'),
                         (SELECT id FROM rhnTaskoBunch WHERE name='compare-task-bunch'),
                         (SELECT id FROM rhnTaskoTask WHERE name='clm-channel-diff'),
                         0, null FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTemplate
                    WHERE  bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='compare-task-bunch')
                    AND    task_id = (SELECT id FROM rhnTaskoTask WHERE name='clm-channel-diff'));
-- Once a day at 6am
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
  SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'compare-task-default',
         (SELECT id FROM rhnTaskoBunch WHERE name='compare-task-bunch'),
         current_timestamp, '0 0 6 ? * *' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoSchedule WHERE job_label = 'compare-task-default');
