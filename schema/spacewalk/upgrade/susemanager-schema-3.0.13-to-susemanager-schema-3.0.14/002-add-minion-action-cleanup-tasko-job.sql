INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
             VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'minion-action-cleanup-bunch', 'Cleanup action on minions for which we missed the job result event', null);

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-action-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-cleanup-bunch'),
        current_timestamp, '0 0 23 ? * *');

INSERT INTO rhnTaskoTask (id, name, class)
         VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'minion-action-cleanup', 'com.redhat.rhn.taskomatic.task.MinionActionCleanup');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
             VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-cleanup-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='minion-action-cleanup'),
                        0,
                        null);
