--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

-- WebUI permissions

-- Namespace: audit.*
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN ('audit.coco', 'audit.cve', 'audit.openscap')
    ON CONFLICT DO NOTHING;

-- Namespace: clm.*
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'clm.filter.list', 'clm.project.build', 'clm.project.details', 'clm.project.environments',
        'clm.project.filters', 'clm.project.list', 'clm.project.sources'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: cm.*
-- Permit 'view' to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace LIKE 'cm.%'
    AND ns.access_mode = 'R'
    ON CONFLICT DO NOTHING;
-- Permit 'modify' to 'image_admin'
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'image_admin'
    AND ns.namespace LIKE 'cm.%'
    AND ns.access_mode = 'W'
    ON CONFLICT DO NOTHING;

-- Namespace: config.*
-- Namespace: systems.config.*
-- Namespace: systems.autoinstallation.*
-- Permit to 'config_admin'
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'config_admin'
    AND (ns.namespace LIKE 'config.%' OR
        ns.namespace LIKE 'systems.config.%' OR
        ns.namespace = 'systems.autoinstallation' OR
        ns.namespace = 'systems.autoinstallation.provisioning')
    ON CONFLICT DO NOTHING;

-- Namespace: home.*
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace LIKE 'home.%'
    ON CONFLICT DO NOTHING;

-- Namespace: patches.details.*
-- Namespace: patches.list
-- Namespace: patches.search
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE (ns.namespace LIKE 'patches.details.%' OR
        ns.namespace = 'patches.list' OR
        ns.namespace = 'patches.search')
    ON CONFLICT DO NOTHING;

-- Namespace: salt.*
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace LIKE 'salt.%'
    ON CONFLICT DO NOTHING;

-- Namespace: schedule.*
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace LIKE 'schedule.%'
    ON CONFLICT DO NOTHING;

-- Namespace: software.details.*
-- Namespace: software.manage.*
-- Namespace: software.distro
-- Namespace: software.list
-- Namespace: software.search
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE (ns.namespace LIKE 'software.details.%' OR
        ns.namespace LIKE 'software.manage.%' OR
        ns.namespace = 'software.distro' OR
        ns.namespace = 'software.list' OR
        ns.namespace = 'software.search')
    ON CONFLICT DO NOTHING;

-- Namespace: patches.manage.*
-- Namespace: patches.clone
-- Namespace: users.channels
-- Permit to 'channel_admin'
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'channel_admin'
    AND (ns.namespace LIKE 'patches.manage.%' OR
        ns.namespace = 'patches.clone' OR
        ns.namespace = 'users.channels')
    ON CONFLICT DO NOTHING;

-- Namespace: systems.ansible
-- Namespace: systems.audit.*
-- Namespace: systems.bootstrap
-- Namespace: systems.custom_data
-- Namespace: systems.details.*
-- Namespace: systems.events
-- Namespace: systems.formulas
-- Namespace: systems.groups.*
-- Namespace: systems.list
-- Namespace: systems.maintenance
-- Namespace: systems.profiles
-- Namespace: systems.proxy
-- Namespace: systems.recurring
-- Namespace: systems.search
-- Namespace: systems.snapshots
-- Namespace: systems.software.*
-- Namespace: systems.ssm
-- Namespace: systems.states.*
-- Namespace: systems.vhms
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE (ns.namespace = 'systems.ansible' OR
        ns.namespace LIKE 'systems.audit.%' OR
        ns.namespace = 'systems.bootstrap' OR
        ns.namespace = 'systems.custom_data' OR
        ns.namespace LIKE 'systems.details.%' OR
        ns.namespace = 'systems.events' OR
        ns.namespace = 'systems.formulas' OR
        ns.namespace LIKE 'systems.groups.%' OR
        ns.namespace = 'systems.list' OR
        ns.namespace = 'systems.maintenance' OR
        ns.namespace = 'systems.profiles' OR
        ns.namespace = 'systems.proxy' OR
        ns.namespace = 'systems.recurring' OR
        ns.namespace = 'systems.search' OR
        ns.namespace = 'systems.snapshots' OR
        ns.namespace LIKE 'systems.software.%' OR
        ns.namespace = 'systems.ssm' OR
        ns.namespace LIKE 'systems.states.%' OR
        ns.namespace = 'systems.vhms')
    ON CONFLICT DO NOTHING;

-- Namespace: users.details
-- Namespace: users.group_config
-- Namespace: users.groups
-- Namespace: users.list.*
-- Namespace: users.preferences
-- Namespace: users.systems
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE (ns.namespace = 'users.details' OR
        ns.namespace = 'users.group_config' OR
        ns.namespace = 'users.groups' OR
        ns.namespace LIKE 'users.list.%' OR
        ns.namespace = 'users.preferences' OR
        ns.namespace = 'users.systems')
    ON CONFLICT DO NOTHING;

