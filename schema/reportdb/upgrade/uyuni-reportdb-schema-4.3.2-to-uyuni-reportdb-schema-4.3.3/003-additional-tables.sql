ALTER TABLE SystemGroup
    DROP CONSTRAINT IF EXISTS SystemGroup_pk,
    DROP COLUMN IF EXISTS system_id;

DELETE FROM SystemGroup T1 USING SystemGroup T2
      WHERE T1.ctid < T2.ctid
                AND T1.mgm_id = T2.mgm_id
                AND T1.system_group_id = T2.system_group_id
;

ALTER TABLE SystemGroup
  ADD CONSTRAINT SystemGroup_pk PRIMARY KEY (mgm_id, system_group_id);

CREATE TABLE IF NOT EXISTS SystemGroupPermission
(
    mgm_id                    NUMERIC NOT NULL,
    system_group_id           NUMERIC NOT NULL,
    account_id                NUMERIC NOT NULL,
    group_name                VARCHAR(64),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT SystemGroupPermission_pk PRIMARY KEY (mgm_id, system_group_id, account_id)
);

ALTER TABLE system
    ADD COLUMN IF NOT EXISTS is_proxy BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS proxy_system_id NUMERIC,
    ADD COLUMN IF NOT EXISTS is_mgr_server BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE Package
    DROP COLUMN IF EXISTS channel_label;

ALTER TABLE Channel
    ADD COLUMN IF NOT EXISTS original_channel_id NUMERIC;

CREATE TABLE IF NOT EXISTS XccdScan
(
    mgm_id              NUMERIC NOT NULL,
    scan_id             NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    action_id           NUMERIC NOT NULL,
    name                VARCHAR(120),
    benchmark           VARCHAR(120),
    benchmark_version   VARCHAR(80),
    profile             VARCHAR(120),
    profile_title       VARCHAR(120),
    end_time            TIMESTAMPTZ,
    pass                NUMERIC,
    fail                NUMERIC,
    error               NUMERIC,
    not_selected        NUMERIC,
    informational       NUMERIC,
    other               NUMERIC,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT XccdScan_pk PRIMARY KEY (mgm_id, scan_id)
);

CREATE TABLE IF NOT EXISTS XccdScanResult
(
    mgm_id              NUMERIC NOT NULL,
    scan_id             NUMERIC NOT NULL,
    rule_id             NUMERIC NOT NULL,
    idref               VARCHAR(255),
    rulesystem          VARCHAR(80),
    system_id           NUMERIC NOT NULL,
    ident               VARCHAR(255),
    result              VARCHAR(16),
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT XccdScanResult_pk PRIMARY KEY (mgm_id, scan_id, rule_id)
);

CREATE TABLE IF NOT EXISTS SystemPackageInstalled
(
    mgm_id              NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    name                VARCHAR(256),
    epoch               VARCHAR(16),
    version             VARCHAR(512),
    release             VARCHAR(512),
    arch                VARCHAR(64),
    type                VARCHAR(10),
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp)
);

CREATE TABLE IF NOT EXISTS SystemPackageUpdate
(
    mgm_id              NUMERIC NOT NULL,
    system_id           NUMERIC NOT NULL,
    package_id          NUMERIC NOT NULL,
    name                VARCHAR(256),
    epoch               VARCHAR(16),
    version             VARCHAR(512),
    release             VARCHAR(512),
    arch                VARCHAR(64),
    type                VARCHAR(10),
    is_latest           BOOLEAN NOT NULL DEFAULT FALSE,
    synced_date         TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT SystemPackageUpdate_pk PRIMARY KEY (mgm_id, system_id, package_id)
);

CREATE TABLE IF NOT EXISTS ChannelPackage
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    package_id                NUMERIC NOT NULL,
    channel_label             VARCHAR(128),
    package_name              VARCHAR(256),
    package_epoch             VARCHAR(16),
    package_version           VARCHAR(512),
    package_release           VARCHAR(512),
    package_type              VARCHAR(10),
    package_arch              VARCHAR(64),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT ChannelPackage_pk PRIMARY KEY (mgm_id, channel_id, package_id)
);

CREATE TABLE IF NOT EXISTS ChannelErrata
(
    mgm_id                    NUMERIC NOT NULL,
    channel_id                NUMERIC NOT NULL,
    errata_id                 NUMERIC NOT NULL,
    channel_label             VARCHAR(128),
    advisory_name             VARCHAR(100),
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT ChannelErrata_pk PRIMARY KEY (mgm_id, channel_id, errata_id)
);

