insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
select lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_power_management'),
        current_timestamp,current_timestamp from dual
where not exists ( select 1 from rhnServerGroupTypeFeature
    where server_group_type_id = lookup_sg_type('salt_entitled')
      and feature_id = lookup_feature_type('ftr_power_management') );
