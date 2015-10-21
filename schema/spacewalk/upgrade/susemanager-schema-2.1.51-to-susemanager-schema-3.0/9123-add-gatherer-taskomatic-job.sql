INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
             VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'gatherer-bunch', 'Schedule running gatherer', null);

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'gatherer-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='gatherer-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoTask (id, name, class)
         VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'gatherer', 'com.redhat.rhn.taskomatic.task.gatherer.GathererJob');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
             VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='gatherer-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='gatherer'),
                        0,
                        null);