-- Namespace: systems.activation_keys.*
-- Permit to 'activation_key_admin'
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'activation_key_admin'
    AND ns.namespace LIKE 'systems.activation_keys.%'
    ON CONFLICT DO NOTHING;

-- API permissions

-- Namespace: actionchain
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.actionchain.add_apply_highstate',
        'api.actionchain.add_configuration_deployment',
        'api.actionchain.add_errata_update',
        'api.actionchain.add_package_install',
        'api.actionchain.add_package_removal',
        'api.actionchain.add_package_upgrade',
        'api.actionchain.add_package_verify',
        'api.actionchain.add_script_run',
        'api.actionchain.add_system_reboot',
        'api.actionchain.create_chain',
        'api.actionchain.delete_chain',
        'api.actionchain.list_chain_actions',
        'api.actionchain.list_chains',
        'api.actionchain.remove_action',
        'api.actionchain.rename_chain',
        'api.actionchain.schedule_chain'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: activationkey
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.activationkey.add_app_streams',
        'api.activationkey.add_child_channels',
        'api.activationkey.add_config_channels',
        'api.activationkey.add_entitlements',
        'api.activationkey.add_packages',
        'api.activationkey.add_server_groups',
        'api.activationkey.check_config_deployment',
        'api.activationkey.clone',
        'api.activationkey.create',
        'api.activationkey.delete',
        'api.activationkey.disable_config_deployment',
        'api.activationkey.enable_config_deployment',
        'api.activationkey.get_details',
        'api.activationkey.list_activated_systems',
        'api.activationkey.list_activation_keys',
        'api.activationkey.list_channels',
        'api.activationkey.list_config_channels',
        'api.activationkey.remove_app_streams',
        'api.activationkey.remove_child_channels',
        'api.activationkey.remove_config_channels',
        'api.activationkey.remove_entitlements',
        'api.activationkey.remove_packages',
        'api.activationkey.remove_server_groups',
        'api.activationkey.set_config_channels',
        'api.activationkey.set_details'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: admin.configuration
-- Namespace: admin.monitoring
-- Namespace: admin.payg
-- Permit to none

-- Namespace: ansible
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.ansible.create_ansible_path',
        'api.ansible.discover_playbooks',
        'api.ansible.fetch_playbook_contents',
        'api.ansible.introspect_inventory',
        'api.ansible.list_ansible_paths',
        'api.ansible.lookup_ansible_path_by_id',
        'api.ansible.remove_ansible_path',
        'api.ansible.schedule_playbook',
        'api.ansible.update_ansible_path'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: audit
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.audit.list_images_by_patch_status',
        'api.audit.list_systems_by_patch_status'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: channel
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.channel.list_all_channels',
        'api.channel.list_manageable_channels',
        'api.channel.list_my_channels',
        'api.channel.list_popular_channels',
        'api.channel.list_retired_channels',
        'api.channel.list_shared_channels',
        'api.channel.list_software_channels',
        'api.channel.list_vendor_channels'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: channel.access
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.channel.access.disable_user_restrictions',
        'api.channel.access.enable_user_restrictions',
        'api.channel.access.get_org_sharing',
        'api.channel.access.set_org_sharing'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: channel.appstreams
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.channel.appstreams.is_modular',
        'api.channel.appstreams.list_modular',
        'api.channel.appstreams.list_module_streams'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: channel.org
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.channel.org.disable_access',
        'api.channel.org.enable_access',
        'api.channel.org.list'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: channel.software
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.channel.software.add_packages',
        'api.channel.software.add_repo_filter',
        'api.channel.software.align_metadata',
        'api.channel.software.apply_channel_state',
        'api.channel.software.associate_repo',
        'api.channel.software.clear_repo_filters',
        'api.channel.software.create_repo',
        'api.channel.software.delete',
        'api.channel.software.disassociate_repo',
        'api.channel.software.get_channel_last_build_by_id',
        'api.channel.software.get_details',
        'api.channel.software.get_repo_details',
        'api.channel.software.get_repo_sync_cron_expression',
        'api.channel.software.is_existing',
        'api.channel.software.is_globally_subscribable',
        'api.channel.software.is_user_manageable',
        'api.channel.software.is_user_subscribable',
        'api.channel.software.list_all_packages',
        'api.channel.software.list_channel_repos',
        'api.channel.software.list_children',
        'api.channel.software.list_errata',
        'api.channel.software.list_errata_by_type',
        'api.channel.software.list_errata_needing_sync',
        'api.channel.software.list_latest_packages',
        'api.channel.software.list_repo_filters',
        'api.channel.software.list_system_channels',
        'api.channel.software.list_user_repos',
        'api.channel.software.merge_packages',
        'api.channel.software.remove_packages',
        'api.channel.software.remove_repo',
        'api.channel.software.remove_repo_filter',
        'api.channel.software.set_globally_subscribable',
        'api.channel.software.set_repo_filters',
        'api.channel.software.set_user_manageable',
        'api.channel.software.set_user_subscribable',
        'api.channel.software.sync_errata',
        'api.channel.software.sync_repo',
        'api.channel.software.update_repo',
        'api.channel.software.update_repo_label',
        'api.channel.software.update_repo_ssl',
        'api.channel.software.update_repo_url'
    )
    ON CONFLICT DO NOTHING;
