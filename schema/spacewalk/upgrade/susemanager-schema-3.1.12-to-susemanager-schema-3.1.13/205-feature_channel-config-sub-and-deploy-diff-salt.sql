INSERT INTO rhnServerGroupTypeFeature (server_group_type_id, feature_id, created, modified)
SELECT lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_config_subscription'),  current_timestamp,current_timestamp FROM dual
WHERE NOT EXISTS ( SELECT 1 FROM rhnServerGroupTypeFeature   WHERE server_group_type_id = lookup_sg_type('salt_entitled')   AND feature_id = lookup_feature_type('ftr_channel_config_subscription') );

INSERT INTO rhnServerGroupTypeFeature (server_group_type_id, feature_id, created, modified)
SELECT lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_deploy_diff'),  current_timestamp,current_timestamp FROM dual
WHERE NOT EXISTS ( SELECT 1 FROM rhnServerGroupTypeFeature   WHERE server_group_type_id = lookup_sg_type('salt_entitled')   AND feature_id = lookup_feature_type('ftr_channel_deploy_diff') );
