UPDATE rhnTaskoSchedule
  SET cron_expr = '0 0/5 * * * ?'
WHERE job_label = 'cobbler-sync-default'
  AND bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='cobbler-sync-bunch')
  AND cron_expr = '0 * * * * ?';

