insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_channel_membership', 'Channel Memberships',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_channel_membership');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_channel_config_subscription', 'Config Channel Subscriptions',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_channel_config_subscription');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_channel_deploy_diff', 'Deploy/Diff Config Channel',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_channel_deploy_diff');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_tag_system', 'Tag System',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_tag_system');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_power_management', 'Power Management Operations',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_power_management');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_system_preferences', 'System Preferences',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_system_preferences');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_system_custom_values', 'System Custom Values',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_system_custom_values');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_add_rm_addon_type', 'Add/Remove Addon Type',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_add_rm_addon_type');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_system_lock', 'Lock System',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_system_lock');
insert into rhnFeature (id, label, name, created, modified)
select sequence_nextval('rhn_feature_seq'), 'ftr_system_audit', 'Audit System',
        current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnFeature where label = 'ftr_system_audit');
