-- Rename gatherer data in taskomatic bunch and schedule

-- gatherer-bunch becomes gatherer-matcher-bunch
UPDATE rhnTaskoBunch
SET name = 'gatherer-matcher-bunch',
    description = 'Schedule running gatherer and matcher'
WHERE name = 'gatherer-bunch';

-- gatherer-matcher-default matcher gatherer-default
UPDATE rhnTaskoSchedule
SET job_label = 'gatherer-matcher-default'
WHERE job_label = 'gatherer-default';

