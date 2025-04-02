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

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.create_role', 'W', 'Create a new role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.delete_role', 'W', 'Delete a role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.grant_access', 'W', 'Grant access to the given namespace for the specified role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.list_namespaces', 'R', 'List available namespaces.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.list_permissions', 'R', 'List permissions granted by a role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.list_roles', 'R', 'List existing roles.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.access.revoke_access', 'W', 'Revoke access to the given namespace for the specified role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_configuration_deployment', 'W', 'Adds an action to deploy a configuration file to an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_errata_update', 'W', 'Adds Errata update to an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_package_install', 'W', 'Adds package installation action to an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_package_removal', 'W', 'Adds an action to remove installed packages on the system to an Action')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_package_upgrade', 'W', 'Adds an action to upgrade installed packages on the system to an Action')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_package_verify', 'W', 'Adds an action to verify installed packages on the system to an Action')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_script_run', 'W', 'Add an action with label to run a script to an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.add_system_reboot', 'W', 'Add system reboot to an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.create_chain', 'W', 'Create an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.delete_chain', 'W', 'Delete action chain by label.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.list_chain_actions', 'R', 'List all actions in the particular Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.list_chains', 'R', 'List currently available action chains.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.remove_action', 'W', 'Remove an action from an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.rename_chain', 'W', 'Rename an Action Chain.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.actionchain.schedule_chain', 'W', 'Schedule the Action Chain so that its actions will actually occur.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_app_streams', 'W', 'Add app streams to an activation key. If any of the provided app streams is not available in the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_child_channels', 'W', 'Add child channels to an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_config_channels', 'W', 'Given a list of activation keys and configuration channels,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_entitlements', 'W', 'Add add-on System Types to an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_packages', 'W', 'Add packages to an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.add_server_groups', 'W', 'Add server groups to an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.check_config_deployment', 'W', 'Check configuration file deployment status for the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.clone', 'W', 'Clone an existing activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.create', 'W', 'Create a new activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.delete', 'W', 'Delete an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.disable_config_deployment', 'W', 'Disable configuration file deployment for the specified activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.enable_config_deployment', 'W', 'Enable configuration file deployment for the specified activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.get_details', 'R', 'Lookup an activation key''s details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.list_activated_systems', 'R', 'List the systems activated with the key provided.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.list_activation_keys', 'R', 'List activation keys that are visible to the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.list_channels', 'R', 'List the channels for the given activation key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.list_config_channels', 'R', 'List configuration channels')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_app_streams', 'W', 'Remove app streams from an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_child_channels', 'W', 'Remove child channels from an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_config_channels', 'W', 'Remove configuration channels from the given activation keys.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_entitlements', 'W', 'Remove entitlements (by label) from an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_packages', 'W', 'Remove package names from an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.remove_server_groups', 'W', 'Remove server groups from an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.set_config_channels', 'W', 'Replace the existing set of')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.activationkey.set_details', 'W', 'Update the details of an activation key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.configuration.configure', 'W', 'Configure server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.monitoring.disable', 'W', 'Disable monitoring.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.monitoring.enable', 'W', 'Enable monitoring.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.monitoring.get_status', 'R', 'Get the status of each Prometheus exporter.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.payg.create', 'W', 'Create a new ssh connection data to extract data from')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.payg.delete', 'W', 'Returns a list of ssh connection data registered.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.payg.get_details', 'W', 'Returns a list of ssh connection data registered.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.payg.list', 'W', 'Returns a list of ssh connection data registered.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.admin.payg.set_details', 'W', 'Updates the details of a ssh connection data')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.create_ansible_path', 'W', 'Create ansible path')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.discover_playbooks', 'W', 'Discover playbooks under given playbook path with given pathId')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.fetch_playbook_contents', 'W', 'Fetch the playbook content from the control node using a synchronous salt call.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.introspect_inventory', 'W', 'Introspect inventory under given inventory path with given pathId and return it in a structured way')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.list_ansible_paths', 'R', 'List ansible paths for server (control node)')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.lookup_ansible_path_by_id', 'R', 'Lookup ansible path by path id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.remove_ansible_path', 'W', 'Create ansible path')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.schedule_playbook', 'W', 'Schedule a playbook execution')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.ansible.update_ansible_path', 'W', 'Create ansible path')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.access.disable_user_restrictions', 'W', 'Disable user restrictions for the given channel.  If disabled,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.access.enable_user_restrictions', 'W', 'Enable user restrictions for the given channel. If enabled, only')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.access.get_org_sharing', 'R', 'Get organization sharing access control.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.access.set_org_sharing', 'W', 'Set organization sharing access control.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.appstreams.is_modular', 'R', 'Check if channel is modular.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.appstreams.list_modular', 'R', 'List modular channels in users organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.appstreams.list_module_streams', 'R', 'List available module streams for a given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_all_channels', 'R', 'List all software channels that the user''s organization is entitled to.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_manageable_channels', 'R', 'List all software channels that the user is entitled to manage.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_my_channels', 'R', 'List all software channels that belong to the user''s organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_popular_channels', 'R', 'List the most popular software channels.  Channels that have at least')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_retired_channels', 'R', 'List all retired software channels.  These are channels that the user''s')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_shared_channels', 'R', 'List all software channels that may be shared by the user''s')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_software_channels', 'R', 'List all visible software channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.list_vendor_channels', 'R', 'Lists all the vendor software channels that the user''s organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.org.disable_access', 'W', 'Disable access to the channel for the given organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.org.enable_access', 'W', 'Enable access to the channel for the given organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.org.list', 'W', 'List the organizations associated with the given channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.add_packages', 'W', 'Adds a given list of packages to the given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.add_repo_filter', 'W', 'Adds a filter for a given repo.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.align_metadata', 'W', 'Align the metadata of a channel to another channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.apply_channel_state', 'W', 'Refresh pillar data and then schedule channels state on the provided systems')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.associate_repo', 'W', 'Associates a repository with a channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.clear_repo_filters', 'W', 'Removes the filters for a repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.clone', 'W', 'Clone a channel.  If arch_label is omitted, the arch label of the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.create', 'W', 'Creates a software channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.create_repo', 'W', 'Creates a repository')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.delete', 'W', 'Deletes a custom software channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.disassociate_repo', 'W', 'Disassociates a repository from a channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.get_channel_last_build_by_id', 'R', 'Returns the last build date of the repomd.xml file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.get_details', 'R', 'Returns details of the given channel as a map')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.get_repo_details', 'R', 'Returns details of the given repository')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.get_repo_sync_cron_expression', 'R', 'Returns repo synchronization cron expression')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.is_existing', 'R', 'Returns whether is existing')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.is_globally_subscribable', 'R', 'Returns whether the channel is subscribable by any user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.is_user_manageable', 'R', 'Returns whether the channel may be managed by the given user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.is_user_subscribable', 'R', 'Returns whether the channel may be subscribed to by the given user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_all_packages', 'R', 'Lists all packages in the channel, regardless of package version,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_arches', 'R', 'Lists the potential software channel architectures that can be created')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_channel_repos', 'R', 'Lists associated repos with the given channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_children', 'R', 'List the children of a channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_errata', 'R', 'List the errata applicable to a channel after given startDate')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_errata_by_type', 'R', 'List the errata of a specific type that are applicable to a channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_errata_needing_sync', 'R', 'If you have synced a new channel then patches')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_latest_packages', 'R', 'Lists the packages with the latest version (including release and')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_packages_without_channel', 'R', 'Lists all packages that are not associated with a channel.  Typically')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_repo_filters', 'R', 'Lists the filters for a repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_subscribed_systems', 'R', 'Returns list of subscribed systems for the given channel label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_system_channels', 'R', 'Returns a list of channels that a system is subscribed to for the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.list_user_repos', 'R', 'Returns a list of ContentSource (repos) that the user can see')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.merge_errata', 'W', 'Merges all errata from one channel into another')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.merge_packages', 'W', 'Merges all packages from one channel into another')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.regenerate_needed_cache', 'W', 'Completely clear and regenerate the needed Errata and Package')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.regenerate_yum_cache', 'W', 'Regenerate yum cache for the specified channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.remove_errata', 'W', 'Removes a given list of errata from the given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.remove_packages', 'W', 'Removes a given list of packages from the given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.remove_repo', 'W', 'Removes a repository')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.remove_repo_filter', 'W', 'Removes a filter for a given repo.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_contact_details', 'W', 'Set contact/support information for given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_details', 'W', 'Allows to modify channel attributes')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_globally_subscribable', 'W', 'Set globally subscribable attribute for given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_repo_filters', 'W', 'Replaces the existing set of filters for a given repo.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_user_manageable', 'W', 'Set the manageable flag for a given channel and user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_user_subscribable', 'W', 'Set the subscribable flag for a given channel and user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.sync_errata', 'W', 'If you have synced a new channel then patches')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.sync_repo', 'W', 'Trigger immediate repo synchronization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.update_repo', 'W', 'Updates a ContentSource (repo)')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.update_repo_label', 'W', 'Updates repository label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.update_repo_ssl', 'W', 'Updates repository SSL certificates')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.update_repo_url', 'W', 'Updates repository source URL')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.channel_exists', 'W', 'Check for the existence of the config channel provided.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.create', 'W', 'Create a new global config channel. Caller must be at least a')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.create_or_update_path', 'W', 'Create a new file or directory with the given path, or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.create_or_update_symlink', 'W', 'Create a new symbolic link with the given path, or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.delete_channels', 'W', 'Delete a list of global config channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.delete_file_revisions', 'W', 'Delete specified revisions of a given configuration file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.delete_files', 'W', 'Remove file paths from a global channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.deploy_all_systems', 'W', 'Schedule an immediate configuration deployment for all systems')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.get_details', 'R', 'Lookup config channel details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.get_encoded_file_revision', 'R', 'Get revision of the specified configuration file and transmit the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.get_file_revision', 'R', 'Get revision of the specified config file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.get_file_revisions', 'R', 'Get list of revisions for specified config file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.list_assigned_system_groups', 'R', 'Return a list of Groups where a given configuration channel is assigned to')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.list_files', 'R', 'Return a list of files in a channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.list_globals', 'R', 'List all the global config channels accessible to the logged-in user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.list_subscribed_systems', 'R', 'Return a list of systems subscribed to a configuration channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.lookup_channel_info', 'R', 'Lists details on a list of channels given their channel labels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.lookup_file_info', 'R', 'Given a list of paths and a channel, returns details about')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.schedule_file_comparisons', 'W', 'Schedule a comparison of the latest revision of a file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.sync_salt_files_on_disk', 'W', 'Synchronize all files on the disk to the current state of the database.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.update', 'W', 'Update a global config channel. Caller must be at least a')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.configchannel.update_init_sls', 'W', 'Update the init.sls file for the given state channel. User can only update contents, nothing else.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.attach_filter', 'W', 'Attach a Filter to a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.attach_source', 'W', 'Attach a Source to a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.build_project', 'W', 'Build a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.create_app_stream_filters', 'W', 'Create Filters for AppStream Modular Channel and attach them to CLM Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.create_environment', 'W', 'Create a Content Environment and appends it behind given Content Environment')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.create_filter', 'W', 'Create a Content Filter')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.create_project', 'W', 'Create Content Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.detach_filter', 'W', 'Detach a Filter from a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.detach_source', 'W', 'Detach a Source from a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_filter_criteria', 'R', 'List of available filter criteria')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_filters', 'R', 'List all Content Filters visible to given user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_project_environments', 'R', 'List Environments in a Content Project with the respect to their ordering')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_project_filters', 'R', 'List all Filters associated with a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_project_sources', 'R', 'List Content Project Sources')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.list_projects', 'R', 'List Content Projects visible to user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.lookup_environment', 'R', 'Look up Content Environment based on Content Project and Content Environment label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.lookup_filter', 'R', 'Lookup a Content Filter by ID')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.lookup_project', 'R', 'Look up Content Project with given label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.lookup_source', 'R', 'Look up Content Project Source')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.promote_project', 'W', 'Promote an Environment in a Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.remove_environment', 'W', 'Remove a Content Environment')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.remove_filter', 'W', 'Remove a Content Filter')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.remove_project', 'W', 'Remove Content Project')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.update_environment', 'W', 'Update Content Environment with given label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.update_filter', 'W', 'Update a Content Filter')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.update_project', 'W', 'Update Content Project with given label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.add_channel', 'W', 'Add a new channel to the #product() database')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.add_channels', 'W', 'Add a new channel to the #product() database')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.add_credentials', 'W', 'Add organization credentials (mirror credentials) to #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.delete_credentials', 'W', 'Delete organization credentials (mirror credentials) from #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.list_channels', 'R', 'List all accessible channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.list_credentials', 'R', 'List organization credentials (mirror credentials) available in')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.list_products', 'R', 'List all accessible products.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.synchronize_channel_families', 'W', 'Synchronize channel families between the Customer Center')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.synchronize_products', 'W', 'Synchronize SUSE products between the Customer Center')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.synchronize_repositories', 'W', 'Synchronize repositories between the Customer Center')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.content.synchronize_subscriptions', 'W', 'Synchronize subscriptions between the Customer Center')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.keys.create', 'W', 'creates a new key with the given parameters')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.keys.delete', 'W', 'deletes the key identified by the given parameters')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.keys.get_details', 'R', 'returns all the data associated with the given key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.keys.list_all_keys', 'R', 'list all keys for the org associated with the user logged into the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.keys.update', 'W', 'Updates type and content of the key identified by the description')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.custominfo.create_key', 'W', 'Create a new custom key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.custominfo.delete_key', 'W', 'Delete an existing custom key and all systems'' values for the key.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.custominfo.list_all_keys', 'R', 'List the custom information keys defined for the user''s organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.custominfo.update_key', 'W', 'Update description of a custom key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.audit.list_images_by_patch_status', 'R', 'List visible images with their patch status regarding a given CVE')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.audit.list_systems_by_patch_status', 'R', 'List visible systems with their patch status regarding a given CVE')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.delta.create_delta_image', 'W', 'Import an image and schedule an inspect afterwards. The "size" entries in the pillar')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.delta.get_details', 'R', 'Get details of an Image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.delta.list_deltas', 'R', 'List available DeltaImages')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.distchannel.list_default_maps', 'R', 'Lists the default distribution channel maps')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.distchannel.list_maps_for_org', 'R', 'Lists distribution channel maps valid for the user''s organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.distchannel.set_map_for_org', 'W', 'Sets, overrides (/removes if channelLabel empty)')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.add_packages', 'W', 'Add a set of packages to an erratum with the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.applicable_to_channels', 'R', 'Returns a list of channels applicable to the errata with the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.bugzilla_fixes', 'W', 'Get the Bugzilla fixes for an erratum matching the given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.clone', 'W', 'Clone a list of errata into the specified channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.clone_as_original', 'W', 'Clones a list of errata into a specified cloned channel according the original erratas.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.clone_as_original_async', 'W', 'Asynchronously clones a list of errata into a specified cloned channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.clone_async', 'W', 'Asynchronously clone a list of errata into the specified channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.create', 'W', 'Create a custom errata')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.delete', 'W', 'Delete an erratum.  This method will only allow for deletion')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.find_by_cve', 'R', 'Lookup the details for errata associated with the given CVE')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.get_details', 'R', 'Retrieves the details for the erratum matching the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.list_affected_systems', 'R', 'Return the list of systems affected by the errata with the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.list_cves', 'R', 'Returns a list of http://cve.mitre.org/_blankCVEs applicable to the errata')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.list_keywords', 'R', 'Get the keywords associated with an erratum matching the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.list_packages', 'R', 'Returns a list of the packages affected by the errata with the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.publish', 'W', 'Adds an existing errata to a set of channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.publish_as_original', 'W', 'Adds an existing cloned errata to a set of cloned')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.remove_packages', 'W', 'Remove a set of packages from an erratum with the given advisory name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.errata.set_details', 'W', 'Set erratum details. All arguments are optional and will only be modified')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.filepreservation.create', 'W', 'Create a new file preservation list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.filepreservation.delete', 'W', 'Delete a file preservation list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.filepreservation.get_details', 'R', 'Returns all the data associated with the given file preservation list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.filepreservation.list_all_file_preservations', 'R', 'List all file preservation lists for the organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_combined_formula_data_by_server_ids', 'R', 'Return the list of formulas a server and all his groups have.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_combined_formulas_by_server_id', 'R', 'Return the list of formulas a server and all his groups have.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_formulas_by_group_id', 'R', 'Return the list of formulas a server group has.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_formulas_by_server_id', 'R', 'Return the list of formulas directly applied to a server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_group_formula_data', 'R', 'Get the saved data for the specific formula against specific group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.get_system_formula_data', 'R', 'Get the saved data for the specific formula against specific server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.list_formulas', 'R', 'Return the list of formulas currently installed.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.set_formulas_of_group', 'W', 'Set the formulas of a server group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.set_formulas_of_server', 'W', 'Set the formulas of a server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.set_group_formula_data', 'W', 'Set the formula form for the specified group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.formula.set_system_formula_data', 'W', 'Set the formula form for the specified server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.deregister', 'W', 'De-register the server locally identified by the fqdn.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.generate_access_token', 'W', 'Generate a new access token for ISS for accessing this system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.register_peripheral', 'W', 'Registers automatically a remote server with the specified ISS role.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.register_peripheral_with_token', 'W', 'Registers a remote server with the specified ISS role using an existing specified access token.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.replace_tokens', 'W', 'Replace the auth tokens for connections between this hub and the given peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.set_details', 'W', 'Set server details. All arguments are optional and will only be modified')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.store_access_token', 'W', 'Generate a new access token for ISS for accessing this system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.add_image_file', 'W', 'Delete image file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.delete', 'W', 'Delete an image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.delete_image_file', 'W', 'Delete image file')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.get_custom_values', 'R', 'Get the custom data values defined for the image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.get_details', 'R', 'Get details of an image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.get_pillar', 'R', 'Get pillar data of an image. The "size" entries are converted to string.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.get_relevant_errata', 'R', 'Returns a list of all errata that are relevant for the image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.import_container_image', 'W', 'Import an image and schedule an inspect afterwards')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.import_image', 'W', 'Import an image and schedule an inspect afterwards')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.import_o_s_image', 'W', 'Import an image and schedule an inspect afterwards')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.list_images', 'R', 'List available images')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.list_packages', 'R', 'List the installed packages on the given image')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.schedule_image_build', 'W', 'Schedule an image build')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.set_pillar', 'W', 'Set pillar data of an image. The "size" entries should be passed as string.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.create', 'W', 'Create a new image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.delete', 'W', 'Delete an image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.delete_custom_values', 'W', 'Delete the custom values defined for the specified image profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.get_custom_values', 'R', 'Get the custom data values defined for the image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.get_details', 'R', 'Get details of an image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.list_image_profile_types', 'R', 'List available image store types')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.list_image_profiles', 'R', 'List available image profiles')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.set_custom_values', 'W', 'Set custom values for the specified image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.profile.set_details', 'W', 'Set details of an image profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.create', 'W', 'Create a new image store')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.delete', 'W', 'Delete an image store')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.get_details', 'R', 'Get details of an image store')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.list_image_store_types', 'R', 'List available image store types')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.list_image_stores', 'R', 'List available image stores')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.image.store.set_details', 'W', 'Set details of an image store')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.keys.add_activation_key', 'W', 'Add an activation key association to the kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.keys.get_activation_keys', 'R', 'Lookup the activation keys associated with the kickstart')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.keys.remove_activation_key', 'W', 'Remove an activation key association from the kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.clone_profile', 'W', 'Clone a Kickstart Profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.create_profile', 'W', 'Create a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.create_profile_with_custom_url', 'W', 'Create a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.delete_profile', 'W', 'Delete a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.disable_profile', 'W', 'Enable/Disable a Kickstart Profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.find_kickstart_for_ip', 'R', 'Find an associated kickstart for a given ip address.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.import_file', 'W', 'Import a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.import_raw_file', 'W', 'Import a raw kickstart file into #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.is_profile_disabled', 'R', 'Returns whether a kickstart profile is disabled')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.list_all_ip_ranges', 'R', 'List all Ip Ranges and their associated kickstarts available')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.list_autoinstallable_channels', 'R', 'List autoinstallable channels for the logged in user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.list_kickstartable_channels', 'R', 'List kickstartable channels for the logged in user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.list_kickstarts', 'R', 'Provides a list of kickstart profiles visible to the user''s')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.rename_profile', 'W', 'Rename a kickstart profile in #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.create', 'W', 'Create a Kickstart Tree (Distribution) in #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.delete', 'W', 'Delete a Kickstart Tree (Distribution) from #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.delete_tree_and_profiles', 'W', 'Delete a kickstarttree and any profiles associated with')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.get_details', 'R', 'The detailed information about a kickstartable tree given the tree name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.list', 'W', 'List the available kickstartable trees for the given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.list_install_types', 'R', 'List the available kickstartable install types (rhel2,3,4,5 and')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.rename', 'W', 'Rename a Kickstart Tree (Distribution) in #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.tree.update', 'W', 'Edit a Kickstart Tree (Distribution) in #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.assign_schedule_to_systems', 'W', 'Assign schedule with given name to systems with given IDs.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.create_calendar', 'W', 'Create a new maintenance calendar')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.create_calendar_with_url', 'W', 'Create a new maintenance calendar')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.create_schedule', 'W', 'Create a new maintenance Schedule')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.delete_calendar', 'W', 'Remove a maintenance calendar')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.delete_schedule', 'W', 'Remove a maintenance schedule')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.get_calendar_details', 'W', 'Lookup a specific maintenance schedule')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.get_schedule_details', 'W', 'Lookup a specific maintenance schedule')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.list_calendar_labels', 'W', 'List schedule names visible to user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.list_schedule_names', 'W', 'List Schedule Names visible to user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.list_systems_with_schedule', 'W', 'List IDs of systems that have given schedule assigned')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.refresh_calendar', 'W', 'Refresh maintenance calendar data using the configured URL')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.retract_schedule_from_systems', 'W', 'Retract schedule with given name from systems with given IDs')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.update_calendar', 'W', 'Update a maintenance calendar')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.maintenance.update_schedule', 'W', 'Update a maintenance schedule')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.add_to_master', 'W', 'Add a single organizations to the list of those the specified Master has')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.create', 'W', 'Create a new Master, known to this Slave.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.delete', 'W', 'Remove the specified Master')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.get_default_master', 'R', 'Return the current default-Master for this Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.get_master', 'R', 'Find a Master by specifying its ID')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.get_master_by_label', 'R', 'Find a Master by specifying its label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.get_master_orgs', 'R', 'List all organizations the specified Master has exported to this Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.get_masters', 'R', 'Get all the Masters this Slave knows about')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.make_default', 'W', 'Make the specified Master the default for this Slave''s inter-server-sync')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.map_to_local', 'W', 'Add a single organizations to the list of those the specified Master has')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.set_ca_cert', 'W', 'Set the CA-CERT filename for specified Master on this Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.set_master_orgs', 'W', 'Reset all organizations the specified Master has exported to this Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.unset_default_master', 'W', 'Make this slave have no default Master for inter-server-sync')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.master.update', 'W', 'Updates the label of the specified Master')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.create', 'W', 'Create a new organization and associated administrator account.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.create_first', 'W', 'Create first organization and user after initial setup without authentication')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.delete', 'W', 'Delete an organization. The default organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.get_clm_sync_patches_config', 'R', 'Reads the content lifecycle management patch synchronization config option.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.get_details', 'R', 'The detailed information about an organization given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.get_policy_for_scap_file_upload', 'R', 'Get the status of SCAP detailed result file upload settings')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.get_policy_for_scap_result_deletion', 'R', 'Get the status of SCAP result deletion settings for the given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.is_content_staging_enabled', 'R', 'Get the status of content staging settings for the given organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.is_errata_email_notifs_for_org', 'R', 'Returns whether errata e-mail notifications are enabled')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.is_org_config_managed_by_org_admin', 'R', 'Returns whether Organization Administrator is able to manage his')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.list_orgs', 'R', 'Returns the list of organizations.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.list_users', 'R', 'Returns the list of users in a given organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.migrate_systems', 'W', 'Transfer systems from one organization to another.  If executed by')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_clm_sync_patches_config', 'W', 'Sets the content lifecycle management patch synchronization config option.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_content_staging', 'W', 'Set the status of content staging for the given organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_errata_email_notifs_for_org', 'W', 'Dis/enables errata e-mail notifications for the organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_org_config_managed_by_org_admin', 'W', 'Sets whether Organization Administrator can manage his organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_policy_for_scap_file_upload', 'W', 'Set the status of SCAP detailed result file upload settings')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.set_policy_for_scap_result_deletion', 'W', 'Set the status of SCAP result deletion settins for the given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.transfer_systems', 'W', 'Transfer systems from one organization to another.  If executed by')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.update_name', 'W', 'Updates the name of an organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.add_trust', 'W', 'Add an organization to the list of trusted organizations.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.get_details', 'R', 'The trust details about an organization given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.list_channels_consumed', 'R', 'Lists all software channels that the organization given may consume')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.list_channels_provided', 'R', 'Lists all software channels that the organization given is providing to')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.list_orgs', 'R', 'List all organanizations trusted by the user''s organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.list_systems_affected', 'R', 'Get a list of systems within the  trusted organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.list_trusts', 'R', 'Returns the list of trusted organizations.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.org.trusts.remove_trust', 'W', 'Remove an organization to the list of trusted organizations.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.find_by_nvrea', 'R', 'Lookup the details for packages with the given name, version,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.get_details', 'R', 'Retrieve details for the package with the ID.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.get_package', 'R', 'Retrieve the package file associated with a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.get_package_url', 'R', 'Retrieve the url that can be used to download a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_changelog', 'R', 'List the change log for a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_dependencies', 'R', 'List the dependencies for a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_files', 'R', 'List the files associated with a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_providing_channels', 'R', 'List the channels that provide the a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_providing_errata', 'R', 'List the errata providing the a package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.list_source_packages', 'R', 'List all source packages in user''s organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.remove_package', 'W', 'Remove a package from #product().')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.remove_source_package', 'W', 'Remove a source package.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.provider.associate_key', 'W', 'Associate a package security key and with the package provider.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.provider.list', 'W', 'List all Package Providers.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.provider.list_keys', 'R', 'List all security keys associated with a package provider.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.advanced', 'R', 'Advanced method to search lucene indexes with a passed in query written')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.advanced_with_act_key', 'R', 'Advanced method to search lucene indexes with a passed in query written')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.advanced_with_channel', 'R', 'Advanced method to search lucene indexes with a passed in query written')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.name', 'R', 'Search the lucene package indexes for all packages which')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.name_and_description', 'R', 'Search the lucene package indexes for all packages which')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.packages.search.name_and_summary', 'R', 'Search the lucene package indexes for all packages which')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.subscriptionmatching.pinnedsubscription.create', 'W', 'Creates a Pinned Subscription based on given subscription and system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.subscriptionmatching.pinnedsubscription.delete', 'W', 'Deletes Pinned Subscription with given id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.subscriptionmatching.pinnedsubscription.list', 'W', 'Lists all PinnedSubscriptions')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.get_details', 'R', 'Get current power management settings of the given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.get_status', 'R', 'Execute powermanagement actions')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.list_types', 'R', 'Return a list of available power management types')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.power_off', 'W', 'Execute power management action ''powerOff''')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.power_on', 'W', 'Execute power management action ''powerOn''')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.reboot', 'W', 'Execute power management action ''Reboot''')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.powermanagement.set_details', 'W', 'Get current power management settings of the given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.preferences.locale.list_locales', 'R', 'Returns a list of all understood locales. Can be')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.preferences.locale.list_time_zones', 'R', 'Returns a list of all understood timezones. Results can be')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.preferences.locale.set_locale', 'W', 'Set a user''s locale.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.preferences.locale.set_time_zone', 'W', 'Set a user''s timezone.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.add_ip_range', 'W', 'Add an ip range to a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.add_script', 'W', 'Add a pre/post script to a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.compare_activation_keys', 'W', 'Returns a list for each kickstart profile; each list will contain')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.compare_advanced_options', 'W', 'Returns a list for each kickstart profile; each list will contain the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.compare_packages', 'W', 'Returns a list for each kickstart profile; each list will contain')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.download_kickstart', 'W', 'Download the full contents of a kickstart file.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.download_rendered_kickstart', 'W', 'Downloads the Cobbler-rendered Kickstart file.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_advanced_options', 'R', 'Get advanced options for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_available_repositories', 'R', 'Lists available OS repositories to associate with the provided')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_cfg_preservation', 'R', 'Get ks.cfg preservation option for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_child_channels', 'R', 'Get the child channels for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_custom_options', 'R', 'Get custom options for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_kickstart_tree', 'R', 'Get the kickstart tree for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_repositories', 'R', 'Lists all OS repositories associated with provided kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_update_type', 'R', 'Get the update type for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_variables', 'R', 'Returns a list of variables')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.get_virtualization_type', 'R', 'For given kickstart profile label returns label of')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.list_ip_ranges', 'R', 'List all ip ranges for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.list_scripts', 'R', 'List the pre and post scripts for a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.order_scripts', 'W', 'Change the order that kickstart scripts will run for')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.remove_ip_range', 'W', 'Remove an ip range from a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.remove_script', 'W', 'Remove a script from a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_advanced_options', 'W', 'Set advanced options for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_cfg_preservation', 'W', 'Set ks.cfg preservation option for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_child_channels', 'W', 'Set the child channels for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_custom_options', 'W', 'Set custom options for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_kickstart_tree', 'W', 'Set the kickstart tree for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_logging', 'W', 'Set logging options for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_repositories', 'W', 'Associates OS repository to a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_update_type', 'W', 'Set the update typefor a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_variables', 'W', 'Associates list of kickstart variables')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.set_virtualization_type', 'W', 'For given kickstart profile label sets its virtualization type.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.activate_proxy', 'W', 'Activates the proxy identified by the given client')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.container_config', 'W', 'Compute and download the configuration for proxy containers')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.create_monitoring_scout', 'W', 'Create Monitoring Scout for proxy.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.deactivate_proxy', 'W', 'Deactivates the proxy identified by the given client')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.is_proxy', 'R', 'Test, if the system identified by the given client')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.list_available_proxy_channels', 'R', 'List available version of proxy channel for system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.list_proxies', 'R', 'List the proxies within the user''s organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.list_proxy_clients', 'R', 'List the clients directly connected to a given Proxy.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.delete', 'W', 'Delete a recurring action with the given action ID.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.list_by_entity', 'R', 'Return a list of recurring actions for a given entity.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.lookup_by_id', 'R', 'Find a recurring action with the given action ID.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.custom.create', 'W', 'Create a new recurring custom state action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.custom.list_available', 'R', 'List all the custom states available to the user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.custom.update', 'W', 'Update a recurring custom state action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.highstate.create', 'W', 'Create a new recurring highstate action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.highstate.update', 'W', 'Update the properties of a recurring highstate action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.accept', 'W', 'Accept a minion key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.accepted_list', 'R', 'List accepted salt keys')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.delete', 'W', 'Delete a minion key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.denied_list', 'R', 'List of denied salt keys')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.pending_list', 'R', 'List pending salt keys')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.reject', 'W', 'Reject a minion key')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.saltkey.rejected_list', 'R', 'List of rejected salt keys')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.archive_actions', 'W', 'Archive all actions in the given list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.cancel_actions', 'W', 'Cancel all actions in given list. If an invalid action is provided,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.delete_actions', 'W', 'Delete all archived actions in the given list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.fail_system_action', 'W', 'Fail specific event on specified system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_all_actions', 'R', 'Returns a list of all actions.  This includes completed, in progress,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_all_archived_actions', 'R', 'Returns a list of actions that have been archived.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_all_completed_actions', 'R', 'Returns a list of actions that have been completed.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_archived_actions', 'R', 'Returns a list of actions that have been archived.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_completed_actions', 'R', 'Returns a list of actions that have completed successfully.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_completed_systems', 'R', 'Returns a list of systems that have completed a specific action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_failed_actions', 'R', 'Returns a list of actions that have failed.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_failed_systems', 'R', 'Returns a list of systems that have failed a specific action.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_in_progress_actions', 'R', 'Returns a list of actions that are in progress.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.list_in_progress_systems', 'R', 'Returns a list of systems that have a specific action in progress.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.schedule.reschedule_actions', 'W', 'Reschedule all actions in the given list.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.add_channels', 'W', 'Given a list of servers and configuration channels,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.create_or_update_path', 'W', 'Create a new file (text or binary) or directory with the given path, or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.create_or_update_symlink', 'W', 'Create a new symbolic link with the given path, or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.delete_files', 'W', 'Removes file paths from a local or sandbox channel of a server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.deploy_all', 'W', 'Schedules a deploy action for all the configuration files')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.list_channels', 'R', 'List all global(''Normal'', ''State'') configuration channels associated to a')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.list_files', 'R', 'Return the list of files in a given channel.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.lookup_file_info', 'R', 'Given a list of paths and a server, returns details about')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.remove_channels', 'W', 'Remove config channels from the given servers.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.schedule_apply_config_channel', 'W', 'Schedule highstate application for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.config.set_channels', 'W', 'Replace the existing set of config channels on the given servers.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.add_or_remove_admins', 'W', 'Add or remove administrators to/from the given group. #product() and')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.add_or_remove_systems', 'W', 'Add/remove the given servers to a system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.create', 'W', 'Create a new system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.delete', 'W', 'Delete a system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.get_details', 'R', 'Retrieve details of a ServerGroup based on it''s id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_active_systems_in_group', 'R', 'Lists active systems within a server group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_administrators', 'R', 'Returns the list of users who can administer the given group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_all_groups', 'R', 'Retrieve a list of system groups that are accessible by the logged')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_assigned_config_channels', 'R', 'List all Configuration Channels assigned to a system group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_assigned_formuals', 'R', 'List all Configuration Channels assigned to a system group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_groups_with_no_associated_admins', 'R', 'Returns a list of system groups that do not have an administrator.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_inactive_systems_in_group', 'R', 'Lists inactive systems within a server group using a')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_systems', 'R', 'Return a list of systems associated with this system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.list_systems_minimal', 'R', 'Return a list of systems associated with this system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.schedule_apply_errata_to_active', 'W', 'Schedules an action to apply errata updates to active systems')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.subscribe_config_channel', 'W', 'Subscribe given config channels to a system group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.unsubscribe_config_channel', 'W', 'Unsubscribe given config channels to a system group')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.systemgroup.update', 'W', 'Update an existing system group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.create', 'W', 'Create a new Slave, known to this Master.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.delete', 'W', 'Remove the specified Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.get_allowed_orgs', 'R', 'Get all orgs this Master is willing to export to the specified Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.get_slave', 'R', 'Find a Slave by specifying its ID')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.get_slave_by_name', 'R', 'Find a Slave by specifying its Fully-Qualified Domain Name')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.get_slaves', 'R', 'Get all the Slaves this Master knows about')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.set_allowed_orgs', 'W', 'Set the orgs this Master is willing to export to the specified Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.slave.update', 'W', 'Updates attributes of the specified Slave')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.add_tag_to_snapshot', 'W', 'Adds tag to snapshot')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.delete_snapshot', 'W', 'Deletes a snapshot with the given snapshot id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.delete_snapshots', 'W', 'Deletes all snapshots across multiple systems based on the given date')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.list_snapshot_config_files', 'R', 'List the config files associated with a snapshot.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.list_snapshot_packages', 'R', 'List the packages associated with a snapshot.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.list_snapshots', 'R', 'List snapshots for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.rollback_to_snapshot', 'W', 'Rollbacks server to snapshot')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provisioning.snapshot.rollback_to_tag', 'W', 'Rollbacks server to snapshot')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.snippet.create_or_update', 'W', 'Will create a snippet with the given name and contents if it')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.snippet.delete', 'W', 'Delete the specified snippet.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.snippet.list_all', 'R', 'List all cobbler snippets for the logged in user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.snippet.list_custom', 'R', 'List only custom snippets for the logged in user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.snippet.list_default', 'R', 'List only pre-made default snippets for the logged in user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.software.append_to_software_list', 'W', 'Append the list of software packages to a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.software.get_software_details', 'R', 'Gets kickstart profile software details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.software.get_software_list', 'R', 'Get a list of a kickstart profile''s software packages.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.software.set_software_details', 'W', 'Sets kickstart profile software details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.software.set_software_list', 'W', 'Set the list of software packages for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.appstreams.disable', 'W', 'Schedule disabling of module streams. Invalid modules will be filtered out. If all provided')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.appstreams.enable', 'W', 'Schedule enabling of module streams. Invalid modules will be filtered out. If all provided')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.appstreams.list_module_streams', 'R', 'List available module streams for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.add_file_preservations', 'W', 'Adds the given list of file preservations to the specified kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.add_keys', 'W', 'Adds the given list of keys to the specified kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.check_config_management', 'W', 'Check the configuration management status for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.check_remote_commands', 'W', 'Check the remote commands status flag for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.disable_config_management', 'W', 'Disables the configuration management flag in a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.disable_remote_commands', 'W', 'Disables the remote command flag in a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.enable_config_management', 'W', 'Enables the configuration management flag in a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.enable_remote_commands', 'W', 'Enables the remote command flag in a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.get_locale', 'R', 'Retrieves the locale for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.get_partitioning_scheme', 'R', 'Get the partitioning scheme for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.get_registration_type', 'W', 'returns the registration type of a given kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.get_selinux', 'R', 'Retrieves the SELinux enforcing mode property of a kickstart')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.list_file_preservations', 'R', 'Returns the set of all file preservations associated with the given')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.list_keys', 'R', 'Returns the set of all keys associated with the given kickstart')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.remove_file_preservations', 'W', 'Removes the given list of file preservations from the specified')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.remove_keys', 'W', 'Removes the given list of keys from the specified kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.set_locale', 'W', 'Sets the locale for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.set_partitioning_scheme', 'W', 'Set the partitioning scheme for a kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.set_registration_type', 'W', 'Sets the registration type of a given kickstart profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.kickstart.profile.system.set_selinux', 'W', 'Sets the SELinux enforcing mode property of a kickstart profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.add_entitlements', 'W', 'Add entitlements to a server. Entitlements a server already has')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.add_note', 'W', 'Add a new note to the given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.bootstrap', 'W', 'Bootstrap a system for management via either Salt or Salt SSH.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.bootstrap_with_private_ssh_key', 'W', 'Bootstrap a system for management via either Salt or Salt SSH.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.change_proxy', 'W', 'Connect given systems to another proxy.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.compare_package_profile', 'W', 'Compare a system''s packages against a package profile.  In')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.compare_packages', 'W', 'Compares the packages installed on two systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.create_package_profile', 'W', 'Create a new stored Package Profile from a systems')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.create_system_profile', 'W', 'Creates a system record in database for a system that is not registered.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.create_system_record', 'W', 'Creates a cobbler system record with the specified kickstart label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_custom_values', 'W', 'Delete the custom values defined for the custom system information keys')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_guest_profiles', 'W', 'Delete the specified list of guest profiles for a given host')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_note', 'W', 'Deletes the given note from the server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_notes', 'W', 'Deletes all notes from the server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_package_profile', 'W', 'Delete a package profile')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_system', 'W', 'Delete a system given its client certificate.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_systems', 'W', 'Delete systems given a list of system ids asynchronously.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.delete_tag_from_snapshot', 'W', 'Deletes tag from system snapshot')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.download_system_id', 'W', 'Get the system ID file for a given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_co_co_attestation_config', 'R', 'Return the Confidential Compute Attestation configuration for the given system id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_co_co_attestation_result_details', 'R', 'Return a specific results with all details')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_connection_path', 'R', 'Get the list of proxies that the given system connects')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_cpu', 'R', 'Gets the CPU information of a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_custom_values', 'R', 'Get the custom data values defined for the server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_details', 'R', 'Get system details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_devices', 'R', 'Gets a list of devices for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_dmi', 'R', 'Gets the DMI information of a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_entitlements', 'R', 'Gets the entitlements for a given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_event_details', 'R', 'Returns the details of the event associated with the specified server and event.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_event_history', 'R', 'Returns a list history items associated with the system, ordered')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_id', 'R', 'Get system IDs and last check in information for the given system name.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_installed_products', 'R', 'Get a list of installed products for given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_kernel_live_patch', 'R', 'Returns the currently active kernel live patching version relative to')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_latest_co_co_attestation_report', 'R', 'Return the latest report for the given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_memory', 'R', 'Gets the memory information for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_minion_id_map', 'R', 'Return a map from Salt minion IDs to System IDs.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_name', 'R', 'Get system name and last check in information for the given system ID.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_network', 'R', 'Get the addresses and hostname for a given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_network_devices', 'R', 'Returns the network devices for the given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_network_for_systems', 'R', 'Get the addresses and hostname for a given list of systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_osa_ping', 'R', 'get details about a ping sent to a system using OSA')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_pillar', 'R', 'Get pillar data of given category for given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_registration_date', 'R', 'Returns the date the system was registered.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_relevant_errata', 'R', 'Returns a list of all errata that are relevant to the system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_relevant_errata_by_type', 'R', 'Returns a list of all errata of the specified type that are')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_running_kernel', 'R', 'Returns the running kernel of the given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_script_action_details', 'R', 'Returns script details for script run actions')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_script_results', 'W', 'Fetch results from a script execution. Returns an empty array if no')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_subscribed_base_channel', 'R', 'Provides the base channel of a given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_system_currency_multipliers', 'R', 'Get the System Currency score multipliers')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_system_currency_scores', 'R', 'Get the System Currency scores for all servers the user has access to')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_unscheduled_errata', 'R', 'Provides an array of errata that are applicable to a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_uuid', 'R', 'Get the UUID from the given system ID.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_variables', 'R', 'Lists kickstart variables set  in the system record')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.is_nvre_installed', 'R', 'Check if the package with the given NVRE is installed on given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_activation_keys', 'R', 'List the activation keys the system was registered with.  An empty')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_active_systems', 'R', 'Returns a list of active servers visible to the user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_active_systems_details', 'R', 'Given a list of server ids, returns a list of active servers''')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_administrators', 'R', 'Returns a list of users which can administer the system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_all_installable_packages', 'R', 'Get the list of all installable packages for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_co_co_attestation_reports', 'R', 'Return a list of reports with its results for the given filters')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_duplicates_by_hostname', 'R', 'List duplicate systems by Hostname.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_duplicates_by_ip', 'R', 'List duplicate systems by IP Address.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_duplicates_by_mac', 'R', 'List duplicate systems by Mac Address.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_empty_system_profiles', 'R', 'Returns a list of empty system profiles visible to user (created by the createSystemProfile method).')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_extra_packages', 'R', 'List extra packages for a system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_fqdns', 'R', 'Provides a list of FQDNs associated with a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_groups', 'R', 'List the available groups for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_inactive_systems', 'R', 'Lists systems that have been inactive for the default period of')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_installed_packages', 'R', 'List the installed packages for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_latest_available_package', 'R', 'Get the latest available version of a package for each system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_latest_installable_packages', 'R', 'Get the list of latest installable packages for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_latest_upgradable_packages', 'R', 'Get the list of latest upgradable packages for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_migration_targets', 'R', 'List possible migration targets for a system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_newer_installed_packages', 'R', 'Given a package name, version, release, and epoch, returns the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_notes', 'R', 'Provides a list of notes associated with a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_older_installed_packages', 'R', 'Given a package name, version, release, and epoch, returns')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_out_of_date_systems', 'R', 'Returns list of systems needing package updates.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_package_profiles', 'R', 'List the package profiles in this organization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_package_state', 'R', 'List possible migration targets for a system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_packages', 'R', 'List the installed packages for a given system. Usage of listInstalledPackages is preferred,')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_packages_from_channel', 'R', 'Provides a list of packages installed on a system that are also')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_packages_lock_status', 'R', 'List current package locks status.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_physical_systems', 'R', 'Returns a list of all Physical servers visible to the user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_subscribable_base_channels', 'R', 'Returns a list of subscribable base channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_subscribable_child_channels', 'R', 'Returns a list of subscribable child channels.  This only shows channels')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_subscribed_child_channels', 'R', 'Returns a list of subscribed child channels.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_suggested_reboot', 'R', 'List systems that require reboot.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_system_events', 'R', 'List system actions of the specified type that were *scheduled* against the given server after the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_system_groups_for_systems_with_entitlement', 'R', 'Returns the groups information a system is member of, for all the systems visible to the passed user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_systems', 'R', 'Returns a list of all servers visible to the user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_systems_with_entitlement', 'R', 'Lists the systems that have the given entitlement')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_systems_with_extra_packages', 'R', 'List systems with extra packages')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_systems_with_package', 'R', 'Lists the systems that have the given installed package')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_ungrouped_systems', 'R', 'List systems that are not associated with any system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_user_systems', 'R', 'List systems for a given user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_virtual_guests', 'R', 'Lists the virtual guests for a given virtual host')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.list_virtual_hosts', 'R', 'Lists the virtual hosts visible to the user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.obtain_reactivation_key', 'W', 'Obtains a reactivation key for this server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provision_system', 'W', 'Provision a system using the specified kickstart/autoinstallation profile.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.provision_virtual_guest', 'W', 'Provision a guest on the host specified.  Defaults to:')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.refresh_pillar', 'W', 'refresh all the pillar data of a list of systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.register_peripheral_server', 'W', 'Register foreign peripheral server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.remove_entitlements', 'W', 'Remove addon entitlements from a server. Entitlements a server does')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_apply_errata', 'W', 'Schedules an action to apply errata updates to multiple systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_apply_highstate', 'W', 'Schedule highstate application for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_apply_states', 'W', 'Schedule highstate application for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_certificate_update', 'W', 'Schedule update of client certificate')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_change_channels', 'W', 'Schedule an action to change the channels of the given system. Works for both traditional')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_co_co_attestation', 'W', 'Schedule Confidential Compute Attestation Action')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_dist_upgrade', 'W', 'Schedule a dist upgrade for a system. This call takes a list of channel')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_hardware_refresh', 'W', 'Schedule a hardware refresh for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_install', 'W', 'Schedule package installation for several systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_install_by_nevra', 'W', 'Schedule package installation for several systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_lock_change', 'W', 'Schedule package lock for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_refresh', 'W', 'Schedule a package list refresh for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_remove', 'W', 'Schedule package removal for several systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_remove_by_nevra', 'W', 'Schedule package removal for several systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_package_update', 'W', 'Schedule full package update for several systems.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_product_migration', 'W', 'Schedule a Product migration for a system. This call is the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_reboot', 'W', 'Schedule a reboot for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_s_p_migration', 'W', 'Schedule a Product migration for a system. This call is the')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_script_run', 'W', 'Schedule a script to run.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_sync_packages_with_system', 'W', 'Sync packages from a source system to a target.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search_by_name', 'W', 'Returns a list of system IDs whose name matches')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.send_osa_ping', 'W', 'send a ping to a system using OSA')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_base_channel', 'W', 'Assigns the server to a new base channel.  If the user provides an empty')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_child_channels', 'W', 'Subscribe the given server to the child channels provided.  This')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_co_co_attestation_config', 'W', 'Configure Confidential Compute Attestation for the given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_custom_values', 'W', 'Set custom values for the specified server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_details', 'W', 'Set server details. All arguments are optional and will only be modified')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_group_membership', 'W', 'Set a servers membership in a given group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_lock_status', 'W', 'Set server lock status.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_pillar', 'W', 'Set pillar data of a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_primary_fqdn', 'W', 'Sets new primary FQDN')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_primary_interface', 'W', 'Sets new primary network interface')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_profile_name', 'W', 'Set the profile name for the server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.set_variables', 'W', 'Sets a list of kickstart variables in the cobbler system record')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.tag_latest_snapshot', 'W', 'Tags latest system snapshot')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.unentitle', 'W', 'Unentitle the system completely')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.update_package_state', 'W', 'Update the package state of a given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.update_peripheral_server_info', 'W', 'Update foreign peripheral server info.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.upgrade_entitlement', 'W', 'Adds an entitlement to a given server.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.who_registered', 'W', 'Returns information about the user who registered the system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.monitoring.list_endpoints', 'R', 'Get the list of monitoring endpoint details.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.delete_xccdf_scan', 'W', 'Delete OpenSCAP XCCDF Scan from the #product() database. Note that')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.get_xccdf_scan_details', 'R', 'Get details of given OpenSCAP XCCDF scan.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.get_xccdf_scan_rule_results', 'R', 'Return a full list of RuleResults for given OpenSCAP XCCDF scan.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.list_xccdf_scans', 'R', 'Return a list of finished OpenSCAP scans for a given system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.schedule_xccdf_scan', 'W', 'Schedule OpenSCAP scan.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.device_description', 'R', 'List the systems which match the device description.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.device_driver', 'R', 'List the systems which match this device driver.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.device_id', 'R', 'List the systems which match this device id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.device_vendor_id', 'R', 'List the systems which match this device vendor_id')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.hostname', 'R', 'List the systems which match this hostname')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.ip', 'R', 'List the systems which match this ip.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.name_and_description', 'R', 'List the systems which match this name or description')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.search.uuid', 'R', 'List the systems which match this UUID')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.create_external_group_to_role_map', 'W', 'Externally authenticated users may be members of external groups. You')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.create_external_group_to_system_group_map', 'W', 'Externally authenticated users may be members of external groups. You')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.delete_external_group_to_role_map', 'W', 'Delete the role map for an external group. Can only be called')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.delete_external_group_to_system_group_map', 'W', 'Delete the server group map for an external group. Can only be called')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.get_default_org', 'R', 'Get the default org that users should be added in if orgunit from')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.get_external_group_to_role_map', 'R', 'Get a representation of the role mapping for an external group.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.get_external_group_to_system_group_map', 'R', 'Get a representation of the server group mapping for an external')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.get_keep_temporary_roles', 'R', 'Get whether we should keeps roles assigned to users because of')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.get_use_org_unit', 'R', 'Get whether we place users into the organization that corresponds')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.list_external_group_to_role_maps', 'R', 'List role mappings for all known external groups. Can only be called')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.list_external_group_to_system_group_maps', 'R', 'List server group mappings for all known external groups. Can only be')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.set_default_org', 'W', 'Set the default org that users should be added in if orgunit from')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.set_external_group_roles', 'W', 'Update the roles for an external group. Replace previously set roles')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.set_external_group_system_groups', 'W', 'Update the server groups for an external group. Replace previously set')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.set_keep_temporary_roles', 'W', 'Set whether we should keeps roles assigned to users because of')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.external.set_use_org_unit', 'W', 'Set whether we place users into the organization that corresponds')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.add_assigned_system_group', 'W', 'Add system group to user''s list of assigned system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.add_assigned_system_groups', 'W', 'Add system groups to user''s list of assigned system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.add_default_system_group', 'W', 'Add system group to user''s list of default system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.add_default_system_groups', 'W', 'Add system groups to user''s list of default system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.add_role', 'W', 'Adds a role to a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.create', 'W', 'Create a new user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.delete', 'W', 'Delete a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.disable', 'W', 'Disable a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.enable', 'W', 'Enable a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.get_create_default_system_group', 'R', 'Returns the current value of the CreateDefaultSystemGroup setting.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.get_details', 'R', 'Returns the details about a given user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_assignable_roles', 'R', 'Returns a list of user roles that this user can assign to others.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_assigned_system_groups', 'R', 'Returns the system groups that a user can administer.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_default_system_groups', 'R', 'Returns a user''s list of default system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_permissions', 'R', 'Lists the effective RBAC permissions of a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_roles', 'R', 'Returns a list of the user''s roles.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.list_users', 'R', 'Returns a list of users in your organization.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.remove_assigned_system_group', 'W', 'Remove system group from the user''s list of assigned system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.remove_assigned_system_groups', 'W', 'Remove system groups from a user''s list of assigned system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.remove_default_system_group', 'W', 'Remove a system group from user''s list of default system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.remove_default_system_groups', 'W', 'Remove system groups from a user''s list of default system groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.remove_role', 'W', 'Remove a role from a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.set_create_default_system_group', 'W', 'Sets the value of the createDefaultSystemGroup setting.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.set_details', 'W', 'Updates the details of a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.set_errata_notifications', 'W', 'Enables/disables errata mail notifications for a specific user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.set_read_only', 'W', 'Sets whether the target user should have only read-only API access or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.use_pam_authentication', 'W', 'Toggles whether or not a user uses PAM authentication or')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.notifications.delete_notifications', 'W', 'Deletes multiple notifications')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.notifications.get_notifications', 'R', 'Get all notifications from a user.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.notifications.set_all_notifications_read', 'W', 'Set all notifications from a user as read')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.user.notifications.set_notifications_read', 'W', 'Set notifications of the given user as read')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.create', 'W', 'Creates a Virtual Host Manager from given arguments')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.delete', 'W', 'Deletes a Virtual Host Manager with a given label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.get_detail', 'R', 'Gets details of a Virtual Host Manager with a given label')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.get_module_parameters', 'R', 'Get a list of parameters for a virtual-host-gatherer module.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.list_available_virtual_host_gatherer_modules', 'R', 'List all available modules from virtual-host-gatherer')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.virtualhostmanager.list_virtual_host_managers', 'R', 'Lists Virtual Host Managers visible to a user')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.createRole', '/manager/api/access/createRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.deleteRole', '/manager/api/access/deleteRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.grantAccess', '/manager/api/access/grantAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listNamespaces', '/manager/api/access/listNamespaces', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listPermissions', '/manager/api/access/listPermissions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.listRoles', '/manager/api/access/listRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.access.AccessHandler.revokeAccess', '/manager/api/access/revokeAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addConfigurationDeployment', '/manager/api/actionchain/addConfigurationDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addErrataUpdate', '/manager/api/actionchain/addErrataUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageInstall', '/manager/api/actionchain/addPackageInstall', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageRemoval', '/manager/api/actionchain/addPackageRemoval', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageUpgrade', '/manager/api/actionchain/addPackageUpgrade', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addPackageVerify', '/manager/api/actionchain/addPackageVerify', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addScriptRun', '/manager/api/actionchain/addScriptRun', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.addSystemReboot', '/manager/api/actionchain/addSystemReboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.createChain', '/manager/api/actionchain/createChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.deleteChain', '/manager/api/actionchain/deleteChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.listChainActions', '/manager/api/actionchain/listChainActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.listChains', '/manager/api/actionchain/listChains', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.removeAction', '/manager/api/actionchain/removeAction', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.renameChain', '/manager/api/actionchain/renameChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler.scheduleChain', '/manager/api/actionchain/scheduleChain', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addAppStreams', '/manager/api/activationkey/addAppStreams', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addChildChannels', '/manager/api/activationkey/addChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addConfigChannels', '/manager/api/activationkey/addConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addEntitlements', '/manager/api/activationkey/addEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addPackages', '/manager/api/activationkey/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.addServerGroups', '/manager/api/activationkey/addServerGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.checkConfigDeployment', '/manager/api/activationkey/checkConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.clone', '/manager/api/activationkey/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.create', '/manager/api/activationkey/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.delete', '/manager/api/activationkey/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.disableConfigDeployment', '/manager/api/activationkey/disableConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.enableConfigDeployment', '/manager/api/activationkey/enableConfigDeployment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.getDetails', '/manager/api/activationkey/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listActivatedSystems', '/manager/api/activationkey/listActivatedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listActivationKeys', '/manager/api/activationkey/listActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listChannels', '/manager/api/activationkey/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.listConfigChannels', '/manager/api/activationkey/listConfigChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeAppStreams', '/manager/api/activationkey/removeAppStreams', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeChildChannels', '/manager/api/activationkey/removeChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeConfigChannels', '/manager/api/activationkey/removeConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeEntitlements', '/manager/api/activationkey/removeEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removePackages', '/manager/api/activationkey/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.removeServerGroups', '/manager/api/activationkey/removeServerGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.setConfigChannels', '/manager/api/activationkey/setConfigChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler.setDetails', '/manager/api/activationkey/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler.configure', '/manager/api/admin/configuration/configure', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.disable', '/manager/api/admin/monitoring/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.enable', '/manager/api/admin/monitoring/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler.getStatus', '/manager/api/admin/monitoring/getStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.create', '/manager/api/admin/payg/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.delete', '/manager/api/admin/payg/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.getDetails', '/manager/api/admin/payg/getDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.list', '/manager/api/admin/payg/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.admin.AdminPaygHandler.setDetails', '/manager/api/admin/payg/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.createAnsiblePath', '/manager/api/ansible/createAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.discoverPlaybooks', '/manager/api/ansible/discoverPlaybooks', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.fetchPlaybookContents', '/manager/api/ansible/fetchPlaybookContents', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.introspectInventory', '/manager/api/ansible/introspectInventory', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.listAnsiblePaths', '/manager/api/ansible/listAnsiblePaths', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.lookupAnsiblePathById', '/manager/api/ansible/lookupAnsiblePathById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.removeAnsiblePath', '/manager/api/ansible/removeAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.schedulePlaybook', '/manager/api/ansible/schedulePlaybook', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler.updateAnsiblePath', '/manager/api/ansible/updateAnsiblePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.disableUserRestrictions', '/manager/api/channel/access/disableUserRestrictions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.enableUserRestrictions', '/manager/api/channel/access/enableUserRestrictions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.getOrgSharing', '/manager/api/channel/access/getOrgSharing', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler.setOrgSharing', '/manager/api/channel/access/setOrgSharing', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.isModular', '/manager/api/channel/appstreams/isModular', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.listModular', '/manager/api/channel/appstreams/listModular', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler.listModuleStreams', '/manager/api/channel/appstreams/listModuleStreams', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listAllChannels', '/manager/api/channel/listAllChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listManageableChannels', '/manager/api/channel/listManageableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listMyChannels', '/manager/api/channel/listMyChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listPopularChannels', '/manager/api/channel/listPopularChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listRetiredChannels', '/manager/api/channel/listRetiredChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listSharedChannels', '/manager/api/channel/listSharedChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listSoftwareChannels', '/manager/api/channel/listSoftwareChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler.listVendorChannels', '/manager/api/channel/listVendorChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.disableAccess', '/manager/api/channel/org/disableAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.enableAccess', '/manager/api/channel/org/enableAccess', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler.list', '/manager/api/channel/org/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addPackages', '/manager/api/channel/software/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.addRepoFilter', '/manager/api/channel/software/addRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.alignMetadata', '/manager/api/channel/software/alignMetadata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.applyChannelState', '/manager/api/channel/software/applyChannelState', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.associateRepo', '/manager/api/channel/software/associateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clearRepoFilters', '/manager/api/channel/software/clearRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.clone', '/manager/api/channel/software/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.create', '/manager/api/channel/software/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.createRepo', '/manager/api/channel/software/createRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.delete', '/manager/api/channel/software/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.disassociateRepo', '/manager/api/channel/software/disassociateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getChannelLastBuildById', '/manager/api/channel/software/getChannelLastBuildById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getDetails', '/manager/api/channel/software/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getRepoDetails', '/manager/api/channel/software/getRepoDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.getRepoSyncCronExpression', '/manager/api/channel/software/getRepoSyncCronExpression', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isExisting', '/manager/api/channel/software/isExisting', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isGloballySubscribable', '/manager/api/channel/software/isGloballySubscribable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isUserManageable', '/manager/api/channel/software/isUserManageable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.isUserSubscribable', '/manager/api/channel/software/isUserSubscribable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listAllPackages', '/manager/api/channel/software/listAllPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listArches', '/manager/api/channel/software/listArches', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listChannelRepos', '/manager/api/channel/software/listChannelRepos', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listChildren', '/manager/api/channel/software/listChildren', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrata', '/manager/api/channel/software/listErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrataByType', '/manager/api/channel/software/listErrataByType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listErrataNeedingSync', '/manager/api/channel/software/listErrataNeedingSync', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listLatestPackages', '/manager/api/channel/software/listLatestPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listPackagesWithoutChannel', '/manager/api/channel/software/listPackagesWithoutChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listRepoFilters', '/manager/api/channel/software/listRepoFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listSubscribedSystems', '/manager/api/channel/software/listSubscribedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listSystemChannels', '/manager/api/channel/software/listSystemChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.listUserRepos', '/manager/api/channel/software/listUserRepos', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.mergeErrata', '/manager/api/channel/software/mergeErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.mergePackages', '/manager/api/channel/software/mergePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.regenerateNeededCache', '/manager/api/channel/software/regenerateNeededCache', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.regenerateYumCache', '/manager/api/channel/software/regenerateYumCache', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeErrata', '/manager/api/channel/software/removeErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removePackages', '/manager/api/channel/software/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeRepo', '/manager/api/channel/software/removeRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.removeRepoFilter', '/manager/api/channel/software/removeRepoFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setContactDetails', '/manager/api/channel/software/setContactDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setDetails', '/manager/api/channel/software/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setGloballySubscribable', '/manager/api/channel/software/setGloballySubscribable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setRepoFilters', '/manager/api/channel/software/setRepoFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setUserManageable', '/manager/api/channel/software/setUserManageable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.setUserSubscribable', '/manager/api/channel/software/setUserSubscribable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.syncErrata', '/manager/api/channel/software/syncErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.syncRepo', '/manager/api/channel/software/syncRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepo', '/manager/api/channel/software/updateRepo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoLabel', '/manager/api/channel/software/updateRepoLabel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoSsl', '/manager/api/channel/software/updateRepoSsl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler.updateRepoUrl', '/manager/api/channel/software/updateRepoUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.channelExists', '/manager/api/configchannel/channelExists', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.create', '/manager/api/configchannel/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.createOrUpdatePath', '/manager/api/configchannel/createOrUpdatePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.createOrUpdateSymlink', '/manager/api/configchannel/createOrUpdateSymlink', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteChannels', '/manager/api/configchannel/deleteChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteFileRevisions', '/manager/api/configchannel/deleteFileRevisions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deleteFiles', '/manager/api/configchannel/deleteFiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.deployAllSystems', '/manager/api/configchannel/deployAllSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getDetails', '/manager/api/configchannel/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getEncodedFileRevision', '/manager/api/configchannel/getEncodedFileRevision', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getFileRevision', '/manager/api/configchannel/getFileRevision', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.getFileRevisions', '/manager/api/configchannel/getFileRevisions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listAssignedSystemGroups', '/manager/api/configchannel/listAssignedSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listFiles', '/manager/api/configchannel/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listGlobals', '/manager/api/configchannel/listGlobals', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.listSubscribedSystems', '/manager/api/configchannel/listSubscribedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.lookupChannelInfo', '/manager/api/configchannel/lookupChannelInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.lookupFileInfo', '/manager/api/configchannel/lookupFileInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.scheduleFileComparisons', '/manager/api/configchannel/scheduleFileComparisons', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.syncSaltFilesOnDisk', '/manager/api/configchannel/syncSaltFilesOnDisk', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.update', '/manager/api/configchannel/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler.updateInitSls', '/manager/api/configchannel/updateInitSls', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.attachFilter', '/manager/api/contentmanagement/attachFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.attachSource', '/manager/api/contentmanagement/attachSource', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.buildProject', '/manager/api/contentmanagement/buildProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createAppStreamFilters', '/manager/api/contentmanagement/createAppStreamFilters', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createEnvironment', '/manager/api/contentmanagement/createEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createFilter', '/manager/api/contentmanagement/createFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.createProject', '/manager/api/contentmanagement/createProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.detachFilter', '/manager/api/contentmanagement/detachFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.detachSource', '/manager/api/contentmanagement/detachSource', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listFilterCriteria', '/manager/api/contentmanagement/listFilterCriteria', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listFilters', '/manager/api/contentmanagement/listFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectEnvironments', '/manager/api/contentmanagement/listProjectEnvironments', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectFilters', '/manager/api/contentmanagement/listProjectFilters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjectSources', '/manager/api/contentmanagement/listProjectSources', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.listProjects', '/manager/api/contentmanagement/listProjects', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupEnvironment', '/manager/api/contentmanagement/lookupEnvironment', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupFilter', '/manager/api/contentmanagement/lookupFilter', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupProject', '/manager/api/contentmanagement/lookupProject', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.lookupSource', '/manager/api/contentmanagement/lookupSource', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.promoteProject', '/manager/api/contentmanagement/promoteProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeEnvironment', '/manager/api/contentmanagement/removeEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeFilter', '/manager/api/contentmanagement/removeFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.removeProject', '/manager/api/contentmanagement/removeProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateEnvironment', '/manager/api/contentmanagement/updateEnvironment', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateFilter', '/manager/api/contentmanagement/updateFilter', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler.updateProject', '/manager/api/contentmanagement/updateProject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addChannel', '/manager/api/sync/content/addChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addChannels', '/manager/api/sync/content/addChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.addCredentials', '/manager/api/sync/content/addCredentials', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.deleteCredentials', '/manager/api/sync/content/deleteCredentials', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listChannels', '/manager/api/sync/content/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listCredentials', '/manager/api/sync/content/listCredentials', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.listProducts', '/manager/api/sync/content/listProducts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeChannelFamilies', '/manager/api/sync/content/synchronizeChannelFamilies', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeProducts', '/manager/api/sync/content/synchronizeProducts', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeRepositories', '/manager/api/sync/content/synchronizeRepositories', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler.synchronizeSubscriptions', '/manager/api/sync/content/synchronizeSubscriptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.create', '/manager/api/kickstart/keys/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.delete', '/manager/api/kickstart/keys/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.getDetails', '/manager/api/kickstart/keys/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.listAllKeys', '/manager/api/kickstart/keys/listAllKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler.update', '/manager/api/kickstart/keys/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.createKey', '/manager/api/system/custominfo/createKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.deleteKey', '/manager/api/system/custominfo/deleteKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.listAllKeys', '/manager/api/system/custominfo/listAllKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler.updateKey', '/manager/api/system/custominfo/updateKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listImagesByPatchStatus', '/manager/api/audit/listImagesByPatchStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listSystemsByPatchStatus', '/manager/api/audit/listSystemsByPatchStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.createDeltaImage', '/manager/api/image/delta/createDeltaImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.getDetails', '/manager/api/image/delta/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler.listDeltas', '/manager/api/image/delta/listDeltas', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.listDefaultMaps', '/manager/api/distchannel/listDefaultMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.listMapsForOrg', '/manager/api/distchannel/listMapsForOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler.setMapForOrg', '/manager/api/distchannel/setMapForOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.addPackages', '/manager/api/errata/addPackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.applicableToChannels', '/manager/api/errata/applicableToChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.bugzillaFixes', '/manager/api/errata/bugzillaFixes', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.clone', '/manager/api/errata/clone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsOriginal', '/manager/api/errata/cloneAsOriginal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsOriginalAsync', '/manager/api/errata/cloneAsOriginalAsync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.cloneAsync', '/manager/api/errata/cloneAsync', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.create', '/manager/api/errata/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.delete', '/manager/api/errata/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.findByCve', '/manager/api/errata/findByCve', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.getDetails', '/manager/api/errata/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listAffectedSystems', '/manager/api/errata/listAffectedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listCves', '/manager/api/errata/listCves', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listKeywords', '/manager/api/errata/listKeywords', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.listPackages', '/manager/api/errata/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.publish', '/manager/api/errata/publish', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.publishAsOriginal', '/manager/api/errata/publishAsOriginal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.removePackages', '/manager/api/errata/removePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler.setDetails', '/manager/api/errata/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.create', '/manager/api/kickstart/filepreservation/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.delete', '/manager/api/kickstart/filepreservation/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.getDetails', '/manager/api/kickstart/filepreservation/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler.listAllFilePreservations', '/manager/api/kickstart/filepreservation/listAllFilePreservations', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getCombinedFormulaDataByServerIds', '/manager/api/formula/getCombinedFormulaDataByServerIds', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getCombinedFormulasByServerId', '/manager/api/formula/getCombinedFormulasByServerId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getFormulasByGroupId', '/manager/api/formula/getFormulasByGroupId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getFormulasByServerId', '/manager/api/formula/getFormulasByServerId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getGroupFormulaData', '/manager/api/formula/getGroupFormulaData', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.getSystemFormulaData', '/manager/api/formula/getSystemFormulaData', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.listFormulas', '/manager/api/formula/listFormulas', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setFormulasOfGroup', '/manager/api/formula/setFormulasOfGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setFormulasOfServer', '/manager/api/formula/setFormulasOfServer', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setGroupFormulaData', '/manager/api/formula/setGroupFormulaData', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler.setSystemFormulaData', '/manager/api/formula/setSystemFormulaData', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.deregister', '/manager/api/sync/hub/deregister', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.generateAccessToken', '/manager/api/sync/hub/generateAccessToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.registerPeripheral', '/manager/api/sync/hub/registerPeripheral', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.registerPeripheralWithToken', '/manager/api/sync/hub/registerPeripheralWithToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.replaceTokens', '/manager/api/sync/hub/replaceTokens', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.setDetails', '/manager/api/sync/hub/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.iss.HubHandler.storeAccessToken', '/manager/api/sync/hub/storeAccessToken', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.addImageFile', '/manager/api/image/addImageFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.delete', '/manager/api/image/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.deleteImageFile', '/manager/api/image/deleteImageFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getCustomValues', '/manager/api/image/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getDetails', '/manager/api/image/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getPillar', '/manager/api/image/getPillar', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.getRelevantErrata', '/manager/api/image/getRelevantErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importContainerImage', '/manager/api/image/importContainerImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importImage', '/manager/api/image/importImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.importOSImage', '/manager/api/image/importOSImage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.listImages', '/manager/api/image/listImages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.listPackages', '/manager/api/image/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.scheduleImageBuild', '/manager/api/image/scheduleImageBuild', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler.setPillar', '/manager/api/image/setPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.create', '/manager/api/image/profile/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.delete', '/manager/api/image/profile/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.deleteCustomValues', '/manager/api/image/profile/deleteCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.getCustomValues', '/manager/api/image/profile/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.getDetails', '/manager/api/image/profile/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.listImageProfileTypes', '/manager/api/image/profile/listImageProfileTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.listImageProfiles', '/manager/api/image/profile/listImageProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.setCustomValues', '/manager/api/image/profile/setCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler.setDetails', '/manager/api/image/profile/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.create', '/manager/api/image/store/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.delete', '/manager/api/image/store/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.getDetails', '/manager/api/image/store/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.listImageStoreTypes', '/manager/api/image/store/listImageStoreTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.listImageStores', '/manager/api/image/store/listImageStores', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler.setDetails', '/manager/api/image/store/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.addActivationKey', '/manager/api/kickstart/profile/keys/addActivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.getActivationKeys', '/manager/api/kickstart/profile/keys/getActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler.removeActivationKey', '/manager/api/kickstart/profile/keys/removeActivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.cloneProfile', '/manager/api/kickstart/cloneProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.createProfile', '/manager/api/kickstart/createProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.createProfileWithCustomUrl', '/manager/api/kickstart/createProfileWithCustomUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.deleteProfile', '/manager/api/kickstart/deleteProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.disableProfile', '/manager/api/kickstart/disableProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.findKickstartForIp', '/manager/api/kickstart/findKickstartForIp', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.importFile', '/manager/api/kickstart/importFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.importRawFile', '/manager/api/kickstart/importRawFile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.isProfileDisabled', '/manager/api/kickstart/isProfileDisabled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listAllIpRanges', '/manager/api/kickstart/listAllIpRanges', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listAutoinstallableChannels', '/manager/api/kickstart/listAutoinstallableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listKickstartableChannels', '/manager/api/kickstart/listKickstartableChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.listKickstarts', '/manager/api/kickstart/listKickstarts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler.renameProfile', '/manager/api/kickstart/renameProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.create', '/manager/api/kickstart/tree/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.delete', '/manager/api/kickstart/tree/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.deleteTreeAndProfiles', '/manager/api/kickstart/tree/deleteTreeAndProfiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.getDetails', '/manager/api/kickstart/tree/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.list', '/manager/api/kickstart/tree/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.listInstallTypes', '/manager/api/kickstart/tree/listInstallTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.rename', '/manager/api/kickstart/tree/rename', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler.update', '/manager/api/kickstart/tree/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.assignScheduleToSystems', '/manager/api/maintenance/assignScheduleToSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createCalendar', '/manager/api/maintenance/createCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createCalendarWithUrl', '/manager/api/maintenance/createCalendarWithUrl', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.createSchedule', '/manager/api/maintenance/createSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.deleteCalendar', '/manager/api/maintenance/deleteCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.deleteSchedule', '/manager/api/maintenance/deleteSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.getCalendarDetails', '/manager/api/maintenance/getCalendarDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.getScheduleDetails', '/manager/api/maintenance/getScheduleDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listCalendarLabels', '/manager/api/maintenance/listCalendarLabels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listScheduleNames', '/manager/api/maintenance/listScheduleNames', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.listSystemsWithSchedule', '/manager/api/maintenance/listSystemsWithSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.refreshCalendar', '/manager/api/maintenance/refreshCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.retractScheduleFromSystems', '/manager/api/maintenance/retractScheduleFromSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.updateCalendar', '/manager/api/maintenance/updateCalendar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.xmlrpc.maintenance.MaintenanceHandler.updateSchedule', '/manager/api/maintenance/updateSchedule', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.addToMaster', '/manager/api/sync/master/addToMaster', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.create', '/manager/api/sync/master/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.delete', '/manager/api/sync/master/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getDefaultMaster', '/manager/api/sync/master/getDefaultMaster', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMaster', '/manager/api/sync/master/getMaster', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasterByLabel', '/manager/api/sync/master/getMasterByLabel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasterOrgs', '/manager/api/sync/master/getMasterOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.getMasters', '/manager/api/sync/master/getMasters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.makeDefault', '/manager/api/sync/master/makeDefault', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.mapToLocal', '/manager/api/sync/master/mapToLocal', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.setCaCert', '/manager/api/sync/master/setCaCert', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.setMasterOrgs', '/manager/api/sync/master/setMasterOrgs', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.unsetDefaultMaster', '/manager/api/sync/master/unsetDefaultMaster', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.update', '/manager/api/sync/master/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.create', '/manager/api/org/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.createFirst', '/manager/api/org/createFirst', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.delete', '/manager/api/org/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getClmSyncPatchesConfig', '/manager/api/org/getClmSyncPatchesConfig', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getDetails', '/manager/api/org/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getPolicyForScapFileUpload', '/manager/api/org/getPolicyForScapFileUpload', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.getPolicyForScapResultDeletion', '/manager/api/org/getPolicyForScapResultDeletion', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isContentStagingEnabled', '/manager/api/org/isContentStagingEnabled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isErrataEmailNotifsForOrg', '/manager/api/org/isErrataEmailNotifsForOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.isOrgConfigManagedByOrgAdmin', '/manager/api/org/isOrgConfigManagedByOrgAdmin', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.listOrgs', '/manager/api/org/listOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.listUsers', '/manager/api/org/listUsers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.migrateSystems', '/manager/api/org/migrateSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setClmSyncPatchesConfig', '/manager/api/org/setClmSyncPatchesConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setContentStaging', '/manager/api/org/setContentStaging', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setErrataEmailNotifsForOrg', '/manager/api/org/setErrataEmailNotifsForOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setOrgConfigManagedByOrgAdmin', '/manager/api/org/setOrgConfigManagedByOrgAdmin', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setPolicyForScapFileUpload', '/manager/api/org/setPolicyForScapFileUpload', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.setPolicyForScapResultDeletion', '/manager/api/org/setPolicyForScapResultDeletion', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.transferSystems', '/manager/api/org/transferSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.OrgHandler.updateName', '/manager/api/org/updateName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.addTrust', '/manager/api/org/trusts/addTrust', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.getDetails', '/manager/api/org/trusts/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listChannelsConsumed', '/manager/api/org/trusts/listChannelsConsumed', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listChannelsProvided', '/manager/api/org/trusts/listChannelsProvided', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listOrgs', '/manager/api/org/trusts/listOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listSystemsAffected', '/manager/api/org/trusts/listSystemsAffected', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.listTrusts', '/manager/api/org/trusts/listTrusts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler.removeTrust', '/manager/api/org/trusts/removeTrust', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.findByNvrea', '/manager/api/packages/findByNvrea', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getDetails', '/manager/api/packages/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getPackage', '/manager/api/packages/getPackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.getPackageUrl', '/manager/api/packages/getPackageUrl', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listChangelog', '/manager/api/packages/listChangelog', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listDependencies', '/manager/api/packages/listDependencies', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listFiles', '/manager/api/packages/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listProvidingChannels', '/manager/api/packages/listProvidingChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listProvidingErrata', '/manager/api/packages/listProvidingErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.listSourcePackages', '/manager/api/packages/listSourcePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.removePackage', '/manager/api/packages/removePackage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler.removeSourcePackage', '/manager/api/packages/removeSourcePackage', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.associateKey', '/manager/api/packages/provider/associateKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.list', '/manager/api/packages/provider/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler.listKeys', '/manager/api/packages/provider/listKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advanced', '/manager/api/packages/search/advanced', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advancedWithActKey', '/manager/api/packages/search/advancedWithActKey', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.advancedWithChannel', '/manager/api/packages/search/advancedWithChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.name', '/manager/api/packages/search/name', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.nameAndDescription', '/manager/api/packages/search/nameAndDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler.nameAndSummary', '/manager/api/packages/search/nameAndSummary', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.create', '/manager/api/subscriptionmatching/pinnedsubscription/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.delete', '/manager/api/subscriptionmatching/pinnedsubscription/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler.list', '/manager/api/subscriptionmatching/pinnedsubscription/list', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.getDetails', '/manager/api/system/provisioning/powermanagement/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.getStatus', '/manager/api/system/provisioning/powermanagement/getStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.listTypes', '/manager/api/system/provisioning/powermanagement/listTypes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.powerOff', '/manager/api/system/provisioning/powermanagement/powerOff', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.powerOn', '/manager/api/system/provisioning/powermanagement/powerOn', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.reboot', '/manager/api/system/provisioning/powermanagement/reboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler.setDetails', '/manager/api/system/provisioning/powermanagement/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.listLocales', '/manager/api/preferences/locale/listLocales', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.listTimeZones', '/manager/api/preferences/locale/listTimeZones', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.setLocale', '/manager/api/preferences/locale/setLocale', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler.setTimeZone', '/manager/api/preferences/locale/setTimeZone', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.addIpRange', '/manager/api/kickstart/profile/addIpRange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.addScript', '/manager/api/kickstart/profile/addScript', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.compareActivationKeys', '/manager/api/kickstart/profile/compareActivationKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.compareAdvancedOptions', '/manager/api/kickstart/profile/compareAdvancedOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.comparePackages', '/manager/api/kickstart/profile/comparePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.downloadKickstart', '/manager/api/kickstart/profile/downloadKickstart', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.downloadRenderedKickstart', '/manager/api/kickstart/profile/downloadRenderedKickstart', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getAdvancedOptions', '/manager/api/kickstart/profile/getAdvancedOptions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getAvailableRepositories', '/manager/api/kickstart/profile/getAvailableRepositories', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getCfgPreservation', '/manager/api/kickstart/profile/getCfgPreservation', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getChildChannels', '/manager/api/kickstart/profile/getChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getCustomOptions', '/manager/api/kickstart/profile/getCustomOptions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getKickstartTree', '/manager/api/kickstart/profile/getKickstartTree', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getRepositories', '/manager/api/kickstart/profile/getRepositories', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getUpdateType', '/manager/api/kickstart/profile/getUpdateType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getVariables', '/manager/api/kickstart/profile/getVariables', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.getVirtualizationType', '/manager/api/kickstart/profile/getVirtualizationType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.listIpRanges', '/manager/api/kickstart/profile/listIpRanges', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.listScripts', '/manager/api/kickstart/profile/listScripts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.orderScripts', '/manager/api/kickstart/profile/orderScripts', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.removeIpRange', '/manager/api/kickstart/profile/removeIpRange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.removeScript', '/manager/api/kickstart/profile/removeScript', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setAdvancedOptions', '/manager/api/kickstart/profile/setAdvancedOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setCfgPreservation', '/manager/api/kickstart/profile/setCfgPreservation', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setChildChannels', '/manager/api/kickstart/profile/setChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setCustomOptions', '/manager/api/kickstart/profile/setCustomOptions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setKickstartTree', '/manager/api/kickstart/profile/setKickstartTree', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setLogging', '/manager/api/kickstart/profile/setLogging', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setRepositories', '/manager/api/kickstart/profile/setRepositories', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setUpdateType', '/manager/api/kickstart/profile/setUpdateType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setVariables', '/manager/api/kickstart/profile/setVariables', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler.setVirtualizationType', '/manager/api/kickstart/profile/setVirtualizationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.activateProxy', '/manager/api/proxy/activateProxy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.containerConfig', '/manager/api/proxy/containerConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.createMonitoringScout', '/manager/api/proxy/createMonitoringScout', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.deactivateProxy', '/manager/api/proxy/deactivateProxy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.isProxy', '/manager/api/proxy/isProxy', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listAvailableProxyChannels', '/manager/api/proxy/listAvailableProxyChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listProxies', '/manager/api/proxy/listProxies', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler.listProxyClients', '/manager/api/proxy/listProxyClients', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.delete', '/manager/api/recurring/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.listByEntity', '/manager/api/recurring/listByEntity', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler.lookupById', '/manager/api/recurring/lookupById', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.create', '/manager/api/recurring/custom/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.listAvailable', '/manager/api/recurring/custom/listAvailable', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler.update', '/manager/api/recurring/custom/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler.create', '/manager/api/recurring/highstate/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler.update', '/manager/api/recurring/highstate/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.accept', '/manager/api/saltkey/accept', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.acceptedList', '/manager/api/saltkey/acceptedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.delete', '/manager/api/saltkey/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.deniedList', '/manager/api/saltkey/deniedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.pendingList', '/manager/api/saltkey/pendingList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.reject', '/manager/api/saltkey/reject', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler.rejectedList', '/manager/api/saltkey/rejectedList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.archiveActions', '/manager/api/schedule/archiveActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.cancelActions', '/manager/api/schedule/cancelActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.deleteActions', '/manager/api/schedule/deleteActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.failSystemAction', '/manager/api/schedule/failSystemAction', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllActions', '/manager/api/schedule/listAllActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllArchivedActions', '/manager/api/schedule/listAllArchivedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listAllCompletedActions', '/manager/api/schedule/listAllCompletedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listArchivedActions', '/manager/api/schedule/listArchivedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listCompletedActions', '/manager/api/schedule/listCompletedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listCompletedSystems', '/manager/api/schedule/listCompletedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listFailedActions', '/manager/api/schedule/listFailedActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listFailedSystems', '/manager/api/schedule/listFailedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listInProgressActions', '/manager/api/schedule/listInProgressActions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.listInProgressSystems', '/manager/api/schedule/listInProgressSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler.rescheduleActions', '/manager/api/schedule/rescheduleActions', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.addChannels', '/manager/api/system/config/addChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.createOrUpdatePath', '/manager/api/system/config/createOrUpdatePath', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.createOrUpdateSymlink', '/manager/api/system/config/createOrUpdateSymlink', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.deleteFiles', '/manager/api/system/config/deleteFiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.deployAll', '/manager/api/system/config/deployAll', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.listChannels', '/manager/api/system/config/listChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.listFiles', '/manager/api/system/config/listFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.lookupFileInfo', '/manager/api/system/config/lookupFileInfo', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.removeChannels', '/manager/api/system/config/removeChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.scheduleApplyConfigChannel', '/manager/api/system/config/scheduleApplyConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler.setChannels', '/manager/api/system/config/setChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.addOrRemoveAdmins', '/manager/api/systemgroup/addOrRemoveAdmins', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.addOrRemoveSystems', '/manager/api/systemgroup/addOrRemoveSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.create', '/manager/api/systemgroup/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.delete', '/manager/api/systemgroup/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.getDetails', '/manager/api/systemgroup/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listActiveSystemsInGroup', '/manager/api/systemgroup/listActiveSystemsInGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAdministrators', '/manager/api/systemgroup/listAdministrators', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAllGroups', '/manager/api/systemgroup/listAllGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAssignedConfigChannels', '/manager/api/systemgroup/listAssignedConfigChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listAssignedFormuals', '/manager/api/systemgroup/listAssignedFormuals', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listGroupsWithNoAssociatedAdmins', '/manager/api/systemgroup/listGroupsWithNoAssociatedAdmins', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listInactiveSystemsInGroup', '/manager/api/systemgroup/listInactiveSystemsInGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listSystems', '/manager/api/systemgroup/listSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.listSystemsMinimal', '/manager/api/systemgroup/listSystemsMinimal', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.scheduleApplyErrataToActive', '/manager/api/systemgroup/scheduleApplyErrataToActive', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.subscribeConfigChannel', '/manager/api/systemgroup/subscribeConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.unsubscribeConfigChannel', '/manager/api/systemgroup/unsubscribeConfigChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler.update', '/manager/api/systemgroup/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.create', '/manager/api/sync/slave/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.delete', '/manager/api/sync/slave/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getAllowedOrgs', '/manager/api/sync/slave/getAllowedOrgs', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlave', '/manager/api/sync/slave/getSlave', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlaveByName', '/manager/api/sync/slave/getSlaveByName', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.getSlaves', '/manager/api/sync/slave/getSlaves', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.setAllowedOrgs', '/manager/api/sync/slave/setAllowedOrgs', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler.update', '/manager/api/sync/slave/update', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.addTagToSnapshot', '/manager/api/system/provisioning/snapshot/addTagToSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.deleteSnapshot', '/manager/api/system/provisioning/snapshot/deleteSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.deleteSnapshots', '/manager/api/system/provisioning/snapshot/deleteSnapshots', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshotConfigFiles', '/manager/api/system/provisioning/snapshot/listSnapshotConfigFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshotPackages', '/manager/api/system/provisioning/snapshot/listSnapshotPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.listSnapshots', '/manager/api/system/provisioning/snapshot/listSnapshots', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.rollbackToSnapshot', '/manager/api/system/provisioning/snapshot/rollbackToSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler.rollbackToTag', '/manager/api/system/provisioning/snapshot/rollbackToTag', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.createOrUpdate', '/manager/api/kickstart/snippet/createOrUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.delete', '/manager/api/kickstart/snippet/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listAll', '/manager/api/kickstart/snippet/listAll', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listCustom', '/manager/api/kickstart/snippet/listCustom', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler.listDefault', '/manager/api/kickstart/snippet/listDefault', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.appendToSoftwareList', '/manager/api/kickstart/profile/software/appendToSoftwareList', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.getSoftwareDetails', '/manager/api/kickstart/profile/software/getSoftwareDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.getSoftwareList', '/manager/api/kickstart/profile/software/getSoftwareList', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.setSoftwareDetails', '/manager/api/kickstart/profile/software/setSoftwareDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler.setSoftwareList', '/manager/api/kickstart/profile/software/setSoftwareList', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.disable', '/manager/api/system/appstreams/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.enable', '/manager/api/system/appstreams/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler.listModuleStreams', '/manager/api/system/appstreams/listModuleStreams', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.addFilePreservations', '/manager/api/kickstart/profile/system/addFilePreservations', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.addKeys', '/manager/api/kickstart/profile/system/addKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.checkConfigManagement', '/manager/api/kickstart/profile/system/checkConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.checkRemoteCommands', '/manager/api/kickstart/profile/system/checkRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.disableConfigManagement', '/manager/api/kickstart/profile/system/disableConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.disableRemoteCommands', '/manager/api/kickstart/profile/system/disableRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.enableConfigManagement', '/manager/api/kickstart/profile/system/enableConfigManagement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.enableRemoteCommands', '/manager/api/kickstart/profile/system/enableRemoteCommands', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getLocale', '/manager/api/kickstart/profile/system/getLocale', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getPartitioningScheme', '/manager/api/kickstart/profile/system/getPartitioningScheme', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getRegistrationType', '/manager/api/kickstart/profile/system/getRegistrationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.getSELinux', '/manager/api/kickstart/profile/system/getSELinux', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.listFilePreservations', '/manager/api/kickstart/profile/system/listFilePreservations', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.listKeys', '/manager/api/kickstart/profile/system/listKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.removeFilePreservations', '/manager/api/kickstart/profile/system/removeFilePreservations', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.removeKeys', '/manager/api/kickstart/profile/system/removeKeys', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setLocale', '/manager/api/kickstart/profile/system/setLocale', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setPartitioningScheme', '/manager/api/kickstart/profile/system/setPartitioningScheme', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setRegistrationType', '/manager/api/kickstart/profile/system/setRegistrationType', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler.setSELinux', '/manager/api/kickstart/profile/system/setSELinux', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.addEntitlements', '/manager/api/system/addEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.addNote', '/manager/api/system/addNote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrap', '/manager/api/system/bootstrap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.bootstrapWithPrivateSshKey', '/manager/api/system/bootstrapWithPrivateSshKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.changeProxy', '/manager/api/system/changeProxy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.comparePackageProfile', '/manager/api/system/comparePackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.comparePackages', '/manager/api/system/comparePackages', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createPackageProfile', '/manager/api/system/createPackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createSystemProfile', '/manager/api/system/createSystemProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.createSystemRecord', '/manager/api/system/createSystemRecord', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteCustomValues', '/manager/api/system/deleteCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteGuestProfiles', '/manager/api/system/deleteGuestProfiles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteNote', '/manager/api/system/deleteNote', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteNotes', '/manager/api/system/deleteNotes', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deletePackageProfile', '/manager/api/system/deletePackageProfile', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteSystem', '/manager/api/system/deleteSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteSystems', '/manager/api/system/deleteSystems', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.deleteTagFromSnapshot', '/manager/api/system/deleteTagFromSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.downloadSystemId', '/manager/api/system/downloadSystemId', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCoCoAttestationConfig', '/manager/api/system/getCoCoAttestationConfig', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCoCoAttestationResultDetails', '/manager/api/system/getCoCoAttestationResultDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getConnectionPath', '/manager/api/system/getConnectionPath', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCpu', '/manager/api/system/getCpu', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getCustomValues', '/manager/api/system/getCustomValues', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDetails', '/manager/api/system/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDevices', '/manager/api/system/getDevices', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getDmi', '/manager/api/system/getDmi', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEntitlements', '/manager/api/system/getEntitlements', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEventDetails', '/manager/api/system/getEventDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getEventHistory', '/manager/api/system/getEventHistory', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getId', '/manager/api/system/getId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getInstalledProducts', '/manager/api/system/getInstalledProducts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getKernelLivePatch', '/manager/api/system/getKernelLivePatch', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getLatestCoCoAttestationReport', '/manager/api/system/getLatestCoCoAttestationReport', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getMemory', '/manager/api/system/getMemory', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getMinionIdMap', '/manager/api/system/getMinionIdMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getName', '/manager/api/system/getName', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetwork', '/manager/api/system/getNetwork', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetworkDevices', '/manager/api/system/getNetworkDevices', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getNetworkForSystems', '/manager/api/system/getNetworkForSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getOsaPing', '/manager/api/system/getOsaPing', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getPillar', '/manager/api/system/getPillar', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRegistrationDate', '/manager/api/system/getRegistrationDate', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRelevantErrata', '/manager/api/system/getRelevantErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRelevantErrataByType', '/manager/api/system/getRelevantErrataByType', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getRunningKernel', '/manager/api/system/getRunningKernel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getScriptActionDetails', '/manager/api/system/getScriptActionDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getScriptResults', '/manager/api/system/getScriptResults', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSubscribedBaseChannel', '/manager/api/system/getSubscribedBaseChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSystemCurrencyMultipliers', '/manager/api/system/getSystemCurrencyMultipliers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSystemCurrencyScores', '/manager/api/system/getSystemCurrencyScores', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getUnscheduledErrata', '/manager/api/system/getUnscheduledErrata', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getUuid', '/manager/api/system/getUuid', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getVariables', '/manager/api/system/getVariables', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.isNvreInstalled', '/manager/api/system/isNvreInstalled', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActivationKeys', '/manager/api/system/listActivationKeys', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActiveSystems', '/manager/api/system/listActiveSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listActiveSystemsDetails', '/manager/api/system/listActiveSystemsDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listAdministrators', '/manager/api/system/listAdministrators', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listAllInstallablePackages', '/manager/api/system/listAllInstallablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listCoCoAttestationReports', '/manager/api/system/listCoCoAttestationReports', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByHostname', '/manager/api/system/listDuplicatesByHostname', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByIp', '/manager/api/system/listDuplicatesByIp', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listDuplicatesByMac', '/manager/api/system/listDuplicatesByMac', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listEmptySystemProfiles', '/manager/api/system/listEmptySystemProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listExtraPackages', '/manager/api/system/listExtraPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listFqdns', '/manager/api/system/listFqdns', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listGroups', '/manager/api/system/listGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listInactiveSystems', '/manager/api/system/listInactiveSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listInstalledPackages', '/manager/api/system/listInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestAvailablePackage', '/manager/api/system/listLatestAvailablePackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestInstallablePackages', '/manager/api/system/listLatestInstallablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listLatestUpgradablePackages', '/manager/api/system/listLatestUpgradablePackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listMigrationTargets', '/manager/api/system/listMigrationTargets', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listNewerInstalledPackages', '/manager/api/system/listNewerInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listNotes', '/manager/api/system/listNotes', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listOlderInstalledPackages', '/manager/api/system/listOlderInstalledPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listOutOfDateSystems', '/manager/api/system/listOutOfDateSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackageProfiles', '/manager/api/system/listPackageProfiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackageState', '/manager/api/system/listPackageState', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackages', '/manager/api/system/listPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackagesFromChannel', '/manager/api/system/listPackagesFromChannel', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPackagesLockStatus', '/manager/api/system/listPackagesLockStatus', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listPhysicalSystems', '/manager/api/system/listPhysicalSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribableBaseChannels', '/manager/api/system/listSubscribableBaseChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribableChildChannels', '/manager/api/system/listSubscribableChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSubscribedChildChannels', '/manager/api/system/listSubscribedChildChannels', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSuggestedReboot', '/manager/api/system/listSuggestedReboot', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemEvents', '/manager/api/system/listSystemEvents', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemGroupsForSystemsWithEntitlement', '/manager/api/system/listSystemGroupsForSystemsWithEntitlement', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystems', '/manager/api/system/listSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithEntitlement', '/manager/api/system/listSystemsWithEntitlement', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithExtraPackages', '/manager/api/system/listSystemsWithExtraPackages', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listSystemsWithPackage', '/manager/api/system/listSystemsWithPackage', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listUngroupedSystems', '/manager/api/system/listUngroupedSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listUserSystems', '/manager/api/system/listUserSystems', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listVirtualGuests', '/manager/api/system/listVirtualGuests', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.listVirtualHosts', '/manager/api/system/listVirtualHosts', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.obtainReactivationKey', '/manager/api/system/obtainReactivationKey', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.provisionSystem', '/manager/api/system/provisionSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.provisionVirtualGuest', '/manager/api/system/provisionVirtualGuest', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.refreshPillar', '/manager/api/system/refreshPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.registerPeripheralServer', '/manager/api/system/registerPeripheralServer', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.removeEntitlements', '/manager/api/system/removeEntitlements', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyErrata', '/manager/api/system/scheduleApplyErrata', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyHighstate', '/manager/api/system/scheduleApplyHighstate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleApplyStates', '/manager/api/system/scheduleApplyStates', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleCertificateUpdate', '/manager/api/system/scheduleCertificateUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleChangeChannels', '/manager/api/system/scheduleChangeChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleCoCoAttestation', '/manager/api/system/scheduleCoCoAttestation', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleDistUpgrade', '/manager/api/system/scheduleDistUpgrade', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleHardwareRefresh', '/manager/api/system/scheduleHardwareRefresh', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageInstall', '/manager/api/system/schedulePackageInstall', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageInstallByNevra', '/manager/api/system/schedulePackageInstallByNevra', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageLockChange', '/manager/api/system/schedulePackageLockChange', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRefresh', '/manager/api/system/schedulePackageRefresh', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRemove', '/manager/api/system/schedulePackageRemove', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageRemoveByNevra', '/manager/api/system/schedulePackageRemoveByNevra', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.schedulePackageUpdate', '/manager/api/system/schedulePackageUpdate', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleProductMigration', '/manager/api/system/scheduleProductMigration', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleReboot', '/manager/api/system/scheduleReboot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSPMigration', '/manager/api/system/scheduleSPMigration', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleScriptRun', '/manager/api/system/scheduleScriptRun', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSyncPackagesWithSystem', '/manager/api/system/scheduleSyncPackagesWithSystem', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.searchByName', '/manager/api/system/searchByName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.sendOsaPing', '/manager/api/system/sendOsaPing', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setBaseChannel', '/manager/api/system/setBaseChannel', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setChildChannels', '/manager/api/system/setChildChannels', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setCoCoAttestationConfig', '/manager/api/system/setCoCoAttestationConfig', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setCustomValues', '/manager/api/system/setCustomValues', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setDetails', '/manager/api/system/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setGroupMembership', '/manager/api/system/setGroupMembership', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setLockStatus', '/manager/api/system/setLockStatus', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPillar', '/manager/api/system/setPillar', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPrimaryFqdn', '/manager/api/system/setPrimaryFqdn', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setPrimaryInterface', '/manager/api/system/setPrimaryInterface', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setProfileName', '/manager/api/system/setProfileName', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.setVariables', '/manager/api/system/setVariables', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.tagLatestSnapshot', '/manager/api/system/tagLatestSnapshot', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.unentitle', '/manager/api/system/unentitle', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.updatePackageState', '/manager/api/system/updatePackageState', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.updatePeripheralServerInfo', '/manager/api/system/updatePeripheralServerInfo', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.upgradeEntitlement', '/manager/api/system/upgradeEntitlement', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.whoRegistered', '/manager/api/system/whoRegistered', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.monitoring.SystemMonitoringHandler.listEndpoints', '/manager/api/system/monitoring/listEndpoints', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.deleteXccdfScan', '/manager/api/system/scap/deleteXccdfScan', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.getXccdfScanDetails', '/manager/api/system/scap/getXccdfScanDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.getXccdfScanRuleResults', '/manager/api/system/scap/getXccdfScanRuleResults', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.listXccdfScans', '/manager/api/system/scap/listXccdfScans', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleXccdfScan', '/manager/api/system/scap/scheduleXccdfScan', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceDescription', '/manager/api/system/search/deviceDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceDriver', '/manager/api/system/search/deviceDriver', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceId', '/manager/api/system/search/deviceId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.deviceVendorId', '/manager/api/system/search/deviceVendorId', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.hostname', '/manager/api/system/search/hostname', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.ip', '/manager/api/system/search/ip', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.nameAndDescription', '/manager/api/system/search/nameAndDescription', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler.uuid', '/manager/api/system/search/uuid', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.createExternalGroupToRoleMap', '/manager/api/user/external/createExternalGroupToRoleMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.createExternalGroupToSystemGroupMap', '/manager/api/user/external/createExternalGroupToSystemGroupMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.deleteExternalGroupToRoleMap', '/manager/api/user/external/deleteExternalGroupToRoleMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.deleteExternalGroupToSystemGroupMap', '/manager/api/user/external/deleteExternalGroupToSystemGroupMap', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getDefaultOrg', '/manager/api/user/external/getDefaultOrg', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getExternalGroupToRoleMap', '/manager/api/user/external/getExternalGroupToRoleMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getExternalGroupToSystemGroupMap', '/manager/api/user/external/getExternalGroupToSystemGroupMap', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getKeepTemporaryRoles', '/manager/api/user/external/getKeepTemporaryRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.getUseOrgUnit', '/manager/api/user/external/getUseOrgUnit', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.listExternalGroupToRoleMaps', '/manager/api/user/external/listExternalGroupToRoleMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.listExternalGroupToSystemGroupMaps', '/manager/api/user/external/listExternalGroupToSystemGroupMaps', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setDefaultOrg', '/manager/api/user/external/setDefaultOrg', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setExternalGroupRoles', '/manager/api/user/external/setExternalGroupRoles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setExternalGroupSystemGroups', '/manager/api/user/external/setExternalGroupSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setKeepTemporaryRoles', '/manager/api/user/external/setKeepTemporaryRoles', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler.setUseOrgUnit', '/manager/api/user/external/setUseOrgUnit', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addAssignedSystemGroup', '/manager/api/user/addAssignedSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addAssignedSystemGroups', '/manager/api/user/addAssignedSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addDefaultSystemGroup', '/manager/api/user/addDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addDefaultSystemGroups', '/manager/api/user/addDefaultSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.addRole', '/manager/api/user/addRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.create', '/manager/api/user/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.delete', '/manager/api/user/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.disable', '/manager/api/user/disable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.enable', '/manager/api/user/enable', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.getCreateDefaultSystemGroup', '/manager/api/user/getCreateDefaultSystemGroup', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.getDetails', '/manager/api/user/getDetails', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listAssignableRoles', '/manager/api/user/listAssignableRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listAssignedSystemGroups', '/manager/api/user/listAssignedSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listDefaultSystemGroups', '/manager/api/user/listDefaultSystemGroups', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listPermissions', '/manager/api/user/listPermissions', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listRoles', '/manager/api/user/listRoles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.listUsers', '/manager/api/user/listUsers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeAssignedSystemGroup', '/manager/api/user/removeAssignedSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeAssignedSystemGroups', '/manager/api/user/removeAssignedSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeDefaultSystemGroup', '/manager/api/user/removeDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeDefaultSystemGroups', '/manager/api/user/removeDefaultSystemGroups', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.removeRole', '/manager/api/user/removeRole', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setCreateDefaultSystemGroup', '/manager/api/user/setCreateDefaultSystemGroup', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setDetails', '/manager/api/user/setDetails', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setErrataNotifications', '/manager/api/user/setErrataNotifications', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.setReadOnly', '/manager/api/user/setReadOnly', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserHandler.usePamAuthentication', '/manager/api/user/usePamAuthentication', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.deleteNotifications', '/manager/api/user/notifications/deleteNotifications', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.getNotifications', '/manager/api/user/notifications/getNotifications', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.setAllNotificationsRead', '/manager/api/user/notifications/setAllNotificationsRead', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler.setNotificationsRead', '/manager/api/user/notifications/setNotificationsRead', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.create', '/manager/api/virtualhostmanager/create', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.delete', '/manager/api/virtualhostmanager/delete', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.getDetail', '/manager/api/virtualhostmanager/getDetail', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.getModuleParameters', '/manager/api/virtualhostmanager/getModuleParameters', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.listAvailableVirtualHostGathererModules', '/manager/api/virtualhostmanager/listAvailableVirtualHostGathererModules', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler.listVirtualHostManagers', '/manager/api/virtualhostmanager/listVirtualHostManagers', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.create_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/createRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.delete_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/deleteRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.grant_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/grantAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_namespaces' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listNamespaces' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_permissions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listPermissions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.list_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/access/listRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.access.revoke_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/access/revokeAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_configuration_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addConfigurationDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_errata_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addErrataUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_install' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageInstall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_removal' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageRemoval' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_upgrade' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageUpgrade' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_package_verify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addPackageVerify' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_script_run' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addScriptRun' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.add_system_reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/addSystemReboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.create_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/createChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.delete_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/deleteChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.list_chain_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/actionchain/listChainActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.list_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/actionchain/listChains' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.remove_action' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/removeAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.rename_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/renameChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.actionchain.schedule_chain' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/actionchain/scheduleChain' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_app_streams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addAppStreams' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.add_server_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/addServerGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.check_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/checkConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.disable_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/disableConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.enable_config_deployment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/enableConfigDeployment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_activated_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listActivatedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.list_config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activationkey/listConfigChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_app_streams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeAppStreams' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.remove_server_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/removeServerGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.set_config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/setConfigChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.activationkey.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/activationkey/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.configuration.configure' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/configuration/configure' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/monitoring/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/monitoring/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.monitoring.get_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/monitoring/getStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.get_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/getDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.admin.payg.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/payg/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.create_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/createAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.discover_playbooks' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/discoverPlaybooks' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.fetch_playbook_contents' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/fetchPlaybookContents' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.introspect_inventory' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/introspectInventory' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.list_ansible_paths' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/ansible/listAnsiblePaths' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.lookup_ansible_path_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/ansible/lookupAnsiblePathById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.remove_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/removeAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.schedule_playbook' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/schedulePlaybook' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.ansible.update_ansible_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/ansible/updateAnsiblePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.disable_user_restrictions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/disableUserRestrictions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.enable_user_restrictions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/enableUserRestrictions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.get_org_sharing' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/access/getOrgSharing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.access.set_org_sharing' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/access/setOrgSharing' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.is_modular' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/isModular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.list_modular' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/listModular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.appstreams.list_module_streams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/appstreams/listModuleStreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_all_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listAllChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_manageable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listManageableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_my_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listMyChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_popular_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listPopularChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_retired_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listRetiredChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_shared_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listSharedChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_software_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listSoftwareChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.list_vendor_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/listVendorChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.disable_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/disableAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.enable_access' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/enableAccess' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.org.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/org/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.add_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/addRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.align_metadata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/alignMetadata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.apply_channel_state' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/applyChannelState' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.associate_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/associateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.clear_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/clearRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.create_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/createRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.disassociate_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/disassociateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_channel_last_build_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getChannelLastBuildById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_repo_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getRepoDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.get_repo_sync_cron_expression' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/getRepoSyncCronExpression' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_existing' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isExisting' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_globally_subscribable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isGloballySubscribable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_user_manageable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isUserManageable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.is_user_subscribable' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/isUserSubscribable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_all_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listAllPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_arches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listArches' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_channel_repos' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listChannelRepos' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_children' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listChildren' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata_by_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrataByType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_errata_needing_sync' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listErrataNeedingSync' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_latest_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listLatestPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_packages_without_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listPackagesWithoutChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_repo_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listRepoFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_subscribed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listSubscribedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_system_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listSystemChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.list_user_repos' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/channel/software/listUserRepos' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.merge_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/mergeErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.merge_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/mergePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.regenerate_needed_cache' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/regenerateNeededCache' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.regenerate_yum_cache' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/regenerateYumCache' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.remove_repo_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/removeRepoFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_contact_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setContactDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_globally_subscribable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setGloballySubscribable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_repo_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setRepoFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_user_manageable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setUserManageable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.set_user_subscribable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/setUserSubscribable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.sync_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/syncErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.sync_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/syncRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_label' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoLabel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_ssl' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoSsl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.channel.software.update_repo_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channel/software/updateRepoUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.channel_exists' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/channelExists' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create_or_update_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/createOrUpdatePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.create_or_update_symlink' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/createOrUpdateSymlink' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_file_revisions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteFileRevisions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.delete_files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deleteFiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.deploy_all_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/deployAllSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_encoded_file_revision' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getEncodedFileRevision' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_file_revision' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getFileRevision' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.get_file_revisions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/getFileRevisions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_assigned_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listAssignedSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_globals' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listGlobals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.list_subscribed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/listSubscribedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.lookup_channel_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/lookupChannelInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.lookup_file_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/configchannel/lookupFileInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.schedule_file_comparisons' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/scheduleFileComparisons' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.sync_salt_files_on_disk' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/syncSaltFilesOnDisk' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.configchannel.update_init_sls' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/configchannel/updateInitSls' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.attach_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/attachFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.attach_source' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/attachSource' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.build_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/buildProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_app_stream_filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createAppStreamFilters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.create_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/createProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.detach_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/detachFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.detach_source' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/detachSource' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_filter_criteria' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listFilterCriteria' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_environments' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectEnvironments' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_filters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectFilters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_project_sources' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjectSources' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.list_projects' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/listProjects' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_environment' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupEnvironment' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_filter' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupFilter' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_project' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupProject' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.lookup_source' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/lookupSource' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.promote_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/promoteProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.remove_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/removeProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_environment' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateEnvironment' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_filter' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateFilter' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.contentmanagement.update_project' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/updateProject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.add_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/addCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.delete_credentials' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/deleteCredentials' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_credentials' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listCredentials' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.list_products' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/content/listProducts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_channel_families' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeChannelFamilies' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_products' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeProducts' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_repositories' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeRepositories' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.content.synchronize_subscriptions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/content/synchronizeSubscriptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/keys/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.list_all_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/keys/listAllKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.keys.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/keys/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.create_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/createKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.delete_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/deleteKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.list_all_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/custominfo/listAllKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.custominfo.update_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/custominfo/updateKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_images_by_patch_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listImagesByPatchStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_systems_by_patch_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listSystemsByPatchStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.create_delta_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/delta/createDeltaImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/delta/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delta.list_deltas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/delta/listDeltas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.list_default_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/distchannel/listDefaultMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.list_maps_for_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/distchannel/listMapsForOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.distchannel.set_map_for_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/distchannel/setMapForOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.add_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/addPackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.applicable_to_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/applicableToChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.bugzilla_fixes' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/bugzillaFixes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/clone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_as_original' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsOriginal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_as_original_async' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsOriginalAsync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.clone_async' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/cloneAsync' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.find_by_cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/findByCve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_affected_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listAffectedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_cves' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listCves' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_keywords' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listKeywords' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/errata/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.publish' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/publish' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.publish_as_original' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/publishAsOriginal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.remove_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/removePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.errata.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/errata/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.filepreservation.list_all_file_preservations' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/filepreservation/listAllFilePreservations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_combined_formula_data_by_server_ids' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getCombinedFormulaDataByServerIds' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_combined_formulas_by_server_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getCombinedFormulasByServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_formulas_by_group_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getFormulasByGroupId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_formulas_by_server_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getFormulasByServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_group_formula_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getGroupFormulaData' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.get_system_formula_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/getSystemFormulaData' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.list_formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula/listFormulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_formulas_of_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setFormulasOfGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_formulas_of_server' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setFormulasOfServer' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_group_formula_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setGroupFormulaData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.formula.set_system_formula_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formula/setSystemFormulaData' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.deregister' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/deregister' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.generate_access_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/generateAccessToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.register_peripheral' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/registerPeripheral' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.register_peripheral_with_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/registerPeripheralWithToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.replace_tokens' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/replaceTokens' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.hub.store_access_token' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/hub/storeAccessToken' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.add_image_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/addImageFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.delete_image_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/deleteImageFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_pillar' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getPillar' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.get_relevant_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/getRelevantErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_container_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importContainerImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.import_o_s_image' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/importOSImage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.list_images' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/listImages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.schedule_image_build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/scheduleImageBuild' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.set_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/setPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.delete_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/deleteCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.list_image_profile_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/listImageProfileTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.list_image_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/profile/listImageProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.set_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/setCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.profile.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/profile/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.list_image_store_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/listImageStoreTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.list_image_stores' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/image/store/listImageStores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.image.store.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/image/store/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.add_activation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/addActivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.get_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/getActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.keys.remove_activation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/keys/removeActivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.clone_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/cloneProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.create_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/createProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.create_profile_with_custom_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/createProfileWithCustomUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.delete_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/deleteProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.disable_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/disableProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.find_kickstart_for_ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/findKickstartForIp' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.import_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/importFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.import_raw_file' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/importRawFile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.is_profile_disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/isProfileDisabled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_all_ip_ranges' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listAllIpRanges' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_autoinstallable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listAutoinstallableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_kickstartable_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listKickstartableChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.list_kickstarts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/listKickstarts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.rename_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/renameProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.delete_tree_and_profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/deleteTreeAndProfiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/tree/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.list_install_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/tree/listInstallTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.rename' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/rename' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.tree.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/tree/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.assign_schedule_to_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/assignScheduleToSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_calendar_with_url' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createCalendarWithUrl' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.create_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/createSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.delete_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/deleteCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.delete_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/deleteSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.get_calendar_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/getCalendarDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.get_schedule_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/getScheduleDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_calendar_labels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listCalendarLabels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_schedule_names' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listScheduleNames' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.list_systems_with_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/listSystemsWithSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.refresh_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/refreshCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.retract_schedule_from_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/retractScheduleFromSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.update_calendar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/updateCalendar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.maintenance.update_schedule' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/updateSchedule' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.add_to_master' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/addToMaster' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_default_master' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getDefaultMaster' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMaster' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master_by_label' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasterByLabel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_master_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasterOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.get_masters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/master/getMasters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.make_default' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/makeDefault' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.map_to_local' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/mapToLocal' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.set_ca_cert' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/setCaCert' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.set_master_orgs' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/setMasterOrgs' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.unset_default_master' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/unsetDefaultMaster' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.master.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/master/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.create_first' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/createFirst' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_clm_sync_patches_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getClmSyncPatchesConfig' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_policy_for_scap_file_upload' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getPolicyForScapFileUpload' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.get_policy_for_scap_result_deletion' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/getPolicyForScapResultDeletion' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_content_staging_enabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isContentStagingEnabled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_errata_email_notifs_for_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isErrataEmailNotifsForOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.is_org_config_managed_by_org_admin' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/isOrgConfigManagedByOrgAdmin' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.list_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/listOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.list_users' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/listUsers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.migrate_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/migrateSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_clm_sync_patches_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setClmSyncPatchesConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_content_staging' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setContentStaging' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_errata_email_notifs_for_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setErrataEmailNotifsForOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_org_config_managed_by_org_admin' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setOrgConfigManagedByOrgAdmin' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_policy_for_scap_file_upload' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setPolicyForScapFileUpload' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.set_policy_for_scap_result_deletion' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/setPolicyForScapResultDeletion' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.transfer_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/transferSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.update_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/updateName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.add_trust' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/trusts/addTrust' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_channels_consumed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listChannelsConsumed' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_channels_provided' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listChannelsProvided' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_systems_affected' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listSystemsAffected' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.list_trusts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/org/trusts/listTrusts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.org.trusts.remove_trust' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/org/trusts/removeTrust' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.find_by_nvrea' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/findByNvrea' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getPackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.get_package_url' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/getPackageUrl' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_changelog' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listChangelog' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_dependencies' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listDependencies' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_providing_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listProvidingChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_providing_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listProvidingErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.list_source_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/listSourcePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.remove_package' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/removePackage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.remove_source_package' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/removeSourcePackage' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.associate_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/provider/associateKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/packages/provider/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.provider.list_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/provider/listKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advanced' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced_with_act_key' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advancedWithActKey' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.advanced_with_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/advancedWithChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/name' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name_and_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/nameAndDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.packages.search.name_and_summary' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/packages/search/nameAndSummary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.subscriptionmatching.pinnedsubscription.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/subscriptionmatching/pinnedsubscription/list' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.get_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/getStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.list_types' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/listTypes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.power_off' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/powerOff' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.power_on' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/powerOn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/reboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.powermanagement.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/powermanagement/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.list_locales' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/preferences/locale/listLocales' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.list_time_zones' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/preferences/locale/listTimeZones' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.set_locale' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/preferences/locale/setLocale' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.preferences.locale.set_time_zone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/preferences/locale/setTimeZone' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.add_ip_range' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/addIpRange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.add_script' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/addScript' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_activation_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/compareActivationKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_advanced_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/compareAdvancedOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.compare_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/comparePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.download_kickstart' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/downloadKickstart' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.download_rendered_kickstart' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/downloadRenderedKickstart' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_advanced_options' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getAdvancedOptions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_available_repositories' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getAvailableRepositories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_cfg_preservation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getCfgPreservation' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_custom_options' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getCustomOptions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_kickstart_tree' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getKickstartTree' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_repositories' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getRepositories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_update_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getUpdateType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_variables' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getVariables' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.get_virtualization_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/getVirtualizationType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.list_ip_ranges' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/listIpRanges' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.list_scripts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/listScripts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.order_scripts' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/orderScripts' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.remove_ip_range' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/removeIpRange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.remove_script' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/removeScript' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_advanced_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setAdvancedOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_cfg_preservation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setCfgPreservation' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_custom_options' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setCustomOptions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_kickstart_tree' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setKickstartTree' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_logging' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setLogging' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_repositories' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setRepositories' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_update_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setUpdateType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_variables' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setVariables' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.set_virtualization_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/setVirtualizationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.activate_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/activateProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.container_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/containerConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.create_monitoring_scout' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/createMonitoringScout' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.deactivate_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/deactivateProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.is_proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/isProxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_available_proxy_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listAvailableProxyChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_proxies' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listProxies' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.proxy.list_proxy_clients' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/proxy/listProxyClients' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.list_by_entity' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/listByEntity' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.lookup_by_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/lookupById' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/custom/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.list_available' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurring/custom/listAvailable' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.custom.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/custom/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.highstate.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/highstate/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.recurring.highstate.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurring/highstate/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.accept' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/accept' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.accepted_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/acceptedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.denied_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/deniedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.pending_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/pendingList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.reject' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/saltkey/reject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.saltkey.rejected_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/saltkey/rejectedList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.archive_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/archiveActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.cancel_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/cancelActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.delete_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/deleteActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.fail_system_action' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/failSystemAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_archived_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllArchivedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_all_completed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listAllCompletedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_archived_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listArchivedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_completed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listCompletedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_completed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listCompletedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_failed_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listFailedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_failed_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listFailedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_in_progress_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listInProgressActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.list_in_progress_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/schedule/listInProgressSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.schedule.reschedule_actions' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/schedule/rescheduleActions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.add_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/addChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.create_or_update_path' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/createOrUpdatePath' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.create_or_update_symlink' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/createOrUpdateSymlink' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.delete_files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/deleteFiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.deploy_all' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/deployAll' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.list_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/listChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.list_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/listFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.lookup_file_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/config/lookupFileInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.remove_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/removeChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.schedule_apply_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/scheduleApplyConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.config.set_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/config/setChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.add_or_remove_admins' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/addOrRemoveAdmins' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.add_or_remove_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/addOrRemoveSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_active_systems_in_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listActiveSystemsInGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_administrators' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAdministrators' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_all_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAllGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_assigned_config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAssignedConfigChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_assigned_formuals' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listAssignedFormuals' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_groups_with_no_associated_admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listGroupsWithNoAssociatedAdmins' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_inactive_systems_in_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listInactiveSystemsInGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.list_systems_minimal' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systemgroup/listSystemsMinimal' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.schedule_apply_errata_to_active' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/scheduleApplyErrataToActive' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.subscribe_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/subscribeConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.unsubscribe_config_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/unsubscribeConfigChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.systemgroup.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systemgroup/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_allowed_orgs' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getAllowedOrgs' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slave' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlave' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slave_by_name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlaveByName' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.get_slaves' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/sync/slave/getSlaves' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.set_allowed_orgs' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/setAllowedOrgs' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.sync.slave.update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/sync/slave/update' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.add_tag_to_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/addTagToSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.delete_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/deleteSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.delete_snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/deleteSnapshots' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshot_config_files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshotConfigFiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshot_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshotPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.list_snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/listSnapshots' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.rollback_to_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/rollbackToSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provisioning.snapshot.rollback_to_tag' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisioning/snapshot/rollbackToTag' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.create_or_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/snippet/createOrUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/snippet/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_all' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listAll' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_custom' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listCustom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.snippet.list_default' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/snippet/listDefault' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.append_to_software_list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/appendToSoftwareList' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.get_software_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/getSoftwareDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.get_software_list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/getSoftwareList' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.set_software_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/setSoftwareDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.software.set_software_list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/software/setSoftwareList' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/appstreams/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/appstreams/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.appstreams.list_module_streams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/appstreams/listModuleStreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.add_file_preservations' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/addFilePreservations' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.add_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/addKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.check_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/checkConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.check_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/checkRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.disable_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/disableConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.disable_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/disableRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.enable_config_management' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/enableConfigManagement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.enable_remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/enableRemoteCommands' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_locale' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getLocale' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_partitioning_scheme' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getPartitioningScheme' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_registration_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getRegistrationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.get_selinux' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/getSELinux' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.list_file_preservations' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/listFilePreservations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.list_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/listKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.remove_file_preservations' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/removeFilePreservations' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.remove_keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/removeKeys' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_locale' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setLocale' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_partitioning_scheme' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setPartitioningScheme' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_registration_type' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setRegistrationType' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.kickstart.profile.system.set_selinux' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/kickstart/profile/system/setSELinux' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.add_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/addEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.add_note' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/addNote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.bootstrap_with_private_ssh_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/bootstrapWithPrivateSshKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.change_proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/changeProxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.compare_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/comparePackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.compare_packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/comparePackages' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createPackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_system_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createSystemProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.create_system_record' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/createSystemRecord' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_guest_profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteGuestProfiles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_note' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteNote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_notes' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteNotes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_package_profile' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deletePackageProfile' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteSystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.delete_tag_from_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/deleteTagFromSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.download_system_id' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/downloadSystemId' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_co_co_attestation_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCoCoAttestationConfig' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_co_co_attestation_result_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCoCoAttestationResultDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_connection_path' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getConnectionPath' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_cpu' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCpu' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_custom_values' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getCustomValues' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_devices' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDevices' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_dmi' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getDmi' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_entitlements' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEntitlements' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_event_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEventDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_event_history' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getEventHistory' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_installed_products' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getInstalledProducts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_kernel_live_patch' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getKernelLivePatch' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_latest_co_co_attestation_report' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getLatestCoCoAttestationReport' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_memory' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getMemory' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_minion_id_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getMinionIdMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_name' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getName' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetwork' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network_devices' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetworkDevices' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_network_for_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getNetworkForSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_osa_ping' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getOsaPing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_pillar' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getPillar' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_registration_date' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRegistrationDate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_relevant_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRelevantErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_relevant_errata_by_type' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRelevantErrataByType' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_running_kernel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getRunningKernel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_script_action_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getScriptActionDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_script_results' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/getScriptResults' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_subscribed_base_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSubscribedBaseChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_system_currency_multipliers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSystemCurrencyMultipliers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_system_currency_scores' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSystemCurrencyScores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_unscheduled_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getUnscheduledErrata' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_uuid' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getUuid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_variables' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getVariables' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.is_nvre_installed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/isNvreInstalled' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_activation_keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActivationKeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_active_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActiveSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_active_systems_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listActiveSystemsDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_administrators' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listAdministrators' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_all_installable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listAllInstallablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_co_co_attestation_reports' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listCoCoAttestationReports' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_hostname' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByHostname' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByIp' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_duplicates_by_mac' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listDuplicatesByMac' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_empty_system_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listEmptySystemProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_extra_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listExtraPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_fqdns' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listFqdns' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_inactive_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listInactiveSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_available_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestAvailablePackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_installable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestInstallablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_latest_upgradable_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listLatestUpgradablePackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_migration_targets' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listMigrationTargets' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_newer_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listNewerInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_notes' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listNotes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_older_installed_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listOlderInstalledPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_out_of_date_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listOutOfDateSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_package_profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackageProfiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_package_state' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackageState' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages_from_channel' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackagesFromChannel' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_packages_lock_status' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPackagesLockStatus' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_physical_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listPhysicalSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribable_base_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribableBaseChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribable_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribableChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_subscribed_child_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSubscribedChildChannels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_suggested_reboot' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSuggestedReboot' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_system_events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemEvents' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_system_groups_for_systems_with_entitlement' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemGroupsForSystemsWithEntitlement' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_entitlement' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithEntitlement' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_extra_packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithExtraPackages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_systems_with_package' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listSystemsWithPackage' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_ungrouped_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listUngroupedSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_user_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listUserSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_virtual_guests' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listVirtualGuests' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.list_virtual_hosts' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/listVirtualHosts' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.obtain_reactivation_key' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/obtainReactivationKey' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provision_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisionSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.provision_virtual_guest' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/provisionVirtualGuest' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.refresh_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/refreshPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.register_peripheral_server' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/registerPeripheralServer' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.remove_entitlements' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/removeEntitlements' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_errata' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyErrata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyHighstate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_apply_states' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleApplyStates' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_certificate_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleCertificateUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_change_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleChangeChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_co_co_attestation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleCoCoAttestation' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_dist_upgrade' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleDistUpgrade' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_hardware_refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleHardwareRefresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_install' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageInstall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_install_by_nevra' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageInstallByNevra' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_lock_change' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageLockChange' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRefresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_remove' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRemove' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_remove_by_nevra' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageRemoveByNevra' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_package_update' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/schedulePackageUpdate' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_product_migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleProductMigration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleReboot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_s_p_migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSPMigration' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_script_run' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleScriptRun' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_sync_packages_with_system' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSyncPackagesWithSystem' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search_by_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/searchByName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.send_osa_ping' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/sendOsaPing' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_base_channel' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setBaseChannel' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_child_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setChildChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_co_co_attestation_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setCoCoAttestationConfig' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_custom_values' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setCustomValues' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_group_membership' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setGroupMembership' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_lock_status' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setLockStatus' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_pillar' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPillar' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_primary_fqdn' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPrimaryFqdn' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_primary_interface' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setPrimaryInterface' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_profile_name' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setProfileName' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.set_variables' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/setVariables' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.tag_latest_snapshot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/tagLatestSnapshot' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.unentitle' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/unentitle' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.update_package_state' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/updatePackageState' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.update_peripheral_server_info' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/updatePeripheralServerInfo' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.upgrade_entitlement' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/upgradeEntitlement' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.who_registered' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/whoRegistered' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.monitoring.list_endpoints' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/monitoring/listEndpoints' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.delete_xccdf_scan' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scap/deleteXccdfScan' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.get_xccdf_scan_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/getXccdfScanDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.get_xccdf_scan_rule_results' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/getXccdfScanRuleResults' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.list_xccdf_scans' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/scap/listXccdfScans' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_xccdf_scan' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scap/scheduleXccdfScan' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_driver' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceDriver' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.device_vendor_id' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/deviceVendorId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.hostname' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/hostname' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.ip' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/ip' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.name_and_description' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/nameAndDescription' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.search.uuid' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/search/uuid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.create_external_group_to_role_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/createExternalGroupToRoleMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.create_external_group_to_system_group_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/createExternalGroupToSystemGroupMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.delete_external_group_to_role_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/deleteExternalGroupToRoleMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.delete_external_group_to_system_group_map' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/deleteExternalGroupToSystemGroupMap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_default_org' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getDefaultOrg' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_external_group_to_role_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getExternalGroupToRoleMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_external_group_to_system_group_map' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getExternalGroupToSystemGroupMap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_keep_temporary_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getKeepTemporaryRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.get_use_org_unit' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/getUseOrgUnit' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.list_external_group_to_role_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/listExternalGroupToRoleMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.list_external_group_to_system_group_maps' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/external/listExternalGroupToSystemGroupMaps' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_default_org' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setDefaultOrg' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_external_group_roles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setExternalGroupRoles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_external_group_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setExternalGroupSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_keep_temporary_roles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setKeepTemporaryRoles' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.external.set_use_org_unit' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/external/setUseOrgUnit' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_assigned_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addAssignedSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_assigned_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addAssignedSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_default_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addDefaultSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.add_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/addRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.disable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/disable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.enable' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/enable' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.get_create_default_system_group' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/getCreateDefaultSystemGroup' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.get_details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/getDetails' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_assignable_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listAssignableRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_assigned_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listAssignedSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_default_system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listDefaultSystemGroups' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_permissions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listPermissions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_roles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listRoles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.list_users' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/listUsers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_assigned_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeAssignedSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_assigned_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeAssignedSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_default_system_groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeDefaultSystemGroups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.remove_role' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/removeRole' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_create_default_system_group' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setCreateDefaultSystemGroup' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setDetails' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_errata_notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setErrataNotifications' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.set_read_only' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/setReadOnly' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.use_pam_authentication' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/usePamAuthentication' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.delete_notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/deleteNotifications' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.get_notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/user/notifications/getNotifications' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.set_all_notifications_read' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/setAllNotificationsRead' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.user.notifications.set_notifications_read' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/user/notifications/setNotificationsRead' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.create' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/virtualhostmanager/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/virtualhostmanager/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.get_detail' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/getDetail' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.get_module_parameters' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/getModuleParameters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.list_available_virtual_host_gatherer_modules' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/listAvailableVirtualHostGathererModules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.virtualhostmanager.list_virtual_host_managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/virtualhostmanager/listVirtualHostManagers' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