CREATE TABLE IF NOT EXISTS Account (
    mgm_id                    NUMERIC NOT NULL,
    account_id                NUMERIC NOT NULL,
    username                  VARCHAR(64),
    organization              VARCHAR(128),
    last_name                 VARCHAR(128),
    first_name                VARCHAR(128),
    position                  VARCHAR(128),
    email                     VARCHAR(128),
    creation_time             TIMESTAMPTZ,
    last_login_time           TIMESTAMPTZ,
    status                    VARCHAR(32),
    md5_encryption            BOOLEAN,
    synced_date               TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT Account_pk PRIMARY KEY (mgm_id, account_id)
);

CREATE TABLE IF NOT EXISTS AccountGroup (
    mgm_id                      NUMERIC NOT NULL,
    account_id                  NUMERIC NOT NULL,
    account_group_id            NUMERIC NOT NULL,
    username                    VARCHAR(64),
    account_group_name          VARCHAR(64),
    account_group_type_id       NUMERIC,
    account_group_type_name     VARCHAR(64),
    account_group_type_label    VARCHAR(64),
    synced_date                 TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT AccountGroup_pk PRIMARY KEY (mgm_id, account_id, account_group_id)
);

CREATE TABLE IF NOT EXISTS SystemCustomInfo
(
    mgm_id                  NUMERIC NOT NULL,
    system_id               NUMERIC NOT NULL,
    organization            VARCHAR(128) NOT NULL,
    key                     VARCHAR(64),
    description             VARCHAR(4000),
    value                   VARCHAR(4000),
    synced_date             TIMESTAMPTZ DEFAULT (current_timestamp),

    CONSTRAINT SystemCustomInfo_pk PRIMARY KEY (mgm_id, organization, system_id, key)
);

-- Views
DROP VIEW IF EXISTS ProxyOverviewReport;
CREATE OR REPLACE VIEW ProxyOverviewReport AS
    SELECT prx.mgm_id
              , prx.system_id AS proxy_id
              , prx.hostname AS proxy_name
              , sys.hostname AS system_name
              , sys.system_id
              , prx.synced_date
      FROM system prx
              INNER JOIN system sys ON sys.proxy_system_id = prx.system_id AND sys.mgm_id = prx.mgm_id
    WHERE prx.is_proxy
 ORDER BY prx.mgm_id, prx.system_id, sys.system_id
;

DROP VIEW IF EXISTS ScapScanReport;
CREATE OR REPLACE VIEW ScapScanReport AS
  SELECT XccdScan.mgm_id
            , XccdScan.scan_id
            , System.system_id
            , XccdScan.action_id
            , System.hostname
            , System.organization
            , SystemNetAddressV4.address AS ip_address
            , XccdScan.name
            , XccdScan.benchmark
            , XccdScan.benchmark_version
            , XccdScan.profile
            , XccdScan.profile_title
            , XccdScan.end_time
            , XccdScan.pass
            , XccdScan.fail
            , XccdScan.error
            , XccdScan.not_selected
            , XccdScan.informational
            , XccdScan.other
            , XccdScan.synced_date
    FROM XccdScan
            LEFT JOIN System ON ( XccdScan.mgm_id = System.mgm_id AND XccdScan.system_id = System.system_id )
            LEFT JOIN SystemNetInterface ON (System.mgm_id = SystemNetInterface.mgm_id AND System.system_id = SystemNetInterface.system_id AND SystemNetInterface.primary_interface)
            LEFT JOIN SystemNetAddressV4 ON (System.mgm_id = SystemNetAddressV4.mgm_id AND System.system_id = SystemNetAddressV4.system_id AND SystemNetInterface.interface_id = SystemNetAddressV4.interface_id)
ORDER BY XccdScan.mgm_id
            , XccdScan.scan_id
            , System.system_id
            , XccdScan.end_time
;

DROP VIEW IF EXISTS ScapScanResultReport;
CREATE OR REPLACE VIEW ScapScanResultReport AS
  SELECT XccdScanResult.mgm_id
            , XccdScanResult.scan_id
            , XccdScanResult.rule_id
            , XccdScanResult.idref
            , XccdScanResult.rulesystem
            , System.system_id
            , System.hostname
            , System.organization
            , XccdScanResult.ident
            , XccdScanResult.result
            , XccdScanResult.synced_date
    FROM XccdScanResult
            LEFT JOIN System ON ( XccdScanResult.mgm_id = System.mgm_id AND XccdScanResult.system_id = System.system_id )
