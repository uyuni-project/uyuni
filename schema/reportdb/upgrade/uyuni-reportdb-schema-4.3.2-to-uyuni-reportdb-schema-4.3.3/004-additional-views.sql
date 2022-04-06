ALTER TABLE SystemAction
    ADD COLUMN IF NOT EXISTS scheduled_by VARCHAR(64),
    ADD COLUMN IF NOT EXISTS earliest_action TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS archived BOOLEAN,
    ADD COLUMN IF NOT EXISTS action_name VARCHAR(128)
;

CREATE OR REPLACE VIEW ActionsReport AS
  SELECT DISTINCT SystemAction.action_id
             , SystemAction.earliest_action
             , SystemAction.event
             , SystemAction.action_name
             , SystemAction.scheduled_by
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Picked Up' OR status = 'Queued') OVER(PARTITION BY action_id) AS in_progress_systems
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Completed') OVER(PARTITION BY action_id) AS completed_systems
             , string_agg(SystemAction.hostname, ';') FILTER(WHERE status = 'Failed') OVER(PARTITION BY action_id) AS failed_systems
             , SystemAction.archived
    FROM SystemAction
;

ALTER TABLE Channel
  ADD COLUMN IF NOT EXISTS checksum_type VARCHAR(32);

CREATE TABLE IF NOT EXISTS ChannelRepository
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    repository_id             NUMERIC NOT NULL,
    repository_label          VARCHAR(128),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT ChannelRepository_pk PRIMARY KEY (mgm_id, channel_id, repository_id)
);

CREATE TABLE IF NOT EXISTS Repository
(
    mgm_id                    NUMERIC NOT NULL,
    repository_id             NUMERIC NOT NULL,
    label                     VARCHAR(128),
    url                       VARCHAR(100),
    type                      VARCHAR(64),
    metadata_signed           BOOLEAN,
    organization              VARCHAR(128),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT Repository_pk PRIMARY KEY (mgm_id, repository_id)
);

CREATE OR REPLACE VIEW CustomChannelsReport AS
  WITH repositories AS (
      SELECT mgm_id, channel_id, string_agg(repository_id || ' - ' || repository_label, ';')  AS channel_repositories
        FROM ChannelRepository
    GROUP BY mgm_id, channel_id
  )
  SELECT Channel.mgm_id
             , Channel.organization
             , Channel.channel_id
             , Channel.label
             , Channel.name
             , Channel.summary
             , Channel.description
             , Channel.parent_channel_label
             , Channel.arch
             , Channel.checksum_type
             , repositories.channel_repositories
    FROM Channel
             LEFT JOIN repositories ON ( Channel.mgm_id = repositories.mgm_id AND Channel.channel_id = repositories.channel_id )
   WHERE Channel.organization IS NOT NULL
ORDER BY Channel.mgm_id, Channel.organization, Channel.channel_id
;

DROP VIEW IF EXISTS ErrataListReport;

CREATE OR REPLACE VIEW ErrataListReport AS
  SELECT Errata.mgm_id
            , Errata.errata_id
            , Errata.advisory_name
            , Errata.advisory_type
            , Errata.cve
            , Errata.synopsis
            , Errata.issue_date
            , Errata.update_date
            , COUNT(SystemErrata.system_id) AS affected_systems
            , Errata.synced_date
    FROM Errata
            LEFT JOIN SystemErrata ON ( Errata.mgm_id = SystemErrata.mgm_id AND Errata.errata_id = SystemErrata.errata_id )
GROUP BY Errata.mgm_id
            , Errata.errata_id
            , Errata.advisory_name
            , Errata.advisory_type
            , Errata.cve
            , Errata.synopsis
            , Errata.issue_date
            , Errata.update_date
            , Errata.synced_date
ORDER BY advisory_name
;

CREATE OR REPLACE VIEW ErrataSystemsReport AS
  WITH V6Addresses AS (
          SELECT mgm_id, system_id, interface_id, string_agg(address || ' (' || scope || ')', ';') AS ip6_addresses
            FROM SystemNetAddressV6
        GROUP BY mgm_id, system_id, interface_id
  )
  SELECT SystemErrata.mgm_id
              , SystemErrata.errata_id
              , SystemErrata.advisory_name
              , SystemErrata.system_id
              , System.profile_name
              , System.hostname
              , SystemNetAddressV4.address AS ip_address
              , V6Addresses.ip6_addresses
              , SystemErrata.synced_date
    FROM SystemErrata
            INNER JOIN System ON ( SystemErrata.mgm_id = System.mgm_id AND SystemErrata.system_id = System.system_id )
            LEFT JOIN SystemNetInterface ON ( System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND primary_interface )
            LEFT JOIN SystemNetAddressV4 ON ( System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id )
            LEFT JOIN V6Addresses ON ( System.mgm_id = V6Addresses.mgm_id AND System.system_id = V6Addresses.system_id AND SystemNetInterface.interface_id = V6Addresses.interface_id )
ORDER BY SystemErrata.mgm_id, SystemErrata.errata_id, SystemErrata.system_id
;

CREATE OR REPLACE VIEW HostGuestsReport AS
  SELECT mgm_id
            , host_system_id AS host
            , virtual_system_id AS guest
            , synced_date
    FROM SystemVirtualData
   WHERE host_system_id IS NOT NULL
            AND virtual_system_id IS NOT NULL
ORDER BY mgm_id, host_system_id, virtual_system_id
;

CREATE OR REPLACE VIEW SystemHistoryChannelsReport AS
  SELECT mgm_id
              , system_id
              , history_id
              , event_time
              , 'Done' AS status
              , event
              , event_data
              , synced_date
    FROM SystemHistory
   WHERE event IN ('Subscribed to channel', 'Unsubscribed from channel')
ORDER BY mgm_id, system_id, history_id
;

CREATE OR REPLACE VIEW SystemHistoryConfigurationReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
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

CREATE OR REPLACE VIEW SystemHistoryEntitlementsReport AS
  SELECT mgm_id
              , system_id
              , history_id
              , event_time
              , 'Done' AS status
              , event
              , event_data
              , synced_date
    FROM SystemHistory
   WHERE event NOT IN ('Subscribed to channel', 'Unsubscribed from channel')
ORDER BY mgm_id, system_id, history_id
;

CREATE OR REPLACE VIEW SystemHistoryErrataReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event = 'Patch Update'
ORDER BY mgm_id, system_id, action_id
;

CREATE OR REPLACE VIEW SystemHistoryKickstartReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event IN ( 'Schedule a package sync for auto installations', 'Initiate an auto installation' )
ORDER BY mgm_id, system_id, action_id
;

CREATE OR REPLACE VIEW SystemHistoryPackagesReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event IN ( 'Package Upgrade', 'Package Removal' )
ORDER BY mgm_id, system_id, action_id
;

CREATE OR REPLACE VIEW SystemHistoryScapReport AS
  SELECT mgm_id
            , system_id
            , action_id
            , earliest_action
            , completion_time
            , status
            , event
            , event_data
            , synced_date
    FROM SystemAction
   WHERE event = 'OpenSCAP xccdf scanning'
ORDER BY mgm_id, system_id, action_id
;
