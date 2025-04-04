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
    VALUES ('schedule.completed', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.archived', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('schedule.details', 'R', NULL)
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
    VALUES ('systems.groups.patches', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.groups.admins', 'R', NULL)
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
    VALUES ('systems.activation_keys.details', 'W', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.activation_keys.details', 'R', NULL)
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
    VALUES ('users.systems', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('users.channels', 'R', NULL)
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
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/audit/cve', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/cve', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/cve.csv', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/ListXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/ListXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/DiffSubmit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/DiffSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/scap/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Overview.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Machine.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Machine.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/audit/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/audit/confidential-computing', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/audit/confidential-computing/listAttestations', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/projects', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/project/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/project', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/properties', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/softwaresources', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/filters', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/environments', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/build', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/projects/:projectId/promote', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/contentmanagement/filters', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters/:filterId', 'PUT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/filters/:filterId', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/appstreams/:channelId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/channels/modular', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/products', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/kernels/product/:productId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/contentmanagement/livepatching/kernels/system/:systemId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/GlobalConfigChannelList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelOverview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChannelSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/TargetSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Systems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Systems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/Copy2Channels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelUploadFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelUploadFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelImportFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelImportFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreateFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreateFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseFilesSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/ChooseSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/DeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/channel/DeployConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/ChannelCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/DeleteChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/DeleteChannel.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalConfigFileList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalConfigFileList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/ManageRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/ManageRevisionSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareCopy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareDeployed.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CompareDeployedSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DownloadDiff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/LocalConfigFileList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/LocalConfigFileList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/FileDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteFile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteRevision.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/DeleteRevision.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileCentral.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileCentralSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileLocal.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/CopyFileSandbox.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/RevisionDeploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeploySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/file/GlobalRevisionDeployConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/ManagedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/ManagedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/configuration/system/TargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/YourRhn.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/YourRhnClips.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/subscription-warning', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/tasks', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/inactive-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/critical-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/pending-actions', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/latest-errata', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/systems-groups', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/recent-systems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/data-unread', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/data-all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/update-messages-status', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/retry-onboarding/:minionId', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/notification-messages/retry-reposync/:channelId', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserDetailsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/Addresses.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/EditAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/EditAddressSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/ChangeEmail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/ChangeEmailSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/AccountDeactivationConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/UserPreferences.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/account/PrefSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgConfigDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgConfigDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/Organizations.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/OrgTrustDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Provided.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Consumed.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Provided.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/multiorg/channels/Consumed.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/yourorg/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/targets/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/yourorg/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/:channelId/content', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/images', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/inspect/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/inspect/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/patches/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/packages/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/buildlog/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/patches/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/packages/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/buildlog/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/import', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/images/import', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/upload/image', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/type/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/hosts/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/activationkeys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/find/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/find/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imageprofiles/edit/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/channels/:token', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/type/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores/create', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/find/:label', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/imagestores/edit/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imagestores/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/imageprofiles/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/cm/build', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/hosts/:type', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/cm/build/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantBugErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantBugErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantEnhancementErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantEnhancementErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantSecurityErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/RelevantSecurityErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllBugErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllBugErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllEnhancementErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllEnhancementErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllSecurityErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/AllSecurityErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/Details.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/SystemsAffected.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/SystemsAffected.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/ErrataConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/details/ErrataConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Errata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Errata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CreateSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/DeleteBug.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagesConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagesConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagePackagePush.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddPackagePackagePushSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/RemovePackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/RemovePackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddChannelPackagePush.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/AddChannelPackagePushSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ChannelsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ListPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/ListPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Notify.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/NotifySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PublishedDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/PublishedDeleteConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/errata/manage/CloneConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/keys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/accept', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/reject', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/keys/:target/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/cmd', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/formula-catalog', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formula-catalog/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/formula-catalog/formula/:name', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formula-catalog/formula/:name/data', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActionsDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/PendingActionsDeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ArchivedActions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ArchivedActions.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/CompletedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/InProgressSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/InProgressSystemsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/FailedSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChains.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChains.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChain.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/action-chain-entries', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/schedule/ActionChain.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/action-chain-save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/maintenance/schedules', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/names', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/setsystems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/systems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/schedule/maintenance/calendars', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/events/:operation/:type/:startOfWeek/:date/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/setsystems', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/calendar/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/All.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/All.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Vendor.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Vendor.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Popular.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Popular.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Custom.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Custom.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Shared.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Shared.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Retired.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/software/channels/Retired.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelDetail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelDetail.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Managers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/Managers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelSubscribers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ChannelSubscribers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/TargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/TargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ConfirmTargetSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/ConfirmTargetSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/AppStreams.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/AppStreams.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/software/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/software/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Manage.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Manage.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Managers.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Managers.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Errata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ListRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ListRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddRedHatErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddCustomErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddCustomErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/ConfirmErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddErrataToChannel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/SyncErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/SyncErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/errata/AddRedHatErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackageMenu.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAdd.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAdd.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAddConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesAddConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompare.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompare.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMerge.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMerge.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMergeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/ChannelPackagesCompareMergeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Repositories.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/Repositories.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/AssociatedChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/AssociatedChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/repos/RepoDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMap.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMap.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channels/manage/DistChannelMapDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/index.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewAllLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewAllLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewCompletedLog.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/ViewCompletedLog.do', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/list/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/list/virtual', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/list/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/list/virtual', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/BootstrapSystemList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPv6List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateIPv6List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateHostName.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateHostName.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateMacAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateMacAddress.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateSystemsCompare.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/DuplicateSystemsCompare.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemCurrency.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemCurrency.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemEntitlements.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemEntitlementsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/csv/all', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/csv/virtualSystems', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/ListErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/ExtraPackagesList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/ExtraPackagesList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/ShowProfiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/ShowProfiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/CompareProfiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgrade.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageUpgrade.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageInstall.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageInstall.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageSchedule.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageSchedule.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemoveSchedule.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ssm/PackageRemoveSchedule.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradableList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradableList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/UpgradeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/InstallConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/VerifyConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/LockPackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/LockPackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveExtraConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/RemoveExtraConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/DeleteProfile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/profiles/DeleteProfile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Manage.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Manage.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemGroupList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/SystemGroupList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/ListRemove.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/ListRemove.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Confirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Confirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AddSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AddSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/groups/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/EditGroup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/EditGroup.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/channel/ssm/ChannelSubscriptions.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels/bases', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels/allowed-changes', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/channels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/upcoming-windows', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Deploy.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeploySubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeployConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DeployConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Diff.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/DiffConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DeployFileConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/DiffFileConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Subscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Rank.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/SubscribeConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Unsubscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/UnsubscribeConfirmSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/config/Enable.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/SubscriptionsSetup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/SubscriptionsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/RankChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/KickstartableSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/KickstartableSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByProfile.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByProfile.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByIp.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/kickstart/ScheduleByIp.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementConfiguration.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementConfiguration.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementOperations.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/PowerManagementOperations.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/CreateProfileWizard.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/CreateProfileWizard.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDeleteAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDeleteAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCloneAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCloneAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEditAdvanced.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/EditVariables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartClone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartClone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDetailsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartSoftwareEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOptionsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/SystemDetailsEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Locale.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPartitionEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationListsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysListSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Troubleshooting.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackagesEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartScriptOrder.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeyEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/tree/EditVariables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/audit/ScheduleXccdfConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScheduleXccdf.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScheduleXccdf.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/coco/settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/coco/schedule', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/coco/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/settings', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/listAttestations', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/Index.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/Index.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/ConfirmSystemPreferences.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/EditNote.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/EditNote.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/HardwareRefresh.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/HardwareRefresh.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemHardware.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SoftwareRefresh.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SoftwareRefresh.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/packages/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/maintenance', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/:id/assign', 'PSOT', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/maintenance/schedule/unassign', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/RemoteCommand.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/provisioning/RemoteCommand.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemRemoteCommand.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemRemoteCommand.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/CustomValue.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/CustomValue.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ListCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SetCustomValue.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/SetCustomValue.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/CreateCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/UpdateCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/UpdateCustomData.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteCustomData.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteCustomData.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystem.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystem.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystemConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/misc/RebootSystemConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RebootSystem.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RebootSystem.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/ssm/proxy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/proxy', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/proxy', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/MigrateSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/MigrateSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemMigrate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemMigrate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/DeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/ssm/DeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/DeleteConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Index.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Index.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTags.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTags.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Groups.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Groups.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Channels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Channels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigChannels.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigFiles.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/ConfigFiles.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/UnservablePackages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/UnservablePackages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Tags.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Tags.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTagCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/SnapshotTagCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Rollback.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/Rollback.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagsDelete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/snapshots/TagsDelete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/WorkWithGroup.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/GroupDetail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListRemoveSystems.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListRemoveSystems.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListErrata.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/ListErrata.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/SystemsAffected.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/SystemsAffected.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AdminList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/groups/AdminList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/applyall', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/formulas', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/list/:targetType/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/formula/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/form/:targetType/:id/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/select', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/groups/details/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/targets/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Notes.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/RemoveFromSSM.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/AddToSSM.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Connection.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ProxyClients.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ProxyClients.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/mgr-server-info/:sid', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/mgr-server-reportdb-newpw', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Activation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/Activation.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemHardware.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/ErrataConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SystemChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels-available-base', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/channels/:channelId/accessible-children', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SPMigration.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/SPMigration.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/appstreams', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/appstreams/:channelId/:appstream/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/appstreams/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/overview', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/allowedActions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/installed', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ptf/install', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/available', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/ptf/scheduleAction', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/Overview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewCentralPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewCentralPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyCentralPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyCentralPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyLocalPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifyLocalPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifySandboxPaths.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ViewModifySandboxPaths.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ConfigChannelList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/ConfigChannelListUnsubscribeSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/configuration/RankChannels.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/groups/Add.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ListScap.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ListScap.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/XccdfDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/ScapResultDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/audit/RuleDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/coco/settings', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/applyall', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/packages/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/custom', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/match', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/apply', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/formulas', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/list/:targetType/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/formula/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/form/:targetType/:id/:formula_id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/select', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/formulas/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/control-node', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/:minionServerId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/playbooks', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/:pathType/:minionServerId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/discover-playbooks/:pathId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/playbook-contents', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/ansible/inventories', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/introspect-inventory/:pathId', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/delete', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/paths/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/details/ansible/schedule-playbook', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/recurring-actions', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:type/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/summary', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/states', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/save', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/states/highstate', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/custom/execute', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/details', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/:id/delete', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Pending.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/History.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/History.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Event.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Pending.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/Event.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/FailEventConfirmation.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/history/FailEventConfirmation.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/bootstrap', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/bootstrap', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/proxy/container-config', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/proxy/container-config', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/proxy/container-config/:filename', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/Search.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/Search.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Create.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Create.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Edit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Clone.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Clone.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Edit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/:tid/channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/base-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/activation-keys/base-channels/:cid/child-channels', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/admin/mandatoryChannels', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/systems/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/systems/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/packages/Packages.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/packages/Packages.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Subscribe.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Subscribe.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Rank.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/configuration/Rank.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/List.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/Add.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/groups/Add.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/activationkeys/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/List.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Details.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/PackageList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/PackageList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Details.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Delete.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/profiles/Delete.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CustomDataList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CustomDataList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CreateCustomKey.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/CreateCustomKey.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/UpdateCustomKey.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/customdata/UpdateCustomKey.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOverview.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Kickstarts.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Kickstarts.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/AdvancedModeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEditAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/EditVariables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownloadAdvanced.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRanges.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRanges.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartIpRangeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartDetailsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartSoftwareEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartOptionsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/SystemDetailsEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Locale.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPartitionEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationLists.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFilePreservationLists.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartCryptoKeysList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Troubleshooting.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackagesEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackageProfileEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartPackageProfileEdit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeys.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeys.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ActivationKeysList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Scripts.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/Scripts.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownload.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/KickstartFileDownload.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/keys/CryptoKeysList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ViewTrees.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/ViewTrees.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/TreeEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/tree/EditVariables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListCreate.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListCreate.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/provisioning/preservation/PreservationListDeleteSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CustomSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CustomSnippetList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/DefaultSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/DefaultSnippetList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetView.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/kickstart/cobbler/CobblerSnippetEdit.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/ScheduleWizard.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/ScheduleWizard.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionCancel.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionCancel.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/Variables.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/PowerManagement.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/PowerManagement.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionStatus.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/SessionStatus.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/systems/details/kickstart/Variables.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/vhms', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/modules', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id/nodes', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/:id/refresh', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/module/:name/params', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/create', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/update/:id', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/vhms/delete/:id', 'DELETE', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ActiveList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ActiveList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisabledList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisabledList.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserList.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableConfirm.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableConfirm.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/Addresses.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserDetailsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChangeEmail.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChangeEmailSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisableUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DisableUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EnableUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DeleteUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/DeleteUserSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/CreateUser.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EditAddress.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/EditAddressSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/AssignedSystemGroups.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/AssignedSystemGroups.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemsAdmined.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemsAdminedSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelPerms.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelManagementPerms.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ChannelPermsSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/UserPreferences.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/PrefSubmit.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemGroupConfig.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgMapping.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgMapping.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/SystemGroupConfig.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgDetails.do', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/users/ExtAuthSgDetails.do', 'POST', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/login', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/Logout.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.webui.controllers.login.LoginController', '/manager/api/login', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.suse.manager.webui.controllers.login.LoginController', '/manager/api/logout', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.login', '/manager/api/auth/login', 'POST', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.logout', '/manager/api/auth/logout', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.isSessionKeyValid', '/manager/api/auth/isSessionKeyValid', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.checkAuthToken', '/manager/api/auth/checkAuthToken', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler.getDuration', '/manager/api/auth/getDuration', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getVersion', '/manager/api/api/getVersion', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiCallList', '/manager/api/api/getApiCallList', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiNamespaceCallList', '/manager/api/api/getApiNamespaceCallList', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.systemVersion', '/manager/api/api/systemVersion', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.getApiNamespaces', '/manager/api/api/getApiNamespaces', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.api.ApiHandler.productName', '/manager/api/api/productName', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler.hasMaster', '/manager/api/sync/master/hasMaster', 'GET', 'A', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/ajax/item-selector', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/frontend-log', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/sets/:label', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/sets/:label/clear', 'POST', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/CSVDownloadAction.do', 'GET', 'W', False)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/audit/cve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/cve' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/cve.csv' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/ListXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/ListXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/DiffSubmit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/DiffSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/scap/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Overview.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Machine.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Machine.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/audit/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/audit/confidential-computing' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/confidential-computing/listAttestations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/projects' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/project/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/contentmanagement/project' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/properties' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.sources' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/softwaresources' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.filters' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/filters' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.environments' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/environments' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/build' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.project.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/projects/:projectId/promote' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/contentmanagement/filters' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters/:filterId' AND ep.http_method = 'PUT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/filters/:filterId' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/appstreams/:channelId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/channels/modular' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/products' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/kernels/product/:productId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'clm.filter.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/contentmanagement/livepatching/kernels/system/:systemId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/GlobalConfigChannelList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/ChannelFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/ChannelSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/ChannelSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/channel/TargetSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Systems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Systems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/Copy2Channels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelUploadFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelUploadFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelImportFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelImportFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreateFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreateFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseFilesSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/ChooseSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/DeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/channel/DeployConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/ChannelCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/DeleteChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/DeleteChannel.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/GlobalConfigFileList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/GlobalConfigFileList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/FileDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/FileDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/ManageRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/ManageRevisionSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareCopy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareDeployed.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/CompareDeployedSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/DownloadDiff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/LocalConfigFileList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/file/LocalConfigFileList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/FileDetails.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteFile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteRevision.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/DeleteRevision.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileCentral.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileCentralSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileLocal.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/CopyFileSandbox.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/RevisionDeploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeploySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/configuration/file/GlobalRevisionDeployConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/ManagedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/ManagedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'config.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/configuration/system/TargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/YourRhn.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/YourRhnClips.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.main' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/subscription-warning' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.tasks' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/tasks' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.inactive_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/inactive-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.critical_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/critical-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.pending_actions' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/pending-actions' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.latest_errata' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/latest-errata' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.system_groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/systems-groups' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.overview.panels.recent_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/recent-systems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages/data-unread' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/notification-messages/data-all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/update-messages-status' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications.retry' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/retry-onboarding/:minionId' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.notifications.retry' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/notification-messages/retry-reposync/:channelId' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/UserDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/UserDetailsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/Addresses.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/EditAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.address' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/EditAddressSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.email' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/ChangeEmail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.email' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/ChangeEmailSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.deactivate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/AccountDeactivationConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.preferences' AND ns.access_mode = 'R'
    AND ep.endpoint = '/account/UserPreferences.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.preferences' AND ns.access_mode = 'W'
    AND ep.endpoint = '/account/PrefSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/OrgConfigDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/multiorg/OrgConfigDetails.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/Organizations.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/OrgTrustDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Provided.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Consumed.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Provided.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.trust' AND ns.access_mode = 'R'
    AND ep.endpoint = '/multiorg/channels/Consumed.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/yourorg/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/targets/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/yourorg/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/:channelId/content' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.config_channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/images' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/inspect/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/inspect/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/patches/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/packages/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/buildlog/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/patches/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/packages/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/images/buildlog/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/import' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/images/import' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/upload/image' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/type/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/hosts/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.image.import' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/activationkeys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/imageprofiles/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imageprofiles/edit/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/channels/:token' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.profile.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores/type/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imagestores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/imagestores/create' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/find/:label' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imagestores/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/cm/imagestores/edit/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.store.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/cm/imagestores/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/imageprofiles/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/cm/build' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/hosts/:type' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'cm.build' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/cm/build/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantBugErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantBugErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantEnhancementErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantEnhancementErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantSecurityErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/RelevantSecurityErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllBugErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllBugErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllEnhancementErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllEnhancementErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllSecurityErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/AllSecurityErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/Details.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/SystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/details/SystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/details/ErrataConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/details/ErrataConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Errata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Errata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CreateSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/DeleteBug.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagesConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagesConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagePackagePush.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddPackagePackagePushSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/RemovePackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/RemovePackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddChannelPackagePush.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/AddChannelPackagePushSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ChannelsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ListPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/errata/manage/ListPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.notify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Notify.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.notify' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/NotifySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PublishedDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/PublishedDeleteConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'patches.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/errata/manage/CloneConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/keys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/keys' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/accept' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/reject' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/keys/:target/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.keys' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/cmd' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/formula-catalog' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula-catalog/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/formula-catalog/formula/:name' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'salt.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formula-catalog/formula/:name/data' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/PendingActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/PendingActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/PendingActionsDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.pending' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/PendingActionsDeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.failed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.failed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.completed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.completed' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.archived' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ArchivedActions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.archived' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ArchivedActions.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/CompletedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/InProgressSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/InProgressSystemsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/FailedSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChains.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChains.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/schedule/ActionChain.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ajax/action-chain-entries' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'W'
    AND ep.endpoint = '/schedule/ActionChain.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.action_chains' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ajax/action-chain-save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/maintenance/schedules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/names' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/setsystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/schedule/systems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/schedule/maintenance/calendars' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/calendar/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/maintenance/events/:operation/:type/:startOfWeek/:date/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/setsystems' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'schedule.maintenance_windows' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/calendar/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/All.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/All.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Vendor.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Vendor.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Popular.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Popular.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Custom.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Custom.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Shared.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Shared.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Retired.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/software/channels/Retired.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelDetail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ChannelDetail.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Managers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.managers' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/Managers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelSubscribers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/ChannelSubscribers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/TargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/TargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ConfirmTargetSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/ConfirmTargetSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/AppStreams.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.details.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/AppStreams.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/software/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/software/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Manage.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.list' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Manage.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.managers' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Managers.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.managers' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Managers.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.clone' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Errata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ListRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ListRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddRedHatErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddCustomErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddCustomErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/ConfirmErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/AddErrataToChannel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/SyncErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/errata/SyncErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/errata/AddRedHatErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackageMenu.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAdd.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAdd.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAddConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesAddConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompare.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompare.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMerge.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMerge.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMergeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/ChannelPackagesCompareMergeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Repositories.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/Repositories.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/AssociatedChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/AssociatedChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.manage.repos' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/repos/RepoDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/DistChannelMap.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'R'
    AND ep.endpoint = '/channels/manage/DistChannelMap.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'software.distro' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channels/manage/DistChannelMapDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewAllLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewAllLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewCompletedLog.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ssm' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/ViewCompletedLog.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/list/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/list/virtual' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/list/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/list/virtual' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/BootstrapSystemList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPv6List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateIPv6List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateHostName.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateHostName.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateMacAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateMacAddress.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateSystemsCompare.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/DuplicateSystemsCompare.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemCurrency.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemCurrency.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemEntitlements.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemEntitlementsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/csv/all' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/csv/virtualSystems' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/ListErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ErrataList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ErrataList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/ssm/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/ExtraPackagesList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/ExtraPackagesList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/ShowProfiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/ShowProfiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/packages/profiles/CompareProfiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgrade.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageUpgrade.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageInstall.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageInstall.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageSchedule.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageSchedule.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemoveSchedule.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/ssm/PackageRemoveSchedule.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradableList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradableList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/UpgradeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/InstallConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/VerifyConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/LockPackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/LockPackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveExtraConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/RemoveExtraConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/DeleteProfile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/profiles/DeleteProfile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/groups/Manage.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/groups/Manage.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemGroupList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/SystemGroupList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/groups/ListRemove.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/groups/ListRemove.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Confirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Confirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/AddSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.target_systems' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/AddSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/groups/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/EditGroup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/EditGroup.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/channel/ssm/ChannelSubscriptions.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels/bases' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels/allowed-changes' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/upcoming-windows' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/channels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Deploy.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeploySubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeployConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DeployConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Diff.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/DiffConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DeployFileConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/DiffFileConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Subscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Rank.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/SubscribeConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Unsubscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/UnsubscribeConfirmSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/config/Enable.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/SubscriptionsSetup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/SubscriptionsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/configuration/RankChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/KickstartableSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/KickstartableSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByProfile.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByProfile.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByIp.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/kickstart/ScheduleByIp.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementConfiguration.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementConfiguration.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementOperations.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/PowerManagementOperations.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/CreateProfileWizard.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/CreateProfileWizard.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/AdvancedModeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDeleteAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDeleteAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCloneAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCloneAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEditAdvanced.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/EditVariables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartClone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartClone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartIpRangeDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartDetailsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartSoftwareEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartOptionsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/SystemDetailsEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/Locale.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartPartitionEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationListsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysListSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/Troubleshooting.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartPackagesEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/ActivationKeysSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/KickstartScriptOrder.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/keys/CryptoKeyEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/tree/EditVariables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/TreeDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'W'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/audit/ScheduleXccdfConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/XccdfDeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/XccdfDeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/ScheduleXccdf.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/audit/ScheduleXccdf.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/coco/settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/coco/schedule' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/coco/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/settings' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/listAttestations' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/ssm/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/Index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/Index.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/ConfirmSystemPreferences.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/EditNote.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/EditNote.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/HardwareRefresh.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/HardwareRefresh.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemHardware.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SoftwareRefresh.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SoftwareRefresh.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/packages/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/maintenance' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/:id/assign' AND ep.http_method = 'PSOT'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.maintenance' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/maintenance/schedule/unassign' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/RemoteCommand.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/provisioning/RemoteCommand.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemRemoteCommand.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.remote_commands' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemRemoteCommand.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/misc/CustomValue.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/ssm/misc/CustomValue.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ListCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SetCustomValue.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/SetCustomValue.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/CreateCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/UpdateCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/UpdateCustomData.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteCustomData.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteCustomData.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystem.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystem.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystemConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/misc/RebootSystemConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/RebootSystem.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.reboot' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/RebootSystem.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/ssm/proxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/proxy' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/proxy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/MigrateSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/MigrateSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemMigrate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.transfer' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SystemMigrate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/DeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/ssm/DeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/DeleteConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Index.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Index.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTags.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTags.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Groups.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Groups.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Channels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Channels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigChannels.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigFiles.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/ConfigFiles.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/UnservablePackages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/UnservablePackages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Tags.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/snapshots/Tags.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTagCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/SnapshotTagCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/Rollback.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/Rollback.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagsDelete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/snapshots/TagsDelete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/WorkWithGroup.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/GroupDetail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.details.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/groups/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListRemoveSystems.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListRemoveSystems.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListErrata.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/ListErrata.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/SystemsAffected.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.patches' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/SystemsAffected.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/AdminList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.admins' AND ns.access_mode = 'R'
    AND ep.endpoint = '/groups/AdminList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/applyall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/formulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/list/:targetType/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/formula/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/form/:targetType/:id/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/select' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/groups/details/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/targets/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Notes.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/RemoveFromSSM.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/AddToSSM.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.connection' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Connection.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ProxyClients.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.proxy' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/ProxyClients.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.peripheral' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/mgr-server-info/:sid' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.peripheral' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/mgr-server-reportdb-newpw' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.activation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Activation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.activation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/Activation.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.details.hardware' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/SystemHardware.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/ErrataConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.patches' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/ErrataConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/SystemChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels-available-base' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/channels/:channelId/accessible-children' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SPMigration.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.migration' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/SPMigration.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/appstreams' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/appstreams/:channelId/:appstream/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.appstreams' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/appstreams/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ptf/overview' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ptf/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/allowedActions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/installed' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/details/ptf/install' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/available' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.software.ptf' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/ptf/scheduleAction' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.overview' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/Overview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewCentralPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewCentralPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyCentralPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyCentralPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyLocalPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifyLocalPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifySandboxPaths.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.files' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ViewModifySandboxPaths.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ConfigChannelList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/ConfigChannelListUnsubscribeSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.config.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/configuration/RankChannels.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.join' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/groups/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.join' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/groups/Add.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ListScap.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ListScap.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/XccdfDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/ScapResultDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.openscap' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/audit/RuleDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/coco/settings' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.highstate' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/applyall' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/packages' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/packages/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/packages/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/custom' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/states/match' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.states.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/apply' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/formulas' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/list/:targetType/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/formula/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/formulas/form/:targetType/:id/:formula_id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/select' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.formulas' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/formulas/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/control-node' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/:minionServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/playbooks' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/:pathType/:minionServerId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/discover-playbooks/:pathId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/playbook-contents' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/ansible/inventories' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/details/ansible/introspect-inventory/:pathId' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/delete' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/paths/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.ansible' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/details/ansible/schedule-playbook' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/recurring-actions' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/recurringactions/:type/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/summary' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/states' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/save' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/states/highstate' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/custom/execute' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/details' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/recurringactions/:id/delete' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/Pending.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/History.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/History.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/history/Event.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/Pending.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/Event.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/FailEventConfirmation.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.events' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/history/FailEventConfirmation.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/systems/bootstrap' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.bootstrap' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/bootstrap' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/proxy/container-config' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/container-config' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.proxy' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/proxy/container-config/:filename' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/Search.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.search' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/Search.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.list' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Create.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Create.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Edit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Clone.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Clone.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/Edit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/:tid/channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/base-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/activation-keys/base-channels/:cid/child-channels' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/admin/mandatoryChannels' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/systems/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/systems/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.packages' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/packages/Packages.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.packages' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/packages/Packages.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/configuration/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/configuration/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Subscribe.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Subscribe.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Rank.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/configuration/Rank.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/groups/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/activationkeys/groups/List.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/groups/Add.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.groups' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/groups/Add.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.activation_keys.delete' AND ns.access_mode = 'W'
    AND ep.endpoint = '/activationkeys/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/List.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/Details.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/PackageList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'R'
    AND ep.endpoint = '/profiles/PackageList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Details.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Delete.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.profiles' AND ns.access_mode = 'W'
    AND ep.endpoint = '/profiles/Delete.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/customdata/CustomDataList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/customdata/CustomDataList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/CreateCustomKey.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/CreateCustomKey.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/UpdateCustomKey.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.custom_data' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/customdata/UpdateCustomKey.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartOverview.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Kickstarts.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Kickstarts.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/AdvancedModeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEditAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/EditVariables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownloadAdvanced.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRanges.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRanges.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartIpRangeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartDetailsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartSoftwareEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartOptionsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/SystemDetailsEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Locale.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPartitionEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationLists.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFilePreservationLists.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartCryptoKeysList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Troubleshooting.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackagesEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackageProfileEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartPackageProfileEdit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeys.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeys.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ActivationKeysList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Scripts.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/Scripts.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownload.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/KickstartFileDownload.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/keys/CryptoKeysList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ViewTrees.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/ViewTrees.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/TreeEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/tree/EditVariables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListCreate.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListCreate.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/provisioning/preservation/PreservationListDeleteSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CustomSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CustomSnippetList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/DefaultSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/DefaultSnippetList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetView.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation' AND ns.access_mode = 'R'
    AND ep.endpoint = '/kickstart/cobbler/CobblerSnippetEdit.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/ScheduleWizard.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/ScheduleWizard.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/SessionCancel.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/SessionCancel.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/Variables.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/PowerManagement.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'W'
    AND ep.endpoint = '/systems/details/kickstart/PowerManagement.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/SessionStatus.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/SessionStatus.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.autoinstallation.provisioning' AND ns.access_mode = 'R'
    AND ep.endpoint = '/systems/details/kickstart/Variables.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/vhms' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/modules' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/:id' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/vhms/:id/nodes' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/:id/refresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/module/:name/params' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/create' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/update/:id' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.vhms' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/vhms/delete/:id' AND ep.http_method = 'DELETE'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.active' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ActiveList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.active' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ActiveList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/DisabledList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/DisabledList.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.all' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserList.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableConfirm.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.list.disabled' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableConfirm.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/Addresses.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/UserDetailsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ChangeEmail.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ChangeEmailSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DisableUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DisableUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EnableUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DeleteUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/DeleteUserSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/CreateUser.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EditAddress.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.details' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/EditAddressSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/AssignedSystemGroups.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.groups' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/AssignedSystemGroups.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemsAdmined.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemsAdminedSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelPerms.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelManagementPerms.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.channels' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ChannelPermsSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.preferences' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/UserPreferences.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.preferences' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/PrefSubmit.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/SystemGroupConfig.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ExtAuthSgMapping.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'R'
    AND ep.endpoint = '/users/ExtAuthSgMapping.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/SystemGroupConfig.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ExtAuthSgDetails.do' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'users.group_config' AND ns.access_mode = 'W'
    AND ep.endpoint = '/users/ExtAuthSgDetails.do' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
