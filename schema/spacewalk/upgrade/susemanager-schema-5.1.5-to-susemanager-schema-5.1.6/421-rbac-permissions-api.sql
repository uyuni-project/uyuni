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

-- API permissions

-- Namespace: actionchain
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.actionchain\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: activationkey
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.activationkey\.[^.]*$'
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
    WHERE ns.namespace ~ '^api\.ansible\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: audit
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.audit\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: channel
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.channel\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: channel.access
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.channel\.access\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: channel.appstreams
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.channel\.appstreams\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: channel.org
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.channel\.org\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: channel.software
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.channel\.software\.[^.]*$'
    AND ns.namespace NOT IN (
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
        'api.channel.software.regenerate_yum_cache'
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
        'api.channel.software.regenerate_yum_cache'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: configchannel
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.configchannel\.[^.]*$'
    AND ns.namespace NOT IN (
        'api.configchannel.create',
        'api.configchannel.delete_channels'
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
    WHERE ns.namespace ~ '^api\.contentmanagement\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: distchannel
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.distchannel\.[^.]*$'
    AND ns.namespace NOT IN (
        'api.distchannel.list_maps_for_org'
    )
    ON CONFLICT DO NOTHING;

-- Namespace: errata
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.errata\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: formula
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.formula\.[^.]*$'
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
    AND ns.namespace ~ '^api\.image(\.delta|\.profile|\.store)?\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: kickstart
-- Namespace: kickstart.filepreservation
-- Namespace: kickstart.keys
-- Namespace: kickstart.snippet
-- Namespace: kickstart.tree
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.kickstart(\.filepreservation|\.keys|\.snippet|\.tree)?\.[^.]*$'
    AND ns.namespace NOT IN (
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
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.kickstart\.profile(\.keys|\.software|\.system)?\.[^.]*$'
    AND ns.namespace NOT IN (
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
    WHERE ns.namespace ~ '^api\.maintenance\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: org
-- Namespace: org.trusts
-- Permit only following
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
    WHERE ns.namespace ~ '^api\.packages(\.search)?\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: packages.provider
-- Permit to none

-- Namespace: preferences.locale
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.preferences\.locale\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: proxy
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.proxy\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: recurring
-- Namespace: recurring.custom
-- Namespace: recurring.highstate
-- Namespace: recurring.playbook
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.recurring(\.custom|\.highstate|\.playbook)?\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: saltkey
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.saltkey\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: schedule
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.schedule\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: subscriptionmatching.pinnedsubscription
-- Permit to none

-- Namespace: sync.content
-- Namespace: sync.hub
-- Namespace: sync.master
-- Namespace: sync.slave
-- Permit to none

-- Namespace: system
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.system\.[^.]*$'
    AND ns.namespace NOT IN (
        'api.system.get_pillar',
        'api.system.set_pillar',
        'api.system.set_group_membership'
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
    WHERE ag.label = 'image_admin'
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
    WHERE ns.namespace ~
        '^api\.system(\.appstreams|\.config|\.custominfo|\.monitoring|\.scap|\.search)\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: system.provisioning.powermanagement
-- Namespace: system.provisioning.snapshot
-- Permit to all
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.system\.provisioning(\.powermanagement|\.snapshot)\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: systemgroup
-- Permit to all except
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.systemgroup\.[^.]*$'
    AND ns.namespace NOT IN (
        'api.systemgroup.add_or_remove_admins',
        'api.systemgroup.create',
        'api.systemgroup.delete'
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
    WHERE ns.namespace ~ '^api\.user(\.notifications)?\.[^.]*$'
    ON CONFLICT DO NOTHING;

-- Namespace: user.external
-- Permit only following
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
    WHERE ns.namespace ~ '^api\.virtualhostmanager\.[^.]*$'
    ON CONFLICT DO NOTHING;
