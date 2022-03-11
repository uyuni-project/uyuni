
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'mgr-update-reporting-hub-bunch',
       'Update Reporting DB with data from other susemanager servers', null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoBunch
        WHERE name = 'mgr-update-reporting-hub-bunch'
    );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-reporting-hub-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-hub-bunch'),
       current_timestamp, '0 0 0 ? * *'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoSchedule
        WHERE job_label = 'update-reporting-hub-default'
    );

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'mgr-update-reporting-hub',
       'com.redhat.rhn.taskomatic.task.HubReportDbUpdateTask'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask
        WHERE name = 'mgr-update-reporting-hub'
    );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
       (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-hub-bunch'),
       (SELECT id FROM rhnTaskoTask WHERE name='mgr-update-reporting-hub'),
       0, null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTemplate
        WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'mgr-update-reporting-hub-bunch')
          AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'mgr-update-reporting-hub')
          AND ordering = 0
    );


INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'mgr-update-reporting-bunch',
       'Update Reporting DB with current data', null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoBunch
        WHERE name = 'mgr-update-reporting-bunch'
    );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-reporting-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-bunch'),
       current_timestamp, '0 0 0 ? * *'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoSchedule
        WHERE job_label = 'update-reporting-default'
    );

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'mgr-update-reporting',
       'com.redhat.rhn.taskomatic.task.ReportDbUpdateTask'
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask
        WHERE name = 'mgr-update-reporting'
    );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
       (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-bunch'),
       (SELECT id FROM rhnTaskoTask WHERE name='mgr-update-reporting'),
       0, null
WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTemplate
        WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'mgr-update-reporting-bunch')
          AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'mgr-update-reporting')
          AND ordering = 0
    );
commit;
