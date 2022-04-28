DO $$
BEGIN
  IF EXISTS(SELECT * FROM information_schema.columns WHERE table_name='systemaction' and column_name='scheduled_by')
  THEN
    DROP VIEW IF EXISTS ActionsReport;

    ALTER TABLE SystemAction ADD COLUMN IF NOT EXISTS scheduler_id NUMERIC;

    IF EXISTS(SELECT * FROM information_schema.columns WHERE table_name='systemaction' and column_name='scheduler_username')
    THEN
      RAISE NOTICE 'The scheduler_username is already present. Dropping scheduled_by';
      ALTER TABLE SystemAction DROP COLUMN scheduled_by;
    ELSE
      ALTER TABLE SystemAction RENAME COLUMN scheduled_by TO scheduler_username;
    END IF;

    CREATE OR REPLACE VIEW ActionsReport AS
      SELECT DISTINCT SystemAction.mgm_id
                 , SystemAction.action_id
                 , SystemAction.earliest_action
                 , SystemAction.event
                 , SystemAction.action_name
                 , SystemAction.scheduler_id
                 , SystemAction.scheduler_username
                 , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Picked Up' OR status = 'Queued') OVER(PARTITION BY action_id) AS in_progress_systems
                 , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Completed') OVER(PARTITION BY action_id) AS completed_systems
                 , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Failed') OVER(PARTITION BY action_id) AS failed_systems
                 , SystemAction.archived
                 , SystemAction.synced_date
        FROM SystemAction
    ORDER BY SystemAction.mgm_id, SystemAction.action_id;

  ELSE
    RAISE NOTICE 'The scheduled_by has already been removed from ActionsReport';
  END IF;
END $$;

DROP VIEW IF EXISTS SystemHistoryConfigurationReport;

CREATE OR REPLACE VIEW SystemHistoryConfigurationReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , pickup_time
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event IN ( 'Upload config file data to server',
                    'Verify deployed config files',
                    'Show differences between profiled config files and deployed config files',
                    'Upload config file data based upon mtime to server',
                    'Deploy config files to system'
                  )
ORDER BY mgm_id, system_id, action_id
;

DROP VIEW IF EXISTS SystemHistoryErrataReport;

CREATE OR REPLACE VIEW SystemHistoryErrataReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , pickup_time
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event = 'Patch Update'
ORDER BY mgm_id, system_id, action_id
;

-- SystemHistoryKickstartReport might have been created by mistake due to a wrong update file
DROP VIEW IF EXISTS SystemHistoryKickstartReport;

DROP VIEW IF EXISTS SystemHistoryAutoinstallationReport;

CREATE OR REPLACE VIEW SystemHistoryAutoinstallationReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , pickup_time
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event IN ( 'Schedule a package sync for auto installations', 'Initiate an auto installation' )
ORDER BY mgm_id, system_id, action_id
;

DROP VIEW IF EXISTS SystemHistoryPackagesReport;

CREATE OR REPLACE VIEW SystemHistoryPackagesReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , pickup_time
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event IN ( 'Package Upgrade', 'Package Removal' )
ORDER BY mgm_id, system_id, action_id
;

DROP VIEW IF EXISTS SystemHistoryScapReport;

CREATE OR REPLACE VIEW SystemHistoryScapReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , pickup_time
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event = 'OpenSCAP xccdf scanning'
ORDER BY mgm_id, system_id, action_id
;