-- Permit to channel_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'channel_admin'
    AND ns.namespace IN (
        'api.channel.software.list_arches',
        'api.channel.software.set_details',
        'api.channel.software.create',
        'api.channel.software.set_contact_details',
        'api.channel.software.list_subscribed_systems',
        'api.channel.software.remove_errata',
        'api.channel.software.list_packages_without_channel',
        'api.channel.software.clone',
        'api.channel.software.merge_errata',
        'api.channel.software.regenerate_needed_cache',
        'api.channel.software.regenerate_yum_cache',
        'api.channel.software.list_vendor_repo_filters',
        'api.channel.software.clear_vendor_repo_filters',
        'api.channel.software.set_vendor_repo_filters',
        'api.channel.software.remove_vendor_repo_filter',
        'api.channel.software.add_vendor_repo_filter'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: configchannel
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.configchannel.channel_exists',
        'api.configchannel.create_or_update_path',
        'api.configchannel.create_or_update_symlink',
        'api.configchannel.delete_file_revisions',
        'api.configchannel.delete_files',
        'api.configchannel.deploy_all_systems',
        'api.configchannel.get_details',
        'api.configchannel.get_encoded_file_revision',
        'api.configchannel.get_file_revision',
        'api.configchannel.get_file_revisions',
        'api.configchannel.list_assigned_system_groups',
        'api.configchannel.list_files',
        'api.configchannel.list_globals',
        'api.configchannel.list_subscribed_systems',
        'api.configchannel.lookup_channel_info',
        'api.configchannel.lookup_file_info',
        'api.configchannel.schedule_file_comparisons',
        'api.configchannel.sync_salt_files_on_disk',
        'api.configchannel.update',
        'api.configchannel.update_init_sls'
    )
    ON CONFLICT DO NOTHING;