ORDER BY mgm_id, scan_id, rule_id
;

DROP VIEW IF EXISTS SystemGroupsReport;
CREATE OR REPLACE VIEW SystemGroupsReport AS
  SELECT mgm_id
            , system_group_id
            , name
            , current_members
            , organization
            , synced_date
    FROM SystemGroup
ORDER BY mgm_id, system_group_id
;

DROP VIEW IF EXISTS SystemGroupsSystemsReport;
CREATE OR REPLACE VIEW SystemGroupsSystemsReport AS
   SELECT SystemGroupMember.mgm_id
              , SystemGroupMember.system_group_id AS group_id
              , SystemGroupMember.group_name AS group_name
              , System.system_id
              , System.profile_name AS system_name
              , SystemGroupMember.synced_date
     FROM SystemGroupMember
              INNER JOIN System ON ( SystemGroupMember.mgm_id = System.mgm_id AND SystemGroupMember.system_id = System.system_id )
 ORDER BY SystemGroupMember.mgm_id, SystemGroupMember.system_group_id, SystemGroupMember.system_id
;

DROP VIEW IF EXISTS SystemPackagesInstalledReport;
CREATE OR REPLACE VIEW SystemPackagesInstalledReport AS
  SELECT SystemPackageInstalled.mgm_id
            , SystemPackageInstalled.system_id
            , System.organization
            , SystemPackageInstalled.name AS package_name
            , SystemPackageInstalled.epoch AS package_epoch
            , SystemPackageInstalled.version AS package_version
            , SystemPackageInstalled.release AS package_release
            , SystemPackageInstalled.arch AS package_arch
            , SystemPackageInstalled.synced_date
    FROM SystemPackageInstalled
             INNER JOIN System ON System.system_id = SystemPackageInstalled.system_id
ORDER BY SystemPackageInstalled.mgm_id, SystemPackageInstalled.system_id, SystemPackageInstalled.name
;

DROP VIEW IF EXISTS SystemExtraPackagesReport;
CREATE OR REPLACE VIEW SystemExtraPackagesReport AS
  WITH packages_from_channels AS (
    SELECT SystemPackageInstalled.mgm_id
              , SystemPackageInstalled.system_id
              , Package.package_id
              , SystemPackageInstalled.name
              , SystemPackageInstalled.epoch
              , SystemPackageInstalled.version
              , SystemPackageInstalled.release
              , SystemPackageInstalled.arch
              , SystemPackageInstalled.type
    FROM SystemPackageInstalled
              INNER JOIN SystemChannel ON ( SystemPackageInstalled.mgm_id = SystemChannel.mgm_id AND SystemPackageInstalled.system_id = SystemChannel.system_id )
              INNER JOIN ChannelPackage ON ( SystemChannel.mgm_id = ChannelPackage.mgm_id AND ChannelPackage.channel_id = SystemChannel.channel_id)
              INNER JOIN Package ON (  SystemPackageInstalled.mgm_id = Package.mgm_id
                                                AND ChannelPackage.package_id  = Package.package_id 
                                                AND Package.name = SystemPackageInstalled.name
                                                AND COALESCE(Package.epoch, '') = COALESCE(SystemPackageInstalled.epoch, '')
                                                AND Package.version = SystemPackageInstalled.version
                                                AND Package.release = SystemPackageInstalled.release
                                                AND Package.arch = SystemPackageInstalled.arch
                                           )
  )
  SELECT System.mgm_id
             , System.system_id
             , System.hostname AS system_name
             , System.organization
             , SystemPackageInstalled.name AS package_name
             , SystemPackageInstalled.epoch AS package_epoch
             , SystemPackageInstalled.version AS package_version
             , SystemPackageInstalled.release AS package_release
             , SystemPackageInstalled.arch AS package_arch
             , SystemPackageInstalled.synced_date
    FROM System
           INNER JOIN SystemPackageInstalled ON ( System.mgm_id = SystemPackageInstalled.mgm_id AND System.system_id = SystemPackageInstalled.system_id )
           LEFT JOIN packages_from_channels ON ( SystemPackageInstalled.mgm_id = packages_from_channels.mgm_id AND SystemPackageInstalled.system_id = packages_from_channels.system_id
                                                     AND SystemPackageInstalled.name = packages_from_channels.name
                                                     AND COALESCE(SystemPackageInstalled.epoch, '') = COALESCE(packages_from_channels.epoch, '')
                                                     AND SystemPackageInstalled.version = packages_from_channels.version
                                                     AND SystemPackageInstalled.release = packages_from_channels.release
                                                     AND SystemPackageInstalled.arch = packages_from_channels.arch
                                               )
   WHERE packages_from_channels.package_id IS NULL
