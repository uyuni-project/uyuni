DROP VIEW ChannelPackagesReport;
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

DROP VIEW SystemExtraPackagesReport;
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

ALTER TABLE channelpackage
    DROP COLUMN IF EXISTS package_name,
    DROP COLUMN IF EXISTS package_epoch,
    DROP COLUMN IF EXISTS package_version,
    DROP COLUMN IF EXISTS package_release,
    DROP COLUMN IF EXISTS package_type,
    DROP COLUMN IF EXISTS package_arch,
    DROP COLUMN IF EXISTS channel_label
;
