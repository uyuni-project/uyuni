--
-- Copyright (c) 2008--2010 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
--
--

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_package_updates'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_errata_updates'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_hardware_refresh'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_package_refresh'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_package_remove'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_auto_errata_updates'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_grouping'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_package_verify'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_profile_compare'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_proxy_capable'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_sat_capable'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_reboot'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_satellite_applet'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_osa_bus'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_daily_summary'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_kickstart'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_config'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_custom_info'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_delta_action'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_snapshotting'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_agent_smith'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_remote_command'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_membership'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_config_subscription'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_channel_deploy_diff'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_tag_system'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_power_management'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_preferences'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_custom_values'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_add_rm_addon_type'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_lock'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('enterprise_entitled'), lookup_feature_type('ftr_system_audit'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('bootstrap_entitled'), lookup_feature_type('ftr_kickstart'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('bootstrap_entitled'), lookup_feature_type('ftr_power_management'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('bootstrap_entitled'), lookup_feature_type('ftr_system_grouping'),
        current_timestamp,current_timestamp);

-- salt entitlement features

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_package_updates'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_package_refresh'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_package_remove'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_profile_compare'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_errata_updates'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_daily_summary'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_custom_info'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_system_grouping'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_reboot'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_remote_command'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_hardware_refresh'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_membership'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_config_subscription'),
        current_timestamp,current_timestamp);
insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_channel_deploy_diff'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_system_preferences'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_system_custom_values'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_power_management'),
        current_timestamp,current_timestamp);

insert into rhnServerGroupTypeFeature (server_group_type_id, feature_id,
                                       created, modified)
values (lookup_sg_type('salt_entitled'), lookup_feature_type('ftr_config'),
        current_timestamp,current_timestamp);