ORDER BY System.mgm_id, System.organization, System.system_id, SystemPackageInstalled.name
;

DROP VIEW IF EXISTS PackagesUpdatesAllReport;
CREATE OR REPLACE VIEW PackagesUpdatesAllReport AS
  SELECT System.mgm_id
            , System.system_id
            , System.organization
            , SystemPackageUpdate.name AS package_name
            , SystemPackageInstalled.epoch AS package_epoch
            , SystemPackageInstalled.version AS package_version
            , SystemPackageInstalled.release AS package_release
            , SystemPackageInstalled.arch AS package_arch
            , SystemPackageUpdate.epoch AS newer_epoch
            , SystemPackageUpdate.version AS newer_version
            , SystemPackageUpdate.release AS newer_release
            , SystemPackageUpdate.synced_date
    FROM System
            INNER JOIN SystemPackageUpdate ON ( System.mgm_id = SystemPackageUpdate.mgm_id AND System.system_id = SystemPackageUpdate.system_id )
            INNER JOIN SystemPackageInstalled ON ( System.mgm_id = SystemPackageInstalled.mgm_id AND System.system_id = SystemPackageInstalled.system_id AND SystemPackageInstalled.name = SystemPackageUpdate.name )
ORDER BY System.mgm_id, System.organization, System.system_id, SystemPackageUpdate.name
;

DROP VIEW IF EXISTS PackagesUpdatesNewestReport;
CREATE OR REPLACE VIEW PackagesUpdatesNewestReport AS
  SELECT System.mgm_id
            , System.system_id
            , System.organization
            , SystemPackageUpdate.name AS package_name
            , SystemPackageInstalled.epoch AS package_epoch
            , SystemPackageInstalled.version AS package_version
            , SystemPackageInstalled.release AS package_release
            , SystemPackageInstalled.arch AS package_arch
            , SystemPackageUpdate.epoch AS newer_epoch
            , SystemPackageUpdate.version AS newer_version
            , SystemPackageUpdate.release AS newer_release
            , SystemPackageUpdate.synced_date
    FROM System
            INNER JOIN SystemPackageUpdate ON ( System.mgm_id = SystemPackageUpdate.mgm_id AND System.system_id = SystemPackageUpdate.system_id )
            INNER JOIN SystemPackageInstalled ON ( System.mgm_id = SystemPackageInstalled.mgm_id AND System.system_id = SystemPackageInstalled.system_id AND SystemPackageInstalled.name = SystemPackageUpdate.name )
   WHERE SystemPackageUpdate.is_latest
ORDER BY System.mgm_id, System.organization, System.system_id, SystemPackageUpdate.name
;

DROP VIEW IF EXISTS ClonedChannelsReport;
CREATE OR REPLACE VIEW ClonedChannelsReport AS
  SELECT original.mgm_id
            , original.channel_id AS original_channel_id
            , original.label AS original_channel_label
            , original.name AS original_channel_name
            , cloned.channel_id AS new_channel_id
            , cloned.label AS new_channel_label
            , cloned.name AS new_channel_name
            , cloned.synced_date
    FROM Channel original
            INNER JOIN Channel cloned ON ( cloned.mgm_id = original.mgm_id AND cloned.original_channel_id = original.channel_id )
ORDER BY original.mgm_id, original.channel_id
;

DROP VIEW IF EXISTS ChannelPackagesReport;
CREATE OR REPLACE VIEW ChannelPackagesReport AS
  SELECT Channel.mgm_id
            , Channel.label AS channel_label
            , Channel.name AS channel_name
            , Package.name
            , Package.version
            , Package.release
            , Package.epoch
            , Package.arch
            , case when Package.epoch is not null then Package.epoch || ':' else '' end || Package.name || '-' || Package.version || '-' || Package.release || '.' || Package.arch AS full_package_name
            , Package.synced_date
    FROM Channel
            INNER JOIN ChannelPackage ON ( Channel.mgm_id = ChannelPackage.mgm_id AND Channel.channel_id = ChannelPackage.channel_id )
            INNER JOIN Package ON ( Channel.mgm_id = Package.mgm_id AND ChannelPackage.package_id = Package.package_id )
