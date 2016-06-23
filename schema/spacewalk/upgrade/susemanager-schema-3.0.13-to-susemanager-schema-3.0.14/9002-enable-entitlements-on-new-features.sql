
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_membership'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_channel_membership') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_config_subscription'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_channel_config_subscription') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_deploy_diff'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_channel_deploy_diff') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_tag_system'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_tag_system') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_power_management'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_power_management') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_preferences'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_system_preferences') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_custom_values'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_system_custom_values') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_add_rm_addon_type'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_add_rm_addon_type') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_lock'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_system_lock') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_audit'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('enterprise_entitled')
      and feature_id = lookup_feature_type('ftr_system_audit') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_membership'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('salt_entitled')
      and feature_id = lookup_feature_type('ftr_channel_membership') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_system_custom_values'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('salt_entitled')
      and feature_id = lookup_feature_type('ftr_system_custom_values') );
