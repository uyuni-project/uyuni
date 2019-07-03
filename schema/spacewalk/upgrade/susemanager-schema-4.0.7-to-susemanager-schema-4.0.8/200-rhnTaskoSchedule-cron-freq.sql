UPDATE rhnTaskoSchedule SET cron_expr = '0 0 0 * * ?'
    WHERE job_label = 'minion-action-cleanup-default'
        AND active_till IS NULL
        AND cron_expr = '0 0 * * * ?';