ORDER BY Channel.mgm_id, Channel.label, Package.name, Package.version, Package.release, Package.epoch, Package.arch
;

DROP VIEW IF EXISTS ErrataChannelsReport;
CREATE OR REPLACE VIEW ErrataChannelsReport AS
  SELECT mgm_id
            , advisory_name
            , errata_id
            , channel_label
            , channel_id
            , synced_date
    FROM ChannelErrata
ORDER BY mgm_id, advisory_name, errata_id, channel_label, channel_id
;

DROP VIEW IF EXISTS AccountsReport;
CREATE OR REPLACE VIEW AccountsReport AS
  SELECT Account.mgm_id
            , Account.organization
            , Account.account_id
            , Account.username
            , Account.last_name
            , Account.first_name
            , Account.position
            , Account.email
            , string_agg(AccountGroup.account_group_type_name, ';') AS roles
            , Account.creation_time
            , Account.last_login_time
            , Account.status
            , Account.md5_encryption
            , Account.synced_date
    FROM Account
            LEFT JOIN AccountGroup ON ( Account.mgm_id = AccountGroup.mgm_id AND Account.account_id = AccountGroup.account_id )
GROUP BY Account.mgm_id
            , Account.organization
            , Account.account_id
            , Account.username
            , Account.last_name
            , Account.first_name
            , Account.position
            , Account.email
            , Account.creation_time
            , Account.last_login_time
            , Account.status
            , Account.md5_encryption
            , Account.synced_date
ORDER BY Account.mgm_id, Account.organization, Account.account_id
;

DROP VIEW IF EXISTS AccountsSystemsReport;
CREATE OR REPLACE VIEW AccountsSystemsReport AS
  WITH org_admins AS (
      SELECT mgm_id, account_id
        FROM AccountGroup
       WHERE account_group_type_label = 'org_admin'
  ), system_users AS (
      SELECT true as is_admin
              , Account.mgm_id
              , Account.account_id
              , System.system_id
              , NULL as group_name
        FROM System
              INNER JOIN Account ON ( System.mgm_id = Account.mgm_id AND System.organization = Account.organization )
    UNION
      SELECT false AS is_admin
                , SystemGroupPermission.mgm_id
                , SystemGroupPermission.account_id
                , SystemGroupMember.system_id
                , SystemGroupPermission.group_name
        FROM SystemGroupPermission
                INNER JOIN SystemGroupMember ON ( SystemGroupPermission.mgm_id = SystemGroupMember.mgm_id AND SystemGroupPermission.system_group_id = SystemGroupMember.system_group_id )
  ), users_details AS (
    SELECT Account.mgm_id
              , Account.account_id
              , Account.username
              , Account.organization
              , org_admins.account_id IS NOT NULL AS is_admin
              , Account.synced_date
      FROM Account
              LEFT JOIN org_admins ON ( Account.mgm_id = org_admins.mgm_id AND Account.account_id = org_admins.account_id )
  )
  SELECT users_details.mgm_id
            , users_details.account_id
            , users_details.username
            , users_details.organization
            , system_users.system_id
            , system_users.group_name
            , users_details.is_admin
            , users_details.synced_date
    FROM users_details
            LEFT JOIN system_users ON ( users_details.mgm_id = system_users.mgm_id AND users_details.is_admin = system_users.is_admin AND users_details.account_id = system_users.account_id)
   WHERE system_users.system_id IS NOT NULL
ORDER BY users_details.mgm_id, users_details.account_id, system_users.system_id
;

DROP VIEW IF EXISTS CustomInfoReport;
CREATE OR REPLACE VIEW CustomInfoReport AS
  SELECT SystemCustomInfo.mgm_id
            , SystemCustomInfo.system_id
            , System.profile_name AS system_name
            , SystemCustomInfo.organization
            , SystemCustomInfo.key
            , SystemCustomInfo.value
            , SystemCustomInfo.synced_date
    FROM SystemCustomInfo
            INNER JOIN System ON (SystemCustomInfo.mgm_id = System.mgm_id AND SystemCustomInfo.system_id = System.system_id )
ORDER BY SystemCustomInfo.mgm_id, SystemCustomInfo.organization, SystemCustomInfo.system_id, SystemCustomInfo.key
;
