DELETE FROM rhnTaskoRun
 WHERE template_id in (
        SELECT id
          FROM rhnTaskoTemplate
         WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='mgr-register-bunch')
           AND task_id = (SELECT id FROM rhnTaskoTask WHERE name='mgr-register')
);
DELETE FROM rhnTaskoTemplate
 WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='mgr-register-bunch')
   AND task_id = (SELECT id FROM rhnTaskoTask WHERE name='mgr-register');
DELETE FROM rhnTaskoTask WHERE name = 'mgr-register';
DELETE FROM rhnTaskoSchedule WHERE job_label = 'mgr-register-default';
DELETE FROM rhnTaskoBunch WHERE name = 'mgr-register-bunch';