-- Permit to config_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'config_admin'
    AND ns.namespace IN (
        'api.configchannel.create',
        'api.configchannel.delete_channels'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: contentmanagement
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.contentmanagement.attach_filter',
        'api.contentmanagement.attach_source',
        'api.contentmanagement.build_project',
        'api.contentmanagement.generate_project_difference',
        'api.contentmanagement.generate_environment_difference',
        'api.contentmanagement.create_app_stream_filters',
        'api.contentmanagement.create_environment',
        'api.contentmanagement.create_filter',
        'api.contentmanagement.create_project',
        'api.contentmanagement.detach_filter',
        'api.contentmanagement.detach_source',
        'api.contentmanagement.list_environment_difference'
        'api.contentmanagement.list_filter_criteria',
        'api.contentmanagement.list_filters',
        'api.contentmanagement.list_project_environments',
        'api.contentmanagement.list_project_filters',
        'api.contentmanagement.list_project_sources',
        'api.contentmanagement.list_projects',
        'api.contentmanagement.lookup_environment',
        'api.contentmanagement.lookup_filter',
        'api.contentmanagement.lookup_project',
        'api.contentmanagement.lookup_source',
        'api.contentmanagement.promote_project',
        'api.contentmanagement.remove_environment',
        'api.contentmanagement.remove_filter',
        'api.contentmanagement.remove_project',
        'api.contentmanagement.update_environment',
        'api.contentmanagement.update_filter',
        'api.contentmanagement.update_project'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: distchannel
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        -- 'api.distchannel.list_maps_for_org' is sat_admin only
        'api.distchannel.list_default_maps',
        'api.distchannel.set_map_for_org'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: errata
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.errata.add_packages',
        'api.errata.applicable_to_channels',
        'api.errata.bugzilla_fixes',
        'api.errata.clone',
        'api.errata.clone_as_original',
        'api.errata.clone_as_original_async',
        'api.errata.clone_async',
        'api.errata.create',
        'api.errata.delete',
        'api.errata.find_by_cve',
        'api.errata.get_details',
        'api.errata.list_affected_systems',
        'api.errata.list_cves',
        'api.errata.list_keywords',
        'api.errata.list_packages',
        'api.errata.publish',
        'api.errata.publish_as_original',
        'api.errata.remove_packages',
        'api.errata.set_details'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: formula
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.formula.get_combined_formula_data_by_server_ids',
        'api.formula.get_combined_formulas_by_server_id',
        'api.formula.get_formulas_by_group_id',
        'api.formula.get_formulas_by_server_id',
        'api.formula.get_group_formula_data',
        'api.formula.get_system_formula_data',
        'api.formula.list_formulas',
        'api.formula.set_formulas_of_group',
        'api.formula.set_formulas_of_server',
        'api.formula.set_group_formula_data',
        'api.formula.set_system_formula_data'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: image
-- Namespace: image.delta
-- Namespace: image.profile
-- Namespace: image.store
-- Permit to image_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'image_admin'
    AND ns.namespace IN (
        'api.image.add_image_file',
        'api.image.delete',
        'api.image.delete_image_file',
        'api.image.delta.create_delta_image',
        'api.image.delta.get_details',
        'api.image.delta.list_deltas',
        'api.image.get_custom_values',
        'api.image.get_details',
        'api.image.get_pillar',
        'api.image.get_relevant_errata',
        'api.image.import_container_image',
        'api.image.import_image',
        'api.image.import_o_s_image',
        'api.image.list_images',
        'api.image.list_packages',
        'api.image.profile.create',
        'api.image.profile.delete',
        'api.image.profile.delete_custom_values',
        'api.image.profile.get_custom_values',
        'api.image.profile.get_details',
        'api.image.profile.list_image_profiles',
        'api.image.profile.list_image_profile_types',
        'api.image.profile.set_custom_values',
        'api.image.profile.set_details',
        'api.image.schedule_image_build',
        'api.image.set_pillar',
        'api.image.store.create',
        'api.image.store.delete',
        'api.image.store.get_details',
        'api.image.store.list_image_stores',
        'api.image.store.list_image_store_types',
        'api.image.store.set_details'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: kickstart
-- Namespace: kickstart.filepreservation
-- Namespace: kickstart.keys
-- Namespace: kickstart.snippet
-- Namespace: kickstart.tree
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.kickstart.filepreservation.create',
        'api.kickstart.filepreservation.delete',
        'api.kickstart.filepreservation.get_details',
        'api.kickstart.filepreservation.list_all_file_preservations',
        'api.kickstart.clone_profile',
        'api.kickstart.create_profile',
        'api.kickstart.create_profile_with_custom_url',
        'api.kickstart.find_kickstart_for_ip',
        'api.kickstart.import_file',
        'api.kickstart.import_raw_file',
        'api.kickstart.is_profile_disabled',
        'api.kickstart.rename_profile',
        'api.kickstart.tree.list_install_types'
    )
    ON CONFLICT DO NOTHING;
-- Permit to config_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'config_admin'
    AND ns.namespace IN (
        'api.kickstart.list_kickstartable_channels',
        'api.kickstart.list_autoinstallable_channels',
        'api.kickstart.list_kickstarts',
        'api.kickstart.list_all_ip_ranges',
        'api.kickstart.delete_profile',
        'api.kickstart.disable_profile',
        'api.kickstart.keys.create',
        'api.kickstart.keys.delete',
        'api.kickstart.keys.get_details',
        'api.kickstart.keys.list_all_keys',
        'api.kickstart.keys.update',
        'api.kickstart.snippet.create_or_update',
        'api.kickstart.snippet.delete',
        'api.kickstart.snippet.list_all',
        'api.kickstart.snippet.list_custom',
        'api.kickstart.snippet.list_default',
        'api.kickstart.tree.create',
        'api.kickstart.tree.delete',
        'api.kickstart.tree.delete_tree_and_profiles',
        'api.kickstart.tree.get_details',
        'api.kickstart.tree.list',
        'api.kickstart.tree.rename',
        'api.kickstart.tree.update'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: kickstart.profile
-- Namespace: kickstart.profile.keys
-- Namespace: kickstart.profile.software
-- Namespace: kickstart.profile.system
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.kickstart.profile.add_ip_range',
        'api.kickstart.profile.compare_activation_keys',
        'api.kickstart.profile.compare_advanced_options',
        'api.kickstart.profile.compare_packages',
        'api.kickstart.profile.download_kickstart',
        'api.kickstart.profile.download_rendered_kickstart',
        'api.kickstart.profile.get_advanced_options',
        'api.kickstart.profile.get_child_channels',
        'api.kickstart.profile.get_custom_options',
        'api.kickstart.profile.get_kickstart_tree',
        'api.kickstart.profile.get_update_type',
        'api.kickstart.profile.get_variables',
        'api.kickstart.profile.get_virtualization_type',
        'api.kickstart.profile.set_advanced_options',
        'api.kickstart.profile.set_child_channels',
        'api.kickstart.profile.set_custom_options',
        'api.kickstart.profile.set_kickstart_tree',
        'api.kickstart.profile.set_update_type',
        'api.kickstart.profile.set_variables',
        'api.kickstart.profile.set_virtualization_type',
        'api.kickstart.profile.software.get_software_details',
        'api.kickstart.profile.software.set_software_details',
        'api.kickstart.profile.system.add_file_preservations',
        'api.kickstart.profile.system.add_keys',
        'api.kickstart.profile.system.get_partitioning_scheme',
        'api.kickstart.profile.system.list_file_preservations',
        'api.kickstart.profile.system.list_keys',
        'api.kickstart.profile.system.remove_file_preservations',
        'api.kickstart.profile.system.remove_keys',
        'api.kickstart.profile.system.set_partitioning_scheme'
    )
    ON CONFLICT DO NOTHING;
-- Permit to config_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'config_admin'
    AND ns.namespace IN (
        'api.kickstart.profile.get_cfg_preservation',
        'api.kickstart.profile.set_cfg_preservation',
        'api.kickstart.profile.set_logging',
        'api.kickstart.profile.list_scripts',
        'api.kickstart.profile.order_scripts',
        'api.kickstart.profile.add_script',
        'api.kickstart.profile.remove_script',
        'api.kickstart.profile.list_ip_ranges',
        'api.kickstart.profile.remove_ip_range',
        'api.kickstart.profile.get_available_repositories',
        'api.kickstart.profile.get_repositories',
        'api.kickstart.profile.set_repositories',
        'api.kickstart.profile.keys.add_activation_key',
        'api.kickstart.profile.keys.get_activation_keys',
        'api.kickstart.profile.keys.remove_activation_key',
        'api.kickstart.profile.software.append_to_software_list',
        'api.kickstart.profile.software.get_software_list',
        'api.kickstart.profile.software.set_software_list',
        'api.kickstart.profile.system.check_config_management',
        'api.kickstart.profile.system.check_remote_commands',
        'api.kickstart.profile.system.disable_config_management',
        'api.kickstart.profile.system.disable_remote_commands',
        'api.kickstart.profile.system.enable_config_management',
        'api.kickstart.profile.system.enable_remote_commands',
        'api.kickstart.profile.system.get_locale',
        'api.kickstart.profile.system.set_locale',
        'api.kickstart.profile.system.get_registration_type',
        'api.kickstart.profile.system.set_registration_type',
        'api.kickstart.profile.system.get_selinux',
        'api.kickstart.profile.system.set_selinux'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: maintenance
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.maintenance.assign_schedule_to_systems',
        'api.maintenance.create_calendar',
        'api.maintenance.create_calendar_with_url',
        'api.maintenance.create_schedule',
        'api.maintenance.delete_calendar',
        'api.maintenance.delete_schedule',
        'api.maintenance.get_calendar_details',
        'api.maintenance.get_schedule_details',
        'api.maintenance.list_calendar_labels',
        'api.maintenance.list_schedule_names',
        'api.maintenance.list_systems_with_schedule',
        'api.maintenance.refresh_calendar',
        'api.maintenance.retract_schedule_from_systems',
        'api.maintenance.update_calendar',
        'api.maintenance.update_schedule'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: org
-- Namespace: org.trusts
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.org.migrate_systems',
        'api.org.transfer_systems',
        'api.org.is_org_config_managed_by_org_admin',
        'api.org.is_errata_email_notifs_for_org',
        'api.org.set_errata_email_notifs_for_org',
        'api.org.get_clm_sync_patches_config',
        'api.org.set_clm_sync_patches_config',
        'api.org.trusts.list_orgs',
        'api.org.trusts.list_channels_provided',
        'api.org.trusts.list_channels_consumed',
        'api.org.trusts.get_details'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: packages
-- Namespace: packages.search
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.packages.find_by_nvrea',
        'api.packages.get_details',
        'api.packages.get_package',
        'api.packages.get_package_url',
        'api.packages.list_changelog',
        'api.packages.list_dependencies',
        'api.packages.list_files',
        'api.packages.list_providing_channels',
        'api.packages.list_providing_errata',
        'api.packages.list_source_packages',
        'api.packages.remove_package',
        'api.packages.remove_source_package',
        'api.packages.search.advanced',
        'api.packages.search.advanced_with_act_key',
        'api.packages.search.advanced_with_channel',
        'api.packages.search.name',
        'api.packages.search.name_and_description',
        'api.packages.search.name_and_summary'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: packages.provider
-- Permit to none

-- Namespace: preferences.locale
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.preferences.locale.list_locales',
        'api.preferences.locale.list_time_zones',
        'api.preferences.locale.set_locale',
        'api.preferences.locale.set_time_zone'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: proxy
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.proxy.activate_proxy',
        'api.proxy.bootstrap_proxy',
        'api.proxy.container_config',
        'api.proxy.create_monitoring_scout',
        'api.proxy.deactivate_proxy',
        'api.proxy.is_proxy',
        'api.proxy.list_available_proxy_channels',
        'api.proxy.list_proxies',
        'api.proxy.list_proxy_clients'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: recurring
-- Namespace: recurring.custom
-- Namespace: recurring.highstate
-- Namespace: recurring.playbook
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.recurring.delete',
        'api.recurring.list_by_entity',
        'api.recurring.lookup_by_id',
        'api.recurring.custom.create',
        'api.recurring.custom.list_available',
        'api.recurring.custom.update',
        'api.recurring.highstate.create',
        'api.recurring.highstate.update',
        'api.recurring.playbook.create',
        'api.recurring.playbook.update'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: saltkey
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.saltkey.accept',
        'api.saltkey.accepted_list',
        'api.saltkey.delete',
        'api.saltkey.denied_list',
        'api.saltkey.pending_list',
        'api.saltkey.reject',
        'api.saltkey.rejected_list'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: schedule
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.schedule.archive_actions',
        'api.schedule.cancel_actions',
        'api.schedule.delete_actions',
        'api.schedule.fail_system_action',
        'api.schedule.list_all_actions',
        'api.schedule.list_all_archived_actions',
        'api.schedule.list_all_completed_actions',
        'api.schedule.list_archived_actions',
        'api.schedule.list_completed_actions',
        'api.schedule.list_completed_systems',
        'api.schedule.list_failed_actions',
        'api.schedule.list_failed_systems',
        'api.schedule.list_in_progress_actions',
        'api.schedule.list_in_progress_systems',
        'api.schedule.reschedule_actions'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: subscriptionmatching.pinnedsubscription
-- Permit to none

-- Namespace: sync.content
-- Namespace: sync.hub
-- Namespace: sync.master
-- Namespace: sync.slave
-- Permit to none

-- Namespace: system
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.system.add_entitlements',
        'api.system.add_note',
        'api.system.bootstrap',
        'api.system.bootstrap_with_private_ssh_key',
        'api.system.change_proxy',
        'api.system.compare_package_profile',
        'api.system.compare_packages',
        'api.system.create_package_profile',
        'api.system.create_system_profile',
        'api.system.create_system_record',
        'api.system.delete_custom_values',
        'api.system.delete_guest_profiles',
        'api.system.delete_note',
        'api.system.delete_notes',
        'api.system.delete_package_profile',
        'api.system.delete_system',
        'api.system.delete_systems',
        'api.system.delete_tag_from_snapshot',
        'api.system.download_system_id',
        'api.system.get_co_co_attestation_config',
        'api.system.get_co_co_attestation_result_details',
        'api.system.get_connection_path',
        'api.system.get_cpu',
        'api.system.get_custom_values',
        'api.system.get_details',
        'api.system.get_devices',
        'api.system.get_dmi',
        'api.system.get_entitlements',
        'api.system.get_event_details',
        'api.system.get_event_history',
        'api.system.get_id',
        'api.system.get_installed_products',
        'api.system.get_kernel_live_patch',
        'api.system.get_latest_co_co_attestation_report',
        'api.system.get_memory',
        'api.system.get_minion_id_map',
        'api.system.get_name',
        'api.system.get_network',
        'api.system.get_network_devices',
        'api.system.get_network_for_systems',
        'api.system.get_osa_ping',
        'api.system.get_registration_date',
        'api.system.get_relevant_errata',
        'api.system.get_relevant_errata_by_type',
        'api.system.get_running_kernel',
        'api.system.get_script_action_details',
        'api.system.get_script_results',
        'api.system.get_subscribed_base_channel',
        'api.system.get_system_currency_multipliers',
        'api.system.get_system_currency_scores',
        'api.system.get_unscheduled_errata',
        'api.system.get_uuid',
        'api.system.get_variables',
        'api.system.has_traditional_systems',
        'api.system.is_nvre_installed',
        'api.system.list_activation_keys',
        'api.system.list_active_systems',
        'api.system.list_active_systems_details',
        'api.system.list_administrators',
        'api.system.list_all_installable_packages',
        'api.system.list_co_co_attestation_reports',
        'api.system.list_duplicates_by_hostname',
        'api.system.list_duplicates_by_ip',
        'api.system.list_duplicates_by_mac',
        'api.system.list_empty_system_profiles',
        'api.system.list_extra_packages',
        'api.system.list_fqdns',
        'api.system.list_groups',
        'api.system.list_inactive_systems',
        'api.system.list_installed_packages',
        'api.system.list_latest_available_package',
        'api.system.list_latest_installable_packages',
        'api.system.list_latest_upgradable_packages',
        'api.system.list_migration_targets',
        'api.system.list_newer_installed_packages',
        'api.system.list_notes',
        'api.system.list_older_installed_packages',
        'api.system.list_out_of_date_systems',
        'api.system.list_package_profiles',
        'api.system.list_package_state',
        'api.system.list_packages',
        'api.system.list_packages_from_channel',
        'api.system.list_packages_lock_status',
        'api.system.list_physical_systems',
        'api.system.list_subscribable_base_channels',
        'api.system.list_subscribable_child_channels',
        'api.system.list_subscribed_child_channels',
        'api.system.list_suggested_reboot',
        'api.system.list_system_events',
        'api.system.list_system_groups_for_systems_with_entitlement',
        'api.system.list_systems',
        'api.system.list_systems_with_entitlement',
        'api.system.list_systems_with_extra_packages',
        'api.system.list_systems_with_package',
        'api.system.list_ungrouped_systems',
        'api.system.list_user_systems',
        'api.system.list_virtual_guests',
        'api.system.list_virtual_hosts',
        'api.system.obtain_reactivation_key',
        'api.system.provision_system',
        'api.system.provision_virtual_guest',
        'api.system.refresh_pillar',
        'api.system.register_peripheral_server',
        'api.system.remove_entitlements',
        'api.system.schedule_apply_errata',
        'api.system.schedule_apply_highstate',
        'api.system.schedule_apply_states',
        'api.system.schedule_certificate_update',
        'api.system.schedule_change_channels',
        'api.system.schedule_co_co_attestation',
        'api.system.schedule_dist_upgrade',
        'api.system.schedule_hardware_refresh',
        'api.system.schedule_package_install',
        'api.system.schedule_package_install_by_nevra',
        'api.system.schedule_package_lock_change',
        'api.system.schedule_package_refresh',
        'api.system.schedule_package_remove',
        'api.system.schedule_package_remove_by_nevra',
        'api.system.schedule_package_update',
        'api.system.schedule_product_migration',
        'api.system.schedule_reboot',
        'api.system.schedule_s_p_migration',
        'api.system.schedule_script_run',
        'api.system.schedule_sync_packages_with_system',
        'api.system.search_by_name',
        'api.system.send_osa_ping',
        'api.system.set_base_channel',
        'api.system.set_child_channels',
        'api.system.set_co_co_attestation_config',
        'api.system.set_custom_values',
        'api.system.set_details',
        'api.system.set_lock_status',
        'api.system.set_primary_fqdn',
        'api.system.set_primary_interface',
        'api.system.set_profile_name',
        'api.system.set_variables',
        'api.system.tag_latest_snapshot',
        'api.system.unentitle',
        'api.system.update_package_state',
        'api.system.update_peripheral_server_info',
        'api.system.upgrade_entitlement',
        'api.system.who_registered'
    )
    ON CONFLICT DO NOTHING;
-- Permit to image_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'image_admin'
    AND ns.namespace IN (
        'api.system.get_pillar',
        'api.system.set_pillar'
    )
    ON CONFLICT DO NOTHING;
-- Permit to system_group_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'system_group_admin'
    AND ns.namespace = 'api.system.set_group_membership'
    ON CONFLICT DO NOTHING;

-- Namespace: system.appstreams
-- Namespace: system.config
-- Namespace: system.custominfo
-- Namespace: system.monitoring
-- Namespace: system.scap
-- Namespace: system.search
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.system.custominfo.create_key',
        'api.system.custominfo.delete_key',
        'api.system.custominfo.list_all_keys',
        'api.system.custominfo.update_key',
        'api.system.config.add_channels',
        'api.system.config.create_or_update_path',
        'api.system.config.create_or_update_symlink',
        'api.system.config.delete_files',
        'api.system.config.deploy_all',
        'api.system.config.list_channels',
        'api.system.config.list_files',
        'api.system.config.lookup_file_info',
        'api.system.config.remove_channels',
        'api.system.config.schedule_apply_config_channel',
        'api.system.config.set_channels',
        'api.system.appstreams.disable',
        'api.system.appstreams.enable',
        'api.system.appstreams.list_module_streams',
        'api.system.monitoring.list_endpoints',
        'api.system.scap.delete_xccdf_scan',
        'api.system.scap.get_xccdf_scan_details',
        'api.system.scap.get_xccdf_scan_rule_results',
        'api.system.scap.list_xccdf_scans',
        'api.system.scap.schedule_xccdf_scan',
        'api.system.search.device_description',
        'api.system.search.device_driver',
        'api.system.search.device_id',
        'api.system.search.device_vendor_id',
        'api.system.search.hostname',
        'api.system.search.ip',
        'api.system.search.name_and_description',
        'api.system.search.uuid'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: system.provisioning.powermanagement
-- Namespace: system.provisioning.snapshot
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.system.provisioning.powermanagement.get_details',
        'api.system.provisioning.powermanagement.get_status',
        'api.system.provisioning.powermanagement.list_types',
        'api.system.provisioning.powermanagement.power_off',
        'api.system.provisioning.powermanagement.power_on',
        'api.system.provisioning.powermanagement.reboot',
        'api.system.provisioning.powermanagement.set_details',
        'api.system.provisioning.snapshot.add_tag_to_snapshot',
        'api.system.provisioning.snapshot.delete_snapshot',
        'api.system.provisioning.snapshot.delete_snapshots',
        'api.system.provisioning.snapshot.list_snapshot_config_files',
        'api.system.provisioning.snapshot.list_snapshot_packages',
        'api.system.provisioning.snapshot.list_snapshots',
        'api.system.provisioning.snapshot.rollback_to_snapshot',
        'api.system.provisioning.snapshot.rollback_to_tag'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: systemgroup
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.systemgroup.add_or_remove_systems',
        'api.systemgroup.get_details',
        'api.systemgroup.list_active_systems_in_group',
        'api.systemgroup.list_administrators',
        'api.systemgroup.list_all_groups',
        'api.systemgroup.list_assigned_config_channels',
        'api.systemgroup.list_assigned_formuals',
        'api.systemgroup.list_groups_with_no_associated_admins',
        'api.systemgroup.list_inactive_systems_in_group',
        'api.systemgroup.list_systems',
        'api.systemgroup.list_systems_minimal',
        'api.systemgroup.schedule_apply_errata_to_active',
        'api.systemgroup.subscribe_config_channel',
        'api.systemgroup.unsubscribe_config_channel',
        'api.systemgroup.update'
    )
    ON CONFLICT DO NOTHING;
