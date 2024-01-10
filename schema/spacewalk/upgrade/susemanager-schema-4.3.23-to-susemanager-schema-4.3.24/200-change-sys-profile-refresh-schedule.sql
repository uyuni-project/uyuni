-- Once a month at the 2nd Saturday at 5am
UPDATE rhnTaskoSchedule
  SET cron_expr = '0 0 5 ? * SAT#2'
WHERE job_label = 'system-profile-refresh-default'
  AND bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='system-profile-refresh-bunch')
  AND cron_expr = '0 0 5 15 * ?';

