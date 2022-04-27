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