-- Permit to system_group_admin
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ag.label = 'system_group_admin'
    AND ns.namespace IN (
        'api.systemgroup.add_or_remove_admins',
        'api.systemgroup.create',
        'api.systemgroup.delete'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: user
-- Namespace: user.notifications
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.user.add_assigned_system_group',
        'api.user.add_assigned_system_groups',
        'api.user.add_default_system_group',
        'api.user.add_default_system_groups',
        'api.user.add_role',
        'api.user.create',
        'api.user.delete',
        'api.user.disable',
        'api.user.enable',
        'api.user.get_create_default_system_group',
        'api.user.get_details',
        'api.user.list_assignable_roles',
        'api.user.list_assigned_system_groups',
        'api.user.list_default_system_groups',
        'api.user.list_permissions',
        'api.user.list_roles',
        'api.user.list_users',
        'api.user.remove_assigned_system_group',
        'api.user.remove_assigned_system_groups',
        'api.user.remove_default_system_group',
        'api.user.remove_default_system_groups',
        'api.user.remove_role',
        'api.user.set_create_default_system_group',
        'api.user.set_details',
        'api.user.set_errata_notifications',
        'api.user.set_read_only',
        'api.user.use_pam_authentication',
        'api.user.notifications.delete_notifications',
        'api.user.notifications.get_notifications',
        'api.user.notifications.set_all_notifications_read',
        'api.user.notifications.set_notifications_read'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: user.external
-- Permit only following to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.user.external.create_external_group_to_system_group_map',
        'api.user.external.get_external_group_to_system_group_map',
        'api.user.external.delete_external_group_to_system_group_map',
        'api.user.external.set_external_group_system_groups'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: virtualhostmanager
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.virtualhostmanager.create',
        'api.virtualhostmanager.delete',
        'api.virtualhostmanager.get_detail',
        'api.virtualhostmanager.get_module_parameters',
        'api.virtualhostmanager.list_available_virtual_host_gatherer_modules',
        'api.virtualhostmanager.list_virtual_host_managers'
    )
    ON CONFLICT DO NOTHING;
