-- ChannelsReport view was missing in previous update files
DROP VIEW IF EXISTS ChannelsReport;
CREATE OR REPLACE VIEW ChannelsReport AS
  SELECT Channel.mgm_id
            , Channel.channel_id
            , Channel.label AS channel_label
            , Channel.name AS channel_name
            , COUNT(ChannelPackage.channel_id) AS number_of_packages
            , Channel.organization
            , Channel.synced_date
    FROM Channel
            LEFT JOIN ChannelPackage ON ( Channel.mgm_id = ChannelPackage.mgm_id AND Channel.channel_id = ChannelPackage.channel_id )
GROUP BY Channel.mgm_id, Channel.channel_id, Channel.label, Channel.name, Channel.organization, Channel.synced_date
ORDER BY Channel.mgm_id, Channel.channel_id
;
