
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
 SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'system-profile-refresh-bunch', 'Refresh System Profiles of all registered servers', null
 WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoBunch WHERE name = 'system-profile-refresh-bunch');
 
-- Once a month at the 15th at 5am

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
  SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'system-profile-refresh-default',
	 (SELECT id FROM rhnTaskoBunch WHERE name='system-profile-refresh-bunch'),
	 current_timestamp, '0 0 5 15 * ?'
  WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoSchedule WHERE job_label = 'system-profile-refresh-default');
 
INSERT INTO rhnTaskoTask (id, name, class)
  SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'system-profile-refresh', 'com.redhat.rhn.taskomatic.task.SystemProfileRefreshTask'
  WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTask WHERE name = 'system-profile-refresh');

 
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
  SELECT sequence_nextval('rhn_tasko_template_id_seq'),
         (SELECT id FROM rhnTaskoBunch WHERE name='system-profile-refresh-bunch'),
         (SELECT id FROM rhnTaskoTask WHERE name='system-profile-refresh'),
         0,
         null
  WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoTemplate 
	              WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'system-profile-refresh-bunch' )
                        AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'system-profile-refresh'));

commit;
