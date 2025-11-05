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
    VALUES ('admin.access', 'R', 'List and detail custom access groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('admin.access', 'W', 'Create, modify and delete custom access groups.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('admin.hub', 'R', 'Browse Hub Online Synchronization pages')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('admin.hub', 'W', 'Modify and delete hub and peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('admin.config', 'R', 'View configuration and setup parameters')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('admin.config', 'W', 'Modify configuration and setup parameters')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('audit.cve', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('audit.openscap', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('audit.coco', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.sources', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.filters', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.environments', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.project.build', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.filter.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('clm.filter.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.channels', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.channels', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.files', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.files', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('config.systems', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.main', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.tasks', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.inactive_systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.critical_systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.pending_actions', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.latest_errata', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.system_groups', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.overview.panels.recent_systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.notifications', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.notifications', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.notifications.retry', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.address', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.address', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.email', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.deactivate', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.preferences', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.preferences', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.config', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.config', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.trust', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.recurring', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.recurring', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.config_channels', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('home.account.myorg.config_channels', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.image.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.image.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.image.overview', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.image.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.image.import', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.profile.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.profile.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.profile.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.profile.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.store.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.store.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.store.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.store.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.build', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.runtime', 'R', 'View container runtime status')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('cm.runtime', 'W', 'Modify container runtime definitions')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.details.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.details.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.details.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.details.systems', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.search', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.manage.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.manage.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.manage.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.manage.notify', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.manage.delete', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('patches.clone', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('salt.keys', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('salt.keys', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('salt.formulas', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.pending', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.pending', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.failed', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.failed', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.completed', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.completed', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.archived', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.archived', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.action_chains', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.action_chains', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.recurring', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.maintenance_windows', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.maintenance_windows', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.overview', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.managers', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.managers', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.patches', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.systems', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.details.appstreams', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.search', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.delete', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.managers', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.clone', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.patches', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.patches', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.packages', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.manage.repos', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.distro', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('software.distro', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.ssm', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.patches', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.packages', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.target_systems', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.channels', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.config.files', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.config.channels', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.autoinstallation', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.openscap', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.coco', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.highstate', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.overview', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.hardware', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.refresh', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.support', 'R', 'Views for generating and uploading the support data')
    ON CONFLICT DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.support', 'W', 'Schedule the support data action on the system')
    ON CONFLICT DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.maintenance', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.remote_commands', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.custom_data', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.custom_data', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.reboot', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.connection', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.transfer', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.delete', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.snapshots', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.snapshots', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.details.delete', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.systems', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.patches', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.patches', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.admins', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.admins', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.config', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.highstate', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.highstate', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.config', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.formulas', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.formulas', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.recurring', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.recurring', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.connection', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.proxy', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.proxy', 'W', 'Modify configuration of a proxy system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.peripheral', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.peripheral', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.activation', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.details.hardware', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.patches', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.channels', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.migration', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.appstreams', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.appstreams', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.ptf', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.software.ptf', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.config.overview', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.config.files', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.config.channels', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.join', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.openscap', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.coco', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.highstate', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.packages', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.config', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.states.config', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.formulas', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.formulas', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.ansible', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.ansible', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.recurring', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.recurring', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.events', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.events', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.bootstrap', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.proxy', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.search', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.list', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.list', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.appstreams', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.appstreams', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.packages', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.packages', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.config', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.config', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.groups', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.groups', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.delete', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.profiles', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.profiles', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.custom_data', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.custom_data', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.autoinstallation', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.autoinstallation.provisioning', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.autoinstallation.provisioning', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.vhms', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.vhms', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.appstreams', 'W', 'Manage AppStreams on systems (SSM)')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.list.active', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.list.disabled', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.list.all', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.list.disabled', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.details', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.groups', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.groups', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.channels', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.channels', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.preferences', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.preferences', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.group_config', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.group_config', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
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
    VALUES ('api.actionchain.add_apply_highstate', 'W', 'Adds an action to apply highstate on the system')
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
    VALUES ('api.channel.software.list_vendor_repo_filters', 'W', 'Lists the filters for a vendor repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.clear_vendor_repo_filters', 'W', 'Clears the filters for a repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.set_vendor_repo_filters', 'W', 'Replaces the existing set of filters for a given repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.remove_vendor_repo_filter', 'W', 'Removes a filter from a given vendor repo')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.channel.software.add_vendor_repo_filter', 'W', 'Adds a filter to a given vendor repo')
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
    VALUES ('api.contentmanagement.generate_project_difference', 'W', 'Generate the difference for a CLM project.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.contentmanagement.generate_environment_difference', 'W', 'Generate the difference between CLM environments.')
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
    VALUES ('api.contentmanagement.list_environment_difference', 'R', 'List the difference of a Project Environment compared to its original')
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
    VALUES ('api.sync.hub.get_all_peripheral_channels', 'R', 'List all peripheral channels')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.get_manager_info', 'R', 'Get Manager Server Details')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.is_iss_peripheral', 'R', 'Return if the server is a peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.get_all_peripheral_orgs', 'R', 'List all Organiazations of the peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.migrate_from_iss_v1', 'W', 'Migrate ISSv1 to Hub environment')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.sync_peripheral_channels', 'W', 'Sync channels with the peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.migrate_from_iss_v2', 'W', 'Migrate ISSv2 to Hub environment')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.remove_peripheral_channels_to_sync', 'W', 'Remove channels from synchronization with a peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.add_peripheral_channels_to_sync', 'W', 'Add channels to synchronize with a peripheral server')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.regenerate_scc_credentials', 'W', 'Regenerate SCC credentials for Hub Synchronization')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.sync.hub.list_peripheral_channels_to_sync', 'R', 'List channels which are configured to be synchronized with a peripheral server')
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
    VALUES ('api.proxy.bootstrap_proxy', 'W', 'Deploy a proxy container on given Salt minion')
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
    VALUES ('api.recurring.playbook.create', 'W', 'Create a new recurring playbook action')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.recurring.playbook.update', 'W', 'Update a recurring Ansible playbook action')
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
    VALUES ('api.system.has_traditional_systems', 'R', 'Returns whether there are traditional systems registered')
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
    VALUES ('api.system.schedule_support_data_upload', 'W', 'Schedule fetch and upload support data from a system to SCC')
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
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.proxy.backup_configuration', 'W', 'Saves the configuration of a proxy to the server for later conversion')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
