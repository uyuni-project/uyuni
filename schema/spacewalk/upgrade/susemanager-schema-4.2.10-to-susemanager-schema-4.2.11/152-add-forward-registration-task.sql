
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
  SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'mgr-forward-registration-bunch',
         'Forward registrations to SUSE Customer Center', null
  WHERE NOT EXISTS (
	SELECT 1 FROM rhnTaskoBunch
         WHERE name = 'mgr-forward-registration-bunch'
  );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
  SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'mgr-forward-registration-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-forward-registration-bunch'),
        current_timestamp, '0 0/15 * * * ?'
  WHERE NOT EXISTS (
	SELECT 1 FROM rhnTaskoSchedule
	WHERE job_label = 'mgr-forward-registration-default'
  );

INSERT INTO rhnTaskoTask (id, name, class)
  SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'mgr-forward-registration',
         'com.redhat.rhn.taskomatic.task.ForwardRegistrationTask'
  WHERE NOT EXISTS (
	SELECT 1 FROM rhnTaskoTask
	WHERE name = 'mgr-forward-registration'
  );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
  SELECT sequence_nextval('rhn_tasko_template_id_seq'),
         (SELECT id FROM rhnTaskoBunch WHERE name='mgr-forward-registration-bunch'),
         (SELECT id FROM rhnTaskoTask WHERE name='mgr-forward-registration'),
         0, null
  WHERE NOT EXISTS (
	SELECT 1 FROM rhnTaskoTemplate
	WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'mgr-forward-registration-bunch')
	  AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'mgr-forward-registration')
	  AND ordering = 0
  );

commit;
