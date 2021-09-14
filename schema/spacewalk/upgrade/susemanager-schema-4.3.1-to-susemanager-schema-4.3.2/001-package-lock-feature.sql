insert into rhnFeature (id, label, name, created, modified)
  select sequence_nextval('rhn_feature_seq'), 'ftr_package_lock', 'Lock Packages',
         current_timestamp, current_timestamp from dual
   where not exists (select 1 from rhnFeature where label = 'ftr_package_lock');

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id, created, modified)
  select lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_package_lock'),
         current_timestamp,current_timestamp from dual
   where not exists ( select 1 from rhnServerGroupTypeFeature
                              where server_group_type_id = lookup_sg_type('enterprise_entitled')
                              and feature_id = lookup_feature_type('ftr_package_lock') );

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id, created, modified)
  select lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_package_lock'),
         current_timestamp,current_timestamp from dual
   where not exists ( select 1 from rhnServerGroupTypeFeature
                              where server_group_type_id = lookup_sg_type('salt_entitled')
                              and feature_id = lookup_feature_type('ftr_package_lock') );
