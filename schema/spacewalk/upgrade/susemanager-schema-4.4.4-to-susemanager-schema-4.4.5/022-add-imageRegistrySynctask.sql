INSERT INTO rhnTaskoTask (id, name, class)
select sequence_nextval('rhn_tasko_task_id_seq'),
'image-registry-sync-task', 'com.redhat.rhn.taskomatic.task.image.ImageRegistrySyncTask'
  WHERE NOT EXISTS (
            SELECT 1 FROM rhnTaskoTask WHERE
                name = 'image-registry-sync-task'
    );


INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
select sequence_nextval('rhn_tasko_bunch_id_seq'), 'image-registry-sync-bunch', 'Runs ImageRegistrySyncTask:
- integer parameter sync project id
- without parameter updates data for all projects', null
WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoBunch WHERE name = 'image-registry-sync-bunch');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
select sequence_nextval('rhn_tasko_template_id_seq'),
            (SELECT id FROM rhnTaskoBunch WHERE name = 'image-registry-sync-bunch'),
            (SELECT id FROM rhnTaskoTask WHERE name = 'image-registry-sync-task'),
            0, null
  WHERE NOT EXISTS
      ( SELECT 1 FROM rhnTaskoTemplate
          WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'image-registry-sync-bunch' )
          and task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'image-registry-sync-task'));

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
select sequence_nextval('rhn_tasko_schedule_id_seq'), 'image-registry-sync-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='image-registry-sync-bunch'),
       current_timestamp, '0 0/10 * * * ?'
  WHERE not exists (
        SELECT 1 FROM rhnTaskoSchedule
        WHERE job_label = 'image-registry-sync-default'
  );

commit;