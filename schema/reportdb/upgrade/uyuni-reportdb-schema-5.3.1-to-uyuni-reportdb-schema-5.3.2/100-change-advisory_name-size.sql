-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- remember the privileges granted on the views which will be affected by the
-- change, since dropping a view also removes all its privileges
SELECT backup_view_acls(ARRAY['ErrataListReport', 'ErrataChannelsReport', 'ErrataSystemsReport']);

-- drop views which will be affected by the change
DROP VIEW IF EXISTS ErrataListReport;
DROP VIEW IF EXISTS ErrataChannelsReport;
DROP VIEW IF EXISTS ErrataSystemsReport;

-- increase the column size
ALTER TABLE Errata ALTER COLUMN advisory_name TYPE VARCHAR(150);
ALTER TABLE ChannelErrata ALTER COLUMN advisory_name TYPE VARCHAR(150);
ALTER TABLE SystemErrata ALTER COLUMN advisory_name TYPE VARCHAR(150);

-- re-create the views which use advisory_name
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
ORDER BY Errata.mgm_id, Errata.advisory_name;

CREATE OR REPLACE VIEW ErrataChannelsReport AS
  SELECT mgm_id
            , advisory_name
            , errata_id
            , channel_label
            , channel_id
            , synced_date
    FROM ChannelErrata
ORDER BY mgm_id, advisory_name, errata_id, channel_label, channel_id;


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
ORDER BY SystemErrata.mgm_id, SystemErrata.errata_id, SystemErrata.system_id;

-- restore the privileges which were granted on the views before they were dropped
SELECT restore_view_acls(ARRAY['ErrataListReport', 'ErrataChannelsReport', 'ErrataSystemsReport']);
