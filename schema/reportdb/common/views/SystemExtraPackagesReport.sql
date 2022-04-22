
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
              INNER JOIN Package ON ( ChannelPackage.package_id  = Package.package_id 
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
