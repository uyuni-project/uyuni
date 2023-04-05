ALTER TABLE rhntaskotask ALTER COLUMN class TYPE varchar(120);

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'payg-dimension-computation-bunch',
       'Compute the dimensions data required for PAYG billing', null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoBunch
        WHERE name = 'payg-dimension-computation-bunch'
    );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'payg-dimension-computation-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='payg-dimension-computation-bunch'),
       current_timestamp, '0 45 * ? * * *'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoSchedule
        WHERE job_label = 'payg-dimension-computation-default'
    );

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'payg-dimension-computation',
       'com.redhat.rhn.taskomatic.task.payg.PaygComputeDimensionsTask'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask
        WHERE name = 'payg-dimension-computation'
    );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
       (SELECT id FROM rhnTaskoBunch WHERE name='payg-dimension-computation-bunch'),
       (SELECT id FROM rhnTaskoTask WHERE name='payg-dimension-computation'),
       0, null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTemplate
        WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'payg-dimension-computation-bunch')
          AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'payg-dimension-computation')
          AND ordering = 0
    );
