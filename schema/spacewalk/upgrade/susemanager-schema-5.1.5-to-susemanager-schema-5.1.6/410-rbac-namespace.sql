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

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'audit.cve', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'audit.cve' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'audit.openscap', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'audit.openscap' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'audit.coco', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'audit.coco' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.sources', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.sources' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.filters', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.environments', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.environments' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.project.build', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.project.build' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.filter.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.filter.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'clm.filter.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'clm.filter.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.channels', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.channels', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.files', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.files', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.files' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'config.systems', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'config.systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.main', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.main' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.tasks', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.tasks' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.inactive_systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.inactive_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.critical_systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.critical_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.pending_actions', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.pending_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.latest_errata', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.latest_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.system_groups', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.system_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.overview.panels.recent_systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.overview.panels.recent_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.notifications', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.notifications' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.notifications', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.notifications' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.notifications.retry', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.notifications.retry' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.address', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.address' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.address', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.address' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.email', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.email' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.deactivate', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.deactivate' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.preferences', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.preferences' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.preferences', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.preferences' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.config', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.config', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.trust', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.trust' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.recurring', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.recurring' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.recurring', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.recurring' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.config_channels', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.config_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'home.account.myorg.config_channels', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'home.account.myorg.config_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.image.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.image.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.image.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.image.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.image.overview', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.image.overview' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.image.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.image.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.image.import', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.image.import' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.profile.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.profile.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.profile.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.profile.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.profile.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.profile.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.profile.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.profile.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.store.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.store.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.store.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.store.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.store.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.store.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.store.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.store.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'cm.build', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'cm.build' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.details.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.details.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.details.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.details.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.details.systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.details.systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.details.systems', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.details.systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.search', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.search' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.manage.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.manage.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.manage.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.manage.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.manage.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.manage.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.manage.notify', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.manage.notify' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.manage.delete', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.manage.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'patches.clone', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'patches.clone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'salt.keys', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'salt.keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'salt.keys', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'salt.keys' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'salt.formulas', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'salt.formulas' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.pending', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.pending' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.pending', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.pending' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.failed', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.failed' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.failed', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.failed' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.completed', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.completed' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.completed', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.completed' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.archived', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.archived' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.archived', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.archived' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.action_chains', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.action_chains' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.action_chains', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.action_chains' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.recurring', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.recurring' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.maintenance_windows', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.maintenance_windows' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'schedule.maintenance_windows', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'schedule.maintenance_windows' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.overview', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.overview' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.managers', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.managers' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.managers', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.managers' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.patches', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.patches' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.systems', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.details.appstreams', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.details.appstreams' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.search', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.search' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.delete', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.managers', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.managers' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.clone', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.clone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.patches', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.patches' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.patches', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.patches' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.packages', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.manage.repos', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.manage.repos' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.distro', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.distro' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'software.distro', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'software.distro' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.ssm', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.ssm' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.patches', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.patches' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.packages', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.target_systems', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.target_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.channels', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.config.files', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.config.files' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.config.channels', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.config.channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.autoinstallation', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.autoinstallation' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.audit.openscap', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.audit.openscap' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.audit.coco', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.audit.coco' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.highstate', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.highstate' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.overview', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.overview' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.hardware', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.hardware' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.refresh', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.refresh' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.maintenance', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.maintenance' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.remote_commands', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.remote_commands' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.custom_data', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.custom_data' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.custom_data', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.custom_data' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.reboot', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.reboot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.connection', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.connection' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.transfer', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.transfer' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.delete', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.snapshots', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.snapshots' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.snapshots', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.snapshots' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.details.delete', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.details.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.systems', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.patches', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.patches' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.patches', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.patches' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.admins', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.admins' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.admins', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.admins' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.config', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.highstate', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.highstate' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.highstate', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.highstate' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.config', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.formulas', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.formulas' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.formulas', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.formulas' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.recurring', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.recurring' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.recurring', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.recurring' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.connection', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.connection' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.proxy', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.proxy' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.peripheral', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.peripheral' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.peripheral', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.peripheral' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.activation', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.activation' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.details.hardware', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.details.hardware' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.patches', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.patches' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.channels', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.migration', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.migration' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.appstreams', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.appstreams' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.appstreams', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.appstreams' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.ptf', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.ptf' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.software.ptf', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.software.ptf' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.config.overview', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.config.overview' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.config.files', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.config.files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.config.channels', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.config.channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.groups.join', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.groups.join' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.audit.openscap', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.audit.openscap' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.audit.coco', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.audit.coco' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.highstate', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.highstate' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.packages', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.config', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.states.config', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.states.config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.formulas', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.formulas' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.formulas', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.formulas' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.ansible', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.ansible' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.ansible', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.ansible' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.recurring', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.recurring' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.recurring', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.recurring' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.events', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.events' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.events', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.events' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.bootstrap', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.bootstrap' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.proxy', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.search', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.search' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.list', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.list', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.packages', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.packages', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.config', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.config', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.groups', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.groups', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.activation_keys.delete', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.activation_keys.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.profiles', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.profiles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.profiles', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.profiles' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.custom_data', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.custom_data' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.custom_data', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.custom_data' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.autoinstallation', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.autoinstallation' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.autoinstallation.provisioning', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.autoinstallation.provisioning' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.autoinstallation.provisioning', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.autoinstallation.provisioning' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.vhms', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.vhms' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'systems.vhms', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'systems.vhms' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.list.active', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.list.active' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.list.disabled', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.list.disabled' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.list.all', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.list.all' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.list.disabled', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.list.disabled' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.details', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.details', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.groups', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.groups', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.systems', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.channels', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.channels', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.preferences', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.preferences' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.preferences', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.preferences' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.group_config', 'R', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.group_config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'users.group_config', 'W', NULL
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'users.group_config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.create_role', 'W', 'Create a new role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.create_role' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.delete_role', 'W', 'Delete a role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.delete_role' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.grant_access', 'W', 'Grant access to the given namespace for the specified role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.grant_access' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.list_namespaces', 'R', 'List available namespaces.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.list_namespaces' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.list_permissions', 'R', 'List permissions granted by a role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.list_permissions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.list_roles', 'R', 'List existing roles.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.list_roles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.access.revoke_access', 'W', 'Revoke access to the given namespace for the specified role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.access.revoke_access' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_configuration_deployment', 'W', 'Adds an action to deploy a configuration file to an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_configuration_deployment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_errata_update', 'W', 'Adds Errata update to an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_errata_update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_package_install', 'W', 'Adds package installation action to an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_package_install' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_package_removal', 'W', 'Adds an action to remove installed packages on the system to an Action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_package_removal' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_package_upgrade', 'W', 'Adds an action to upgrade installed packages on the system to an Action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_package_upgrade' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_package_verify', 'W', 'Adds an action to verify installed packages on the system to an Action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_package_verify' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_script_run', 'W', 'Add an action with label to run a script to an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_script_run' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.add_system_reboot', 'W', 'Add system reboot to an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.add_system_reboot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.create_chain', 'W', 'Create an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.create_chain' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.delete_chain', 'W', 'Delete action chain by label.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.delete_chain' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.list_chain_actions', 'R', 'List all actions in the particular Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.list_chain_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.list_chains', 'R', 'List currently available action chains.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.list_chains' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.remove_action', 'W', 'Remove an action from an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.remove_action' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.rename_chain', 'W', 'Rename an Action Chain.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.rename_chain' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.actionchain.schedule_chain', 'W', 'Schedule the Action Chain so that its actions will actually occur.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.actionchain.schedule_chain' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_app_streams', 'W', 'Add app streams to an activation key. If any of the provided app streams is not available in the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_app_streams' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_child_channels', 'W', 'Add child channels to an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_child_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_config_channels', 'W', 'Given a list of activation keys and configuration channels,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_config_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_entitlements', 'W', 'Add add-on System Types to an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_entitlements' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_packages', 'W', 'Add packages to an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.add_server_groups', 'W', 'Add server groups to an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.add_server_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.check_config_deployment', 'W', 'Check configuration file deployment status for the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.check_config_deployment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.clone', 'W', 'Clone an existing activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.clone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.create', 'W', 'Create a new activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.delete', 'W', 'Delete an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.disable_config_deployment', 'W', 'Disable configuration file deployment for the specified activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.disable_config_deployment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.enable_config_deployment', 'W', 'Enable configuration file deployment for the specified activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.enable_config_deployment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.get_details', 'R', 'Lookup an activation key''s details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.list_activated_systems', 'R', 'List the systems activated with the key provided.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.list_activated_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.list_activation_keys', 'R', 'List activation keys that are visible to the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.list_activation_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.list_channels', 'R', 'List the channels for the given activation key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.list_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.list_config_channels', 'R', 'List configuration channels'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.list_config_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_app_streams', 'W', 'Remove app streams from an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_app_streams' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_child_channels', 'W', 'Remove child channels from an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_child_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_config_channels', 'W', 'Remove configuration channels from the given activation keys.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_config_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_entitlements', 'W', 'Remove entitlements (by label) from an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_entitlements' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_packages', 'W', 'Remove package names from an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.remove_server_groups', 'W', 'Remove server groups from an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.remove_server_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.set_config_channels', 'W', 'Replace the existing set of'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.set_config_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.activationkey.set_details', 'W', 'Update the details of an activation key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.activationkey.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.configuration.configure', 'W', 'Configure server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.configuration.configure' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.monitoring.disable', 'W', 'Disable monitoring.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.monitoring.disable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.monitoring.enable', 'W', 'Enable monitoring.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.monitoring.enable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.monitoring.get_status', 'R', 'Get the status of each Prometheus exporter.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.monitoring.get_status' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.payg.create', 'W', 'Create a new ssh connection data to extract data from'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.payg.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.payg.delete', 'W', 'Returns a list of ssh connection data registered.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.payg.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.payg.get_details', 'W', 'Returns a list of ssh connection data registered.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.payg.get_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.payg.list', 'W', 'Returns a list of ssh connection data registered.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.payg.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.admin.payg.set_details', 'W', 'Updates the details of a ssh connection data'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.admin.payg.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.create_ansible_path', 'W', 'Create ansible path'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.create_ansible_path' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.discover_playbooks', 'W', 'Discover playbooks under given playbook path with given pathId'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.discover_playbooks' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.fetch_playbook_contents', 'W', 'Fetch the playbook content from the control node using a synchronous salt call.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.fetch_playbook_contents' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.introspect_inventory', 'W', 'Introspect inventory under given inventory path with given pathId and return it in a structured way'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.introspect_inventory' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.list_ansible_paths', 'R', 'List ansible paths for server (control node)'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.list_ansible_paths' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.lookup_ansible_path_by_id', 'R', 'Lookup ansible path by path id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.lookup_ansible_path_by_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.remove_ansible_path', 'W', 'Create ansible path'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.remove_ansible_path' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.schedule_playbook', 'W', 'Schedule a playbook execution'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.schedule_playbook' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.ansible.update_ansible_path', 'W', 'Create ansible path'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.ansible.update_ansible_path' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.access.disable_user_restrictions', 'W', 'Disable user restrictions for the given channel.  If disabled,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.access.disable_user_restrictions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.access.enable_user_restrictions', 'W', 'Enable user restrictions for the given channel. If enabled, only'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.access.enable_user_restrictions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.access.get_org_sharing', 'R', 'Get organization sharing access control.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.access.get_org_sharing' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.access.set_org_sharing', 'W', 'Set organization sharing access control.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.access.set_org_sharing' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.appstreams.is_modular', 'R', 'Check if channel is modular.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.appstreams.is_modular' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.appstreams.list_modular', 'R', 'List modular channels in users organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.appstreams.list_modular' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.appstreams.list_module_streams', 'R', 'List available module streams for a given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.appstreams.list_module_streams' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_all_channels', 'R', 'List all software channels that the user''s organization is entitled to.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_all_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_manageable_channels', 'R', 'List all software channels that the user is entitled to manage.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_manageable_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_my_channels', 'R', 'List all software channels that belong to the user''s organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_my_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_popular_channels', 'R', 'List the most popular software channels.  Channels that have at least'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_popular_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_retired_channels', 'R', 'List all retired software channels.  These are channels that the user''s'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_retired_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_shared_channels', 'R', 'List all software channels that may be shared by the user''s'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_shared_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_software_channels', 'R', 'List all visible software channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_software_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.list_vendor_channels', 'R', 'Lists all the vendor software channels that the user''s organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.list_vendor_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.org.disable_access', 'W', 'Disable access to the channel for the given organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.org.disable_access' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.org.enable_access', 'W', 'Enable access to the channel for the given organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.org.enable_access' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.org.list', 'W', 'List the organizations associated with the given channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.org.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.add_packages', 'W', 'Adds a given list of packages to the given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.add_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.add_repo_filter', 'W', 'Adds a filter for a given repo.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.add_repo_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.align_metadata', 'W', 'Align the metadata of a channel to another channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.align_metadata' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.apply_channel_state', 'W', 'Refresh pillar data and then schedule channels state on the provided systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.apply_channel_state' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.associate_repo', 'W', 'Associates a repository with a channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.associate_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.clear_repo_filters', 'W', 'Removes the filters for a repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.clear_repo_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.clone', 'W', 'Clone a channel.  If arch_label is omitted, the arch label of the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.clone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.create', 'W', 'Creates a software channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.create_repo', 'W', 'Creates a repository'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.create_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.delete', 'W', 'Deletes a custom software channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.disassociate_repo', 'W', 'Disassociates a repository from a channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.disassociate_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.get_channel_last_build_by_id', 'R', 'Returns the last build date of the repomd.xml file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.get_channel_last_build_by_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.get_details', 'R', 'Returns details of the given channel as a map'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.get_repo_details', 'R', 'Returns details of the given repository'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.get_repo_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.get_repo_sync_cron_expression', 'R', 'Returns repo synchronization cron expression'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.get_repo_sync_cron_expression' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.is_existing', 'R', 'Returns whether is existing'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.is_existing' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.is_globally_subscribable', 'R', 'Returns whether the channel is subscribable by any user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.is_globally_subscribable' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.is_user_manageable', 'R', 'Returns whether the channel may be managed by the given user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.is_user_manageable' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.is_user_subscribable', 'R', 'Returns whether the channel may be subscribed to by the given user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.is_user_subscribable' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_all_packages', 'R', 'Lists all packages in the channel, regardless of package version,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_all_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_arches', 'R', 'Lists the potential software channel architectures that can be created'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_arches' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_channel_repos', 'R', 'Lists associated repos with the given channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_channel_repos' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_children', 'R', 'List the children of a channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_children' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_errata', 'R', 'List the errata applicable to a channel after given startDate'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_errata_by_type', 'R', 'List the errata of a specific type that are applicable to a channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_errata_by_type' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_errata_needing_sync', 'R', 'If you have synced a new channel then patches'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_errata_needing_sync' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_latest_packages', 'R', 'Lists the packages with the latest version (including release and'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_latest_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_packages_without_channel', 'R', 'Lists all packages that are not associated with a channel.  Typically'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_packages_without_channel' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_repo_filters', 'R', 'Lists the filters for a repo'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_repo_filters' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_subscribed_systems', 'R', 'Returns list of subscribed systems for the given channel label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_subscribed_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_system_channels', 'R', 'Returns a list of channels that a system is subscribed to for the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_system_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.list_user_repos', 'R', 'Returns a list of ContentSource (repos) that the user can see'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.list_user_repos' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.merge_errata', 'W', 'Merges all errata from one channel into another'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.merge_errata' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.merge_packages', 'W', 'Merges all packages from one channel into another'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.merge_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.regenerate_needed_cache', 'W', 'Completely clear and regenerate the needed Errata and Package'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.regenerate_needed_cache' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.regenerate_yum_cache', 'W', 'Regenerate yum cache for the specified channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.regenerate_yum_cache' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.remove_errata', 'W', 'Removes a given list of errata from the given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.remove_errata' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.remove_packages', 'W', 'Removes a given list of packages from the given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.remove_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.remove_repo', 'W', 'Removes a repository'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.remove_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.remove_repo_filter', 'W', 'Removes a filter for a given repo.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.remove_repo_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_contact_details', 'W', 'Set contact/support information for given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_contact_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_details', 'W', 'Allows to modify channel attributes'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_globally_subscribable', 'W', 'Set globally subscribable attribute for given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_globally_subscribable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_repo_filters', 'W', 'Replaces the existing set of filters for a given repo.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_repo_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_user_manageable', 'W', 'Set the manageable flag for a given channel and user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_user_manageable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.set_user_subscribable', 'W', 'Set the subscribable flag for a given channel and user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.set_user_subscribable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.sync_errata', 'W', 'If you have synced a new channel then patches'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.sync_errata' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.sync_repo', 'W', 'Trigger immediate repo synchronization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.sync_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.update_repo', 'W', 'Updates a ContentSource (repo)'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.update_repo' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.update_repo_label', 'W', 'Updates repository label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.update_repo_label' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.update_repo_ssl', 'W', 'Updates repository SSL certificates'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.update_repo_ssl' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.channel.software.update_repo_url', 'W', 'Updates repository source URL'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.channel.software.update_repo_url' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.channel_exists', 'W', 'Check for the existence of the config channel provided.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.channel_exists' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.create', 'W', 'Create a new global config channel. Caller must be at least a'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.create_or_update_path', 'W', 'Create a new file or directory with the given path, or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.create_or_update_path' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.create_or_update_symlink', 'W', 'Create a new symbolic link with the given path, or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.create_or_update_symlink' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.delete_channels', 'W', 'Delete a list of global config channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.delete_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.delete_file_revisions', 'W', 'Delete specified revisions of a given configuration file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.delete_file_revisions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.delete_files', 'W', 'Remove file paths from a global channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.delete_files' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.deploy_all_systems', 'W', 'Schedule an immediate configuration deployment for all systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.deploy_all_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.get_details', 'R', 'Lookup config channel details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.get_encoded_file_revision', 'R', 'Get revision of the specified configuration file and transmit the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.get_encoded_file_revision' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.get_file_revision', 'R', 'Get revision of the specified config file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.get_file_revision' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.get_file_revisions', 'R', 'Get list of revisions for specified config file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.get_file_revisions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.list_assigned_system_groups', 'R', 'Return a list of Groups where a given configuration channel is assigned to'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.list_assigned_system_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.list_files', 'R', 'Return a list of files in a channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.list_files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.list_globals', 'R', 'List all the global config channels accessible to the logged-in user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.list_globals' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.list_subscribed_systems', 'R', 'Return a list of systems subscribed to a configuration channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.list_subscribed_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.lookup_channel_info', 'R', 'Lists details on a list of channels given their channel labels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.lookup_channel_info' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.lookup_file_info', 'R', 'Given a list of paths and a channel, returns details about'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.lookup_file_info' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.schedule_file_comparisons', 'W', 'Schedule a comparison of the latest revision of a file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.schedule_file_comparisons' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.sync_salt_files_on_disk', 'W', 'Synchronize all files on the disk to the current state of the database.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.sync_salt_files_on_disk' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.update', 'W', 'Update a global config channel. Caller must be at least a'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.configchannel.update_init_sls', 'W', 'Update the init.sls file for the given state channel. User can only update contents, nothing else.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.configchannel.update_init_sls' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.attach_filter', 'W', 'Attach a Filter to a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.attach_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.attach_source', 'W', 'Attach a Source to a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.attach_source' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.build_project', 'W', 'Build a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.build_project' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.create_app_stream_filters', 'W', 'Create Filters for AppStream Modular Channel and attach them to CLM Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.create_app_stream_filters' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.create_environment', 'W', 'Create a Content Environment and appends it behind given Content Environment'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.create_environment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.create_filter', 'W', 'Create a Content Filter'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.create_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.create_project', 'W', 'Create Content Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.create_project' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.detach_filter', 'W', 'Detach a Filter from a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.detach_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.detach_source', 'W', 'Detach a Source from a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.detach_source' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_filter_criteria', 'R', 'List of available filter criteria'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_filter_criteria' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_filters', 'R', 'List all Content Filters visible to given user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_filters' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_project_environments', 'R', 'List Environments in a Content Project with the respect to their ordering'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_project_environments' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_project_filters', 'R', 'List all Filters associated with a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_project_filters' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_project_sources', 'R', 'List Content Project Sources'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_project_sources' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.list_projects', 'R', 'List Content Projects visible to user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.list_projects' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.lookup_environment', 'R', 'Look up Content Environment based on Content Project and Content Environment label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.lookup_environment' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.lookup_filter', 'R', 'Lookup a Content Filter by ID'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.lookup_filter' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.lookup_project', 'R', 'Look up Content Project with given label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.lookup_project' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.lookup_source', 'R', 'Look up Content Project Source'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.lookup_source' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.promote_project', 'W', 'Promote an Environment in a Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.promote_project' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.remove_environment', 'W', 'Remove a Content Environment'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.remove_environment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.remove_filter', 'W', 'Remove a Content Filter'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.remove_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.remove_project', 'W', 'Remove Content Project'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.remove_project' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.update_environment', 'W', 'Update Content Environment with given label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.update_environment' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.update_filter', 'W', 'Update a Content Filter'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.update_filter' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.contentmanagement.update_project', 'W', 'Update Content Project with given label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.contentmanagement.update_project' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.add_channel', 'W', 'Add a new channel to the #product() database'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.add_channel' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.add_channels', 'W', 'Add a new channel to the #product() database'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.add_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.add_credentials', 'W', 'Add organization credentials (mirror credentials) to #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.add_credentials' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.delete_credentials', 'W', 'Delete organization credentials (mirror credentials) from #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.delete_credentials' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.list_channels', 'R', 'List all accessible channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.list_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.list_credentials', 'R', 'List organization credentials (mirror credentials) available in'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.list_credentials' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.list_products', 'R', 'List all accessible products.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.list_products' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.synchronize_channel_families', 'W', 'Synchronize channel families between the Customer Center'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.synchronize_channel_families' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.synchronize_products', 'W', 'Synchronize SUSE products between the Customer Center'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.synchronize_products' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.synchronize_repositories', 'W', 'Synchronize repositories between the Customer Center'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.synchronize_repositories' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.content.synchronize_subscriptions', 'W', 'Synchronize subscriptions between the Customer Center'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.content.synchronize_subscriptions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.keys.create', 'W', 'creates a new key with the given parameters'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.keys.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.keys.delete', 'W', 'deletes the key identified by the given parameters'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.keys.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.keys.get_details', 'R', 'returns all the data associated with the given key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.keys.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.keys.list_all_keys', 'R', 'list all keys for the org associated with the user logged into the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.keys.list_all_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.keys.update', 'W', 'Updates type and content of the key identified by the description'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.keys.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.custominfo.create_key', 'W', 'Create a new custom key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.custominfo.create_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.custominfo.delete_key', 'W', 'Delete an existing custom key and all systems'' values for the key.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.custominfo.delete_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.custominfo.list_all_keys', 'R', 'List the custom information keys defined for the user''s organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.custominfo.list_all_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.custominfo.update_key', 'W', 'Update description of a custom key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.custominfo.update_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.audit.list_images_by_patch_status', 'R', 'List visible images with their patch status regarding a given CVE'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.audit.list_images_by_patch_status' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.audit.list_systems_by_patch_status', 'R', 'List visible systems with their patch status regarding a given CVE'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.audit.list_systems_by_patch_status' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.delta.create_delta_image', 'W', 'Import an image and schedule an inspect afterwards. The "size" entries in the pillar'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.delta.create_delta_image' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.delta.get_details', 'R', 'Get details of an Image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.delta.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.delta.list_deltas', 'R', 'List available DeltaImages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.delta.list_deltas' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.distchannel.list_default_maps', 'R', 'Lists the default distribution channel maps'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.distchannel.list_default_maps' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.distchannel.list_maps_for_org', 'R', 'Lists distribution channel maps valid for the user''s organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.distchannel.list_maps_for_org' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.distchannel.set_map_for_org', 'W', 'Sets, overrides (/removes if channelLabel empty)'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.distchannel.set_map_for_org' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.add_packages', 'W', 'Add a set of packages to an erratum with the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.add_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.applicable_to_channels', 'R', 'Returns a list of channels applicable to the errata with the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.applicable_to_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.bugzilla_fixes', 'W', 'Get the Bugzilla fixes for an erratum matching the given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.bugzilla_fixes' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.clone', 'W', 'Clone a list of errata into the specified channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.clone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.clone_as_original', 'W', 'Clones a list of errata into a specified cloned channel according the original erratas.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.clone_as_original' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.clone_as_original_async', 'W', 'Asynchronously clones a list of errata into a specified cloned channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.clone_as_original_async' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.clone_async', 'W', 'Asynchronously clone a list of errata into the specified channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.clone_async' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.create', 'W', 'Create a custom errata'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.delete', 'W', 'Delete an erratum.  This method will only allow for deletion'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.find_by_cve', 'R', 'Lookup the details for errata associated with the given CVE'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.find_by_cve' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.get_details', 'R', 'Retrieves the details for the erratum matching the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.list_affected_systems', 'R', 'Return the list of systems affected by the errata with the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.list_affected_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.list_cves', 'R', 'Returns a list of http://cve.mitre.org/_blankCVEs applicable to the errata'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.list_cves' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.list_keywords', 'R', 'Get the keywords associated with an erratum matching the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.list_keywords' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.list_packages', 'R', 'Returns a list of the packages affected by the errata with the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.list_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.publish', 'W', 'Adds an existing errata to a set of channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.publish' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.publish_as_original', 'W', 'Adds an existing cloned errata to a set of cloned'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.publish_as_original' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.remove_packages', 'W', 'Remove a set of packages from an erratum with the given advisory name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.remove_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.errata.set_details', 'W', 'Set erratum details. All arguments are optional and will only be modified'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.errata.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.filepreservation.create', 'W', 'Create a new file preservation list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.filepreservation.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.filepreservation.delete', 'W', 'Delete a file preservation list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.filepreservation.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.filepreservation.get_details', 'R', 'Returns all the data associated with the given file preservation list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.filepreservation.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.filepreservation.list_all_file_preservations', 'R', 'List all file preservation lists for the organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.filepreservation.list_all_file_preservations' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_combined_formula_data_by_server_ids', 'R', 'Return the list of formulas a server and all his groups have.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_combined_formula_data_by_server_ids' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_combined_formulas_by_server_id', 'R', 'Return the list of formulas a server and all his groups have.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_combined_formulas_by_server_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_formulas_by_group_id', 'R', 'Return the list of formulas a server group has.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_formulas_by_group_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_formulas_by_server_id', 'R', 'Return the list of formulas directly applied to a server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_formulas_by_server_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_group_formula_data', 'R', 'Get the saved data for the specific formula against specific group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_group_formula_data' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.get_system_formula_data', 'R', 'Get the saved data for the specific formula against specific server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.get_system_formula_data' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.list_formulas', 'R', 'Return the list of formulas currently installed.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.list_formulas' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.set_formulas_of_group', 'W', 'Set the formulas of a server group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.set_formulas_of_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.set_formulas_of_server', 'W', 'Set the formulas of a server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.set_formulas_of_server' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.set_group_formula_data', 'W', 'Set the formula form for the specified group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.set_group_formula_data' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.formula.set_system_formula_data', 'W', 'Set the formula form for the specified server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.formula.set_system_formula_data' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.deregister', 'W', 'De-register the server locally identified by the fqdn.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.deregister' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.generate_access_token', 'W', 'Generate a new access token for ISS for accessing this system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.generate_access_token' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.register_peripheral', 'W', 'Registers automatically a remote server with the specified ISS role.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.register_peripheral' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.register_peripheral_with_token', 'W', 'Registers a remote server with the specified ISS role using an existing specified access token.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.register_peripheral_with_token' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.replace_tokens', 'W', 'Replace the auth tokens for connections between this hub and the given peripheral server'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.replace_tokens' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.set_details', 'W', 'Set server details. All arguments are optional and will only be modified'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.hub.store_access_token', 'W', 'Generate a new access token for ISS for accessing this system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.hub.store_access_token' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.add_image_file', 'W', 'Delete image file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.add_image_file' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.delete', 'W', 'Delete an image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.delete_image_file', 'W', 'Delete image file'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.delete_image_file' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.get_custom_values', 'R', 'Get the custom data values defined for the image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.get_custom_values' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.get_details', 'R', 'Get details of an image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.get_pillar', 'R', 'Get pillar data of an image. The "size" entries are converted to string.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.get_pillar' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.get_relevant_errata', 'R', 'Returns a list of all errata that are relevant for the image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.get_relevant_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.import_container_image', 'W', 'Import an image and schedule an inspect afterwards'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.import_container_image' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.import_image', 'W', 'Import an image and schedule an inspect afterwards'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.import_image' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.import_o_s_image', 'W', 'Import an image and schedule an inspect afterwards'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.import_o_s_image' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.list_images', 'R', 'List available images'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.list_images' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.list_packages', 'R', 'List the installed packages on the given image'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.list_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.schedule_image_build', 'W', 'Schedule an image build'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.schedule_image_build' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.set_pillar', 'W', 'Set pillar data of an image. The "size" entries should be passed as string.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.set_pillar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.create', 'W', 'Create a new image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.delete', 'W', 'Delete an image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.delete_custom_values', 'W', 'Delete the custom values defined for the specified image profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.delete_custom_values' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.get_custom_values', 'R', 'Get the custom data values defined for the image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.get_custom_values' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.get_details', 'R', 'Get details of an image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.list_image_profile_types', 'R', 'List available image store types'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.list_image_profile_types' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.list_image_profiles', 'R', 'List available image profiles'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.list_image_profiles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.set_custom_values', 'W', 'Set custom values for the specified image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.set_custom_values' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.profile.set_details', 'W', 'Set details of an image profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.profile.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.create', 'W', 'Create a new image store'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.delete', 'W', 'Delete an image store'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.get_details', 'R', 'Get details of an image store'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.list_image_store_types', 'R', 'List available image store types'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.list_image_store_types' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.list_image_stores', 'R', 'List available image stores'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.list_image_stores' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.image.store.set_details', 'W', 'Set details of an image store'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.image.store.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.keys.add_activation_key', 'W', 'Add an activation key association to the kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.keys.add_activation_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.keys.get_activation_keys', 'R', 'Lookup the activation keys associated with the kickstart'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.keys.get_activation_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.keys.remove_activation_key', 'W', 'Remove an activation key association from the kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.keys.remove_activation_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.clone_profile', 'W', 'Clone a Kickstart Profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.clone_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.create_profile', 'W', 'Create a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.create_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.create_profile_with_custom_url', 'W', 'Create a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.create_profile_with_custom_url' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.delete_profile', 'W', 'Delete a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.delete_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.disable_profile', 'W', 'Enable/Disable a Kickstart Profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.disable_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.find_kickstart_for_ip', 'R', 'Find an associated kickstart for a given ip address.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.find_kickstart_for_ip' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.import_file', 'W', 'Import a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.import_file' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.import_raw_file', 'W', 'Import a raw kickstart file into #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.import_raw_file' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.is_profile_disabled', 'R', 'Returns whether a kickstart profile is disabled'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.is_profile_disabled' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.list_all_ip_ranges', 'R', 'List all Ip Ranges and their associated kickstarts available'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.list_all_ip_ranges' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.list_autoinstallable_channels', 'R', 'List autoinstallable channels for the logged in user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.list_autoinstallable_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.list_kickstartable_channels', 'R', 'List kickstartable channels for the logged in user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.list_kickstartable_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.list_kickstarts', 'R', 'Provides a list of kickstart profiles visible to the user''s'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.list_kickstarts' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.rename_profile', 'W', 'Rename a kickstart profile in #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.rename_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.create', 'W', 'Create a Kickstart Tree (Distribution) in #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.delete', 'W', 'Delete a Kickstart Tree (Distribution) from #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.delete_tree_and_profiles', 'W', 'Delete a kickstarttree and any profiles associated with'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.delete_tree_and_profiles' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.get_details', 'R', 'The detailed information about a kickstartable tree given the tree name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.list', 'W', 'List the available kickstartable trees for the given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.list_install_types', 'R', 'List the available kickstartable install types (rhel2,3,4,5 and'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.list_install_types' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.rename', 'W', 'Rename a Kickstart Tree (Distribution) in #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.rename' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.tree.update', 'W', 'Edit a Kickstart Tree (Distribution) in #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.tree.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.assign_schedule_to_systems', 'W', 'Assign schedule with given name to systems with given IDs.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.assign_schedule_to_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.create_calendar', 'W', 'Create a new maintenance calendar'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.create_calendar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.create_calendar_with_url', 'W', 'Create a new maintenance calendar'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.create_calendar_with_url' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.create_schedule', 'W', 'Create a new maintenance Schedule'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.create_schedule' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.delete_calendar', 'W', 'Remove a maintenance calendar'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.delete_calendar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.delete_schedule', 'W', 'Remove a maintenance schedule'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.delete_schedule' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.get_calendar_details', 'W', 'Lookup a specific maintenance schedule'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.get_calendar_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.get_schedule_details', 'W', 'Lookup a specific maintenance schedule'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.get_schedule_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.list_calendar_labels', 'W', 'List schedule names visible to user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.list_calendar_labels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.list_schedule_names', 'W', 'List Schedule Names visible to user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.list_schedule_names' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.list_systems_with_schedule', 'W', 'List IDs of systems that have given schedule assigned'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.list_systems_with_schedule' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.refresh_calendar', 'W', 'Refresh maintenance calendar data using the configured URL'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.refresh_calendar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.retract_schedule_from_systems', 'W', 'Retract schedule with given name from systems with given IDs'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.retract_schedule_from_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.update_calendar', 'W', 'Update a maintenance calendar'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.update_calendar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.maintenance.update_schedule', 'W', 'Update a maintenance schedule'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.maintenance.update_schedule' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.add_to_master', 'W', 'Add a single organizations to the list of those the specified Master has'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.add_to_master' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.create', 'W', 'Create a new Master, known to this Slave.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.delete', 'W', 'Remove the specified Master'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.get_default_master', 'R', 'Return the current default-Master for this Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.get_default_master' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.get_master', 'R', 'Find a Master by specifying its ID'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.get_master' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.get_master_by_label', 'R', 'Find a Master by specifying its label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.get_master_by_label' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.get_master_orgs', 'R', 'List all organizations the specified Master has exported to this Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.get_master_orgs' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.get_masters', 'R', 'Get all the Masters this Slave knows about'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.get_masters' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.make_default', 'W', 'Make the specified Master the default for this Slave''s inter-server-sync'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.make_default' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.map_to_local', 'W', 'Add a single organizations to the list of those the specified Master has'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.map_to_local' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.set_ca_cert', 'W', 'Set the CA-CERT filename for specified Master on this Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.set_ca_cert' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.set_master_orgs', 'W', 'Reset all organizations the specified Master has exported to this Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.set_master_orgs' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.unset_default_master', 'W', 'Make this slave have no default Master for inter-server-sync'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.unset_default_master' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.master.update', 'W', 'Updates the label of the specified Master'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.master.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.create', 'W', 'Create a new organization and associated administrator account.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.create_first', 'W', 'Create first organization and user after initial setup without authentication'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.create_first' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.delete', 'W', 'Delete an organization. The default organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.get_clm_sync_patches_config', 'R', 'Reads the content lifecycle management patch synchronization config option.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.get_clm_sync_patches_config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.get_details', 'R', 'The detailed information about an organization given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.get_policy_for_scap_file_upload', 'R', 'Get the status of SCAP detailed result file upload settings'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.get_policy_for_scap_file_upload' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.get_policy_for_scap_result_deletion', 'R', 'Get the status of SCAP result deletion settings for the given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.get_policy_for_scap_result_deletion' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.is_content_staging_enabled', 'R', 'Get the status of content staging settings for the given organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.is_content_staging_enabled' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.is_errata_email_notifs_for_org', 'R', 'Returns whether errata e-mail notifications are enabled'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.is_errata_email_notifs_for_org' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.is_org_config_managed_by_org_admin', 'R', 'Returns whether Organization Administrator is able to manage his'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.is_org_config_managed_by_org_admin' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.list_orgs', 'R', 'Returns the list of organizations.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.list_orgs' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.list_users', 'R', 'Returns the list of users in a given organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.list_users' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.migrate_systems', 'W', 'Transfer systems from one organization to another.  If executed by'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.migrate_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_clm_sync_patches_config', 'W', 'Sets the content lifecycle management patch synchronization config option.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_clm_sync_patches_config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_content_staging', 'W', 'Set the status of content staging for the given organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_content_staging' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_errata_email_notifs_for_org', 'W', 'Dis/enables errata e-mail notifications for the organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_errata_email_notifs_for_org' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_org_config_managed_by_org_admin', 'W', 'Sets whether Organization Administrator can manage his organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_org_config_managed_by_org_admin' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_policy_for_scap_file_upload', 'W', 'Set the status of SCAP detailed result file upload settings'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_policy_for_scap_file_upload' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.set_policy_for_scap_result_deletion', 'W', 'Set the status of SCAP result deletion settins for the given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.set_policy_for_scap_result_deletion' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.transfer_systems', 'W', 'Transfer systems from one organization to another.  If executed by'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.transfer_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.update_name', 'W', 'Updates the name of an organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.update_name' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.add_trust', 'W', 'Add an organization to the list of trusted organizations.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.add_trust' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.get_details', 'R', 'The trust details about an organization given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.list_channels_consumed', 'R', 'Lists all software channels that the organization given may consume'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.list_channels_consumed' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.list_channels_provided', 'R', 'Lists all software channels that the organization given is providing to'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.list_channels_provided' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.list_orgs', 'R', 'List all organanizations trusted by the user''s organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.list_orgs' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.list_systems_affected', 'R', 'Get a list of systems within the  trusted organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.list_systems_affected' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.list_trusts', 'R', 'Returns the list of trusted organizations.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.list_trusts' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.org.trusts.remove_trust', 'W', 'Remove an organization to the list of trusted organizations.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.org.trusts.remove_trust' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.find_by_nvrea', 'R', 'Lookup the details for packages with the given name, version,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.find_by_nvrea' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.get_details', 'R', 'Retrieve details for the package with the ID.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.get_package', 'R', 'Retrieve the package file associated with a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.get_package' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.get_package_url', 'R', 'Retrieve the url that can be used to download a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.get_package_url' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_changelog', 'R', 'List the change log for a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_changelog' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_dependencies', 'R', 'List the dependencies for a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_dependencies' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_files', 'R', 'List the files associated with a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_providing_channels', 'R', 'List the channels that provide the a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_providing_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_providing_errata', 'R', 'List the errata providing the a package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_providing_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.list_source_packages', 'R', 'List all source packages in user''s organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.list_source_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.remove_package', 'W', 'Remove a package from #product().'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.remove_package' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.remove_source_package', 'W', 'Remove a source package.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.remove_source_package' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.provider.associate_key', 'W', 'Associate a package security key and with the package provider.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.provider.associate_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.provider.list', 'W', 'List all Package Providers.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.provider.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.provider.list_keys', 'R', 'List all security keys associated with a package provider.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.provider.list_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.advanced', 'R', 'Advanced method to search lucene indexes with a passed in query written'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.advanced' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.advanced_with_act_key', 'R', 'Advanced method to search lucene indexes with a passed in query written'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.advanced_with_act_key' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.advanced_with_channel', 'R', 'Advanced method to search lucene indexes with a passed in query written'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.advanced_with_channel' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.name', 'R', 'Search the lucene package indexes for all packages which'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.name' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.name_and_description', 'R', 'Search the lucene package indexes for all packages which'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.name_and_description' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.packages.search.name_and_summary', 'R', 'Search the lucene package indexes for all packages which'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.packages.search.name_and_summary' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.subscriptionmatching.pinnedsubscription.create', 'W', 'Creates a Pinned Subscription based on given subscription and system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.subscriptionmatching.pinnedsubscription.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.subscriptionmatching.pinnedsubscription.delete', 'W', 'Deletes Pinned Subscription with given id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.subscriptionmatching.pinnedsubscription.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.subscriptionmatching.pinnedsubscription.list', 'W', 'Lists all PinnedSubscriptions'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.subscriptionmatching.pinnedsubscription.list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.get_details', 'R', 'Get current power management settings of the given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.get_status', 'R', 'Execute powermanagement actions'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.get_status' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.list_types', 'R', 'Return a list of available power management types'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.list_types' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.power_off', 'W', 'Execute power management action ''powerOff'''
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.power_off' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.power_on', 'W', 'Execute power management action ''powerOn'''
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.power_on' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.reboot', 'W', 'Execute power management action ''Reboot'''
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.reboot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.powermanagement.set_details', 'W', 'Get current power management settings of the given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.powermanagement.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.preferences.locale.list_locales', 'R', 'Returns a list of all understood locales. Can be'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.preferences.locale.list_locales' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.preferences.locale.list_time_zones', 'R', 'Returns a list of all understood timezones. Results can be'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.preferences.locale.list_time_zones' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.preferences.locale.set_locale', 'W', 'Set a user''s locale.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.preferences.locale.set_locale' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.preferences.locale.set_time_zone', 'W', 'Set a user''s timezone.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.preferences.locale.set_time_zone' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.add_ip_range', 'W', 'Add an ip range to a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.add_ip_range' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.add_script', 'W', 'Add a pre/post script to a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.add_script' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.compare_activation_keys', 'W', 'Returns a list for each kickstart profile; each list will contain'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.compare_activation_keys' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.compare_advanced_options', 'W', 'Returns a list for each kickstart profile; each list will contain the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.compare_advanced_options' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.compare_packages', 'W', 'Returns a list for each kickstart profile; each list will contain'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.compare_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.download_kickstart', 'W', 'Download the full contents of a kickstart file.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.download_kickstart' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.download_rendered_kickstart', 'W', 'Downloads the Cobbler-rendered Kickstart file.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.download_rendered_kickstart' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_advanced_options', 'R', 'Get advanced options for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_advanced_options' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_available_repositories', 'R', 'Lists available OS repositories to associate with the provided'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_available_repositories' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_cfg_preservation', 'R', 'Get ks.cfg preservation option for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_cfg_preservation' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_child_channels', 'R', 'Get the child channels for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_child_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_custom_options', 'R', 'Get custom options for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_custom_options' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_kickstart_tree', 'R', 'Get the kickstart tree for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_kickstart_tree' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_repositories', 'R', 'Lists all OS repositories associated with provided kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_repositories' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_update_type', 'R', 'Get the update type for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_update_type' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_variables', 'R', 'Returns a list of variables'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_variables' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.get_virtualization_type', 'R', 'For given kickstart profile label returns label of'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.get_virtualization_type' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.list_ip_ranges', 'R', 'List all ip ranges for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.list_ip_ranges' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.list_scripts', 'R', 'List the pre and post scripts for a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.list_scripts' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.order_scripts', 'W', 'Change the order that kickstart scripts will run for'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.order_scripts' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.remove_ip_range', 'W', 'Remove an ip range from a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.remove_ip_range' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.remove_script', 'W', 'Remove a script from a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.remove_script' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_advanced_options', 'W', 'Set advanced options for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_advanced_options' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_cfg_preservation', 'W', 'Set ks.cfg preservation option for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_cfg_preservation' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_child_channels', 'W', 'Set the child channels for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_child_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_custom_options', 'W', 'Set custom options for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_custom_options' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_kickstart_tree', 'W', 'Set the kickstart tree for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_kickstart_tree' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_logging', 'W', 'Set logging options for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_logging' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_repositories', 'W', 'Associates OS repository to a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_repositories' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_update_type', 'W', 'Set the update typefor a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_update_type' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_variables', 'W', 'Associates list of kickstart variables'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_variables' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.set_virtualization_type', 'W', 'For given kickstart profile label sets its virtualization type.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.set_virtualization_type' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.activate_proxy', 'W', 'Activates the proxy identified by the given client'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.activate_proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.container_config', 'W', 'Compute and download the configuration for proxy containers'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.container_config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.create_monitoring_scout', 'W', 'Create Monitoring Scout for proxy.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.create_monitoring_scout' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.deactivate_proxy', 'W', 'Deactivates the proxy identified by the given client'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.deactivate_proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.is_proxy', 'R', 'Test, if the system identified by the given client'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.is_proxy' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.list_available_proxy_channels', 'R', 'List available version of proxy channel for system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.list_available_proxy_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.list_proxies', 'R', 'List the proxies within the user''s organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.list_proxies' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.proxy.list_proxy_clients', 'R', 'List the clients directly connected to a given Proxy.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.proxy.list_proxy_clients' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.delete', 'W', 'Delete a recurring action with the given action ID.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.list_by_entity', 'R', 'Return a list of recurring actions for a given entity.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.list_by_entity' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.lookup_by_id', 'R', 'Find a recurring action with the given action ID.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.lookup_by_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.custom.create', 'W', 'Create a new recurring custom state action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.custom.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.custom.list_available', 'R', 'List all the custom states available to the user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.custom.list_available' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.custom.update', 'W', 'Update a recurring custom state action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.custom.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.highstate.create', 'W', 'Create a new recurring highstate action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.highstate.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.recurring.highstate.update', 'W', 'Update the properties of a recurring highstate action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.recurring.highstate.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.accept', 'W', 'Accept a minion key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.accept' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.accepted_list', 'R', 'List accepted salt keys'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.accepted_list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.delete', 'W', 'Delete a minion key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.denied_list', 'R', 'List of denied salt keys'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.denied_list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.pending_list', 'R', 'List pending salt keys'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.pending_list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.reject', 'W', 'Reject a minion key'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.reject' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.saltkey.rejected_list', 'R', 'List of rejected salt keys'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.saltkey.rejected_list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.archive_actions', 'W', 'Archive all actions in the given list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.archive_actions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.cancel_actions', 'W', 'Cancel all actions in given list. If an invalid action is provided,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.cancel_actions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.delete_actions', 'W', 'Delete all archived actions in the given list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.delete_actions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.fail_system_action', 'W', 'Fail specific event on specified system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.fail_system_action' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_all_actions', 'R', 'Returns a list of all actions.  This includes completed, in progress,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_all_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_all_archived_actions', 'R', 'Returns a list of actions that have been archived.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_all_archived_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_all_completed_actions', 'R', 'Returns a list of actions that have been completed.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_all_completed_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_archived_actions', 'R', 'Returns a list of actions that have been archived.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_archived_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_completed_actions', 'R', 'Returns a list of actions that have completed successfully.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_completed_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_completed_systems', 'R', 'Returns a list of systems that have completed a specific action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_completed_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_failed_actions', 'R', 'Returns a list of actions that have failed.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_failed_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_failed_systems', 'R', 'Returns a list of systems that have failed a specific action.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_failed_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_in_progress_actions', 'R', 'Returns a list of actions that are in progress.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_in_progress_actions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.list_in_progress_systems', 'R', 'Returns a list of systems that have a specific action in progress.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.list_in_progress_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.schedule.reschedule_actions', 'W', 'Reschedule all actions in the given list.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.schedule.reschedule_actions' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.add_channels', 'W', 'Given a list of servers and configuration channels,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.add_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.create_or_update_path', 'W', 'Create a new file (text or binary) or directory with the given path, or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.create_or_update_path' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.create_or_update_symlink', 'W', 'Create a new symbolic link with the given path, or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.create_or_update_symlink' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.delete_files', 'W', 'Removes file paths from a local or sandbox channel of a server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.delete_files' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.deploy_all', 'W', 'Schedules a deploy action for all the configuration files'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.deploy_all' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.list_channels', 'R', 'List all global(''Normal'', ''State'') configuration channels associated to a'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.list_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.list_files', 'R', 'Return the list of files in a given channel.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.list_files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.lookup_file_info', 'R', 'Given a list of paths and a server, returns details about'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.lookup_file_info' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.remove_channels', 'W', 'Remove config channels from the given servers.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.remove_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.schedule_apply_config_channel', 'W', 'Schedule highstate application for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.schedule_apply_config_channel' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.config.set_channels', 'W', 'Replace the existing set of config channels on the given servers.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.config.set_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.add_or_remove_admins', 'W', 'Add or remove administrators to/from the given group. #product() and'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.add_or_remove_admins' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.add_or_remove_systems', 'W', 'Add/remove the given servers to a system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.add_or_remove_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.create', 'W', 'Create a new system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.delete', 'W', 'Delete a system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.get_details', 'R', 'Retrieve details of a ServerGroup based on it''s id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_active_systems_in_group', 'R', 'Lists active systems within a server group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_active_systems_in_group' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_administrators', 'R', 'Returns the list of users who can administer the given group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_administrators' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_all_groups', 'R', 'Retrieve a list of system groups that are accessible by the logged'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_all_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_assigned_config_channels', 'R', 'List all Configuration Channels assigned to a system group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_assigned_config_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_assigned_formuals', 'R', 'List all Configuration Channels assigned to a system group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_assigned_formuals' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_groups_with_no_associated_admins', 'R', 'Returns a list of system groups that do not have an administrator.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_groups_with_no_associated_admins' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_inactive_systems_in_group', 'R', 'Lists inactive systems within a server group using a'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_inactive_systems_in_group' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_systems', 'R', 'Return a list of systems associated with this system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.list_systems_minimal', 'R', 'Return a list of systems associated with this system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.list_systems_minimal' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.schedule_apply_errata_to_active', 'W', 'Schedules an action to apply errata updates to active systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.schedule_apply_errata_to_active' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.subscribe_config_channel', 'W', 'Subscribe given config channels to a system group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.subscribe_config_channel' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.unsubscribe_config_channel', 'W', 'Unsubscribe given config channels to a system group'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.unsubscribe_config_channel' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.systemgroup.update', 'W', 'Update an existing system group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.systemgroup.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.create', 'W', 'Create a new Slave, known to this Master.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.delete', 'W', 'Remove the specified Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.get_allowed_orgs', 'R', 'Get all orgs this Master is willing to export to the specified Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.get_allowed_orgs' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.get_slave', 'R', 'Find a Slave by specifying its ID'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.get_slave' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.get_slave_by_name', 'R', 'Find a Slave by specifying its Fully-Qualified Domain Name'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.get_slave_by_name' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.get_slaves', 'R', 'Get all the Slaves this Master knows about'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.get_slaves' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.set_allowed_orgs', 'W', 'Set the orgs this Master is willing to export to the specified Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.set_allowed_orgs' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.sync.slave.update', 'W', 'Updates attributes of the specified Slave'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.sync.slave.update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.add_tag_to_snapshot', 'W', 'Adds tag to snapshot'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.add_tag_to_snapshot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.delete_snapshot', 'W', 'Deletes a snapshot with the given snapshot id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.delete_snapshot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.delete_snapshots', 'W', 'Deletes all snapshots across multiple systems based on the given date'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.delete_snapshots' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.list_snapshot_config_files', 'R', 'List the config files associated with a snapshot.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.list_snapshot_config_files' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.list_snapshot_packages', 'R', 'List the packages associated with a snapshot.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.list_snapshot_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.list_snapshots', 'R', 'List snapshots for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.list_snapshots' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.rollback_to_snapshot', 'W', 'Rollbacks server to snapshot'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.rollback_to_snapshot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provisioning.snapshot.rollback_to_tag', 'W', 'Rollbacks server to snapshot'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provisioning.snapshot.rollback_to_tag' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.snippet.create_or_update', 'W', 'Will create a snippet with the given name and contents if it'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.snippet.create_or_update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.snippet.delete', 'W', 'Delete the specified snippet.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.snippet.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.snippet.list_all', 'R', 'List all cobbler snippets for the logged in user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.snippet.list_all' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.snippet.list_custom', 'R', 'List only custom snippets for the logged in user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.snippet.list_custom' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.snippet.list_default', 'R', 'List only pre-made default snippets for the logged in user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.snippet.list_default' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.software.append_to_software_list', 'W', 'Append the list of software packages to a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.software.append_to_software_list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.software.get_software_details', 'R', 'Gets kickstart profile software details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.software.get_software_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.software.get_software_list', 'R', 'Get a list of a kickstart profile''s software packages.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.software.get_software_list' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.software.set_software_details', 'W', 'Sets kickstart profile software details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.software.set_software_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.software.set_software_list', 'W', 'Set the list of software packages for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.software.set_software_list' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.appstreams.disable', 'W', 'Schedule disabling of module streams. Invalid modules will be filtered out. If all provided'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.appstreams.disable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.appstreams.enable', 'W', 'Schedule enabling of module streams. Invalid modules will be filtered out. If all provided'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.appstreams.enable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.appstreams.list_module_streams', 'R', 'List available module streams for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.appstreams.list_module_streams' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.add_file_preservations', 'W', 'Adds the given list of file preservations to the specified kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.add_file_preservations' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.add_keys', 'W', 'Adds the given list of keys to the specified kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.add_keys' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.check_config_management', 'W', 'Check the configuration management status for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.check_config_management' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.check_remote_commands', 'W', 'Check the remote commands status flag for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.check_remote_commands' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.disable_config_management', 'W', 'Disables the configuration management flag in a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.disable_config_management' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.disable_remote_commands', 'W', 'Disables the remote command flag in a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.disable_remote_commands' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.enable_config_management', 'W', 'Enables the configuration management flag in a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.enable_config_management' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.enable_remote_commands', 'W', 'Enables the remote command flag in a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.enable_remote_commands' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.get_locale', 'R', 'Retrieves the locale for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.get_locale' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.get_partitioning_scheme', 'R', 'Get the partitioning scheme for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.get_partitioning_scheme' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.get_registration_type', 'W', 'returns the registration type of a given kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.get_registration_type' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.get_selinux', 'R', 'Retrieves the SELinux enforcing mode property of a kickstart'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.get_selinux' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.list_file_preservations', 'R', 'Returns the set of all file preservations associated with the given'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.list_file_preservations' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.list_keys', 'R', 'Returns the set of all keys associated with the given kickstart'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.list_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.remove_file_preservations', 'W', 'Removes the given list of file preservations from the specified'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.remove_file_preservations' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.remove_keys', 'W', 'Removes the given list of keys from the specified kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.remove_keys' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.set_locale', 'W', 'Sets the locale for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.set_locale' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.set_partitioning_scheme', 'W', 'Set the partitioning scheme for a kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.set_partitioning_scheme' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.set_registration_type', 'W', 'Sets the registration type of a given kickstart profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.set_registration_type' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.kickstart.profile.system.set_selinux', 'W', 'Sets the SELinux enforcing mode property of a kickstart profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.kickstart.profile.system.set_selinux' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.add_entitlements', 'W', 'Add entitlements to a server. Entitlements a server already has'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.add_entitlements' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.add_note', 'W', 'Add a new note to the given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.add_note' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.bootstrap', 'W', 'Bootstrap a system for management via either Salt or Salt SSH.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.bootstrap' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.bootstrap_with_private_ssh_key', 'W', 'Bootstrap a system for management via either Salt or Salt SSH.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.bootstrap_with_private_ssh_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.change_proxy', 'W', 'Connect given systems to another proxy.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.change_proxy' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.compare_package_profile', 'W', 'Compare a system''s packages against a package profile.  In'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.compare_package_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.compare_packages', 'W', 'Compares the packages installed on two systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.compare_packages' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.create_package_profile', 'W', 'Create a new stored Package Profile from a systems'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.create_package_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.create_system_profile', 'W', 'Creates a system record in database for a system that is not registered.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.create_system_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.create_system_record', 'W', 'Creates a cobbler system record with the specified kickstart label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.create_system_record' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_custom_values', 'W', 'Delete the custom values defined for the custom system information keys'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_custom_values' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_guest_profiles', 'W', 'Delete the specified list of guest profiles for a given host'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_guest_profiles' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_note', 'W', 'Deletes the given note from the server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_note' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_notes', 'W', 'Deletes all notes from the server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_notes' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_package_profile', 'W', 'Delete a package profile'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_package_profile' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_system', 'W', 'Delete a system given its client certificate.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_system' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_systems', 'W', 'Delete systems given a list of system ids asynchronously.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_systems' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.delete_tag_from_snapshot', 'W', 'Deletes tag from system snapshot'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.delete_tag_from_snapshot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.download_system_id', 'W', 'Get the system ID file for a given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.download_system_id' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_co_co_attestation_config', 'R', 'Return the Confidential Compute Attestation configuration for the given system id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_co_co_attestation_config' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_co_co_attestation_result_details', 'R', 'Return a specific results with all details'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_co_co_attestation_result_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_connection_path', 'R', 'Get the list of proxies that the given system connects'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_connection_path' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_cpu', 'R', 'Gets the CPU information of a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_cpu' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_custom_values', 'R', 'Get the custom data values defined for the server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_custom_values' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_details', 'R', 'Get system details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_devices', 'R', 'Gets a list of devices for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_devices' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_dmi', 'R', 'Gets the DMI information of a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_dmi' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_entitlements', 'R', 'Gets the entitlements for a given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_entitlements' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_event_details', 'R', 'Returns the details of the event associated with the specified server and event.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_event_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_event_history', 'R', 'Returns a list history items associated with the system, ordered'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_event_history' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_id', 'R', 'Get system IDs and last check in information for the given system name.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_installed_products', 'R', 'Get a list of installed products for given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_installed_products' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_kernel_live_patch', 'R', 'Returns the currently active kernel live patching version relative to'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_kernel_live_patch' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_latest_co_co_attestation_report', 'R', 'Return the latest report for the given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_latest_co_co_attestation_report' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_memory', 'R', 'Gets the memory information for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_memory' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_minion_id_map', 'R', 'Return a map from Salt minion IDs to System IDs.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_minion_id_map' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_name', 'R', 'Get system name and last check in information for the given system ID.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_name' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_network', 'R', 'Get the addresses and hostname for a given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_network' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_network_devices', 'R', 'Returns the network devices for the given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_network_devices' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_network_for_systems', 'R', 'Get the addresses and hostname for a given list of systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_network_for_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_osa_ping', 'R', 'get details about a ping sent to a system using OSA'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_osa_ping' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_pillar', 'R', 'Get pillar data of given category for given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_pillar' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_registration_date', 'R', 'Returns the date the system was registered.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_registration_date' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_relevant_errata', 'R', 'Returns a list of all errata that are relevant to the system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_relevant_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_relevant_errata_by_type', 'R', 'Returns a list of all errata of the specified type that are'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_relevant_errata_by_type' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_running_kernel', 'R', 'Returns the running kernel of the given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_running_kernel' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_script_action_details', 'R', 'Returns script details for script run actions'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_script_action_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_script_results', 'W', 'Fetch results from a script execution. Returns an empty array if no'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_script_results' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_subscribed_base_channel', 'R', 'Provides the base channel of a given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_subscribed_base_channel' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_system_currency_multipliers', 'R', 'Get the System Currency score multipliers'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_system_currency_multipliers' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_system_currency_scores', 'R', 'Get the System Currency scores for all servers the user has access to'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_system_currency_scores' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_unscheduled_errata', 'R', 'Provides an array of errata that are applicable to a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_unscheduled_errata' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_uuid', 'R', 'Get the UUID from the given system ID.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_uuid' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.get_variables', 'R', 'Lists kickstart variables set  in the system record'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.get_variables' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.is_nvre_installed', 'R', 'Check if the package with the given NVRE is installed on given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.is_nvre_installed' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_activation_keys', 'R', 'List the activation keys the system was registered with.  An empty'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_activation_keys' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_active_systems', 'R', 'Returns a list of active servers visible to the user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_active_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_active_systems_details', 'R', 'Given a list of server ids, returns a list of active servers'''
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_active_systems_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_administrators', 'R', 'Returns a list of users which can administer the system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_administrators' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_all_installable_packages', 'R', 'Get the list of all installable packages for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_all_installable_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_co_co_attestation_reports', 'R', 'Return a list of reports with its results for the given filters'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_co_co_attestation_reports' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_duplicates_by_hostname', 'R', 'List duplicate systems by Hostname.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_duplicates_by_hostname' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_duplicates_by_ip', 'R', 'List duplicate systems by IP Address.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_duplicates_by_ip' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_duplicates_by_mac', 'R', 'List duplicate systems by Mac Address.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_duplicates_by_mac' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_empty_system_profiles', 'R', 'Returns a list of empty system profiles visible to user (created by the createSystemProfile method).'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_empty_system_profiles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_extra_packages', 'R', 'List extra packages for a system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_extra_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_fqdns', 'R', 'Provides a list of FQDNs associated with a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_fqdns' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_groups', 'R', 'List the available groups for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_inactive_systems', 'R', 'Lists systems that have been inactive for the default period of'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_inactive_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_installed_packages', 'R', 'List the installed packages for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_installed_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_latest_available_package', 'R', 'Get the latest available version of a package for each system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_latest_available_package' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_latest_installable_packages', 'R', 'Get the list of latest installable packages for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_latest_installable_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_latest_upgradable_packages', 'R', 'Get the list of latest upgradable packages for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_latest_upgradable_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_migration_targets', 'R', 'List possible migration targets for a system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_migration_targets' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_newer_installed_packages', 'R', 'Given a package name, version, release, and epoch, returns the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_newer_installed_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_notes', 'R', 'Provides a list of notes associated with a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_notes' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_older_installed_packages', 'R', 'Given a package name, version, release, and epoch, returns'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_older_installed_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_out_of_date_systems', 'R', 'Returns list of systems needing package updates.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_out_of_date_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_package_profiles', 'R', 'List the package profiles in this organization'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_package_profiles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_package_state', 'R', 'List possible migration targets for a system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_package_state' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_packages', 'R', 'List the installed packages for a given system. Usage of listInstalledPackages is preferred,'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_packages_from_channel', 'R', 'Provides a list of packages installed on a system that are also'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_packages_from_channel' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_packages_lock_status', 'R', 'List current package locks status.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_packages_lock_status' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_physical_systems', 'R', 'Returns a list of all Physical servers visible to the user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_physical_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_subscribable_base_channels', 'R', 'Returns a list of subscribable base channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_subscribable_base_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_subscribable_child_channels', 'R', 'Returns a list of subscribable child channels.  This only shows channels'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_subscribable_child_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_subscribed_child_channels', 'R', 'Returns a list of subscribed child channels.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_subscribed_child_channels' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_suggested_reboot', 'R', 'List systems that require reboot.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_suggested_reboot' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_system_events', 'R', 'List system actions of the specified type that were *scheduled* against the given server after the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_system_events' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_system_groups_for_systems_with_entitlement', 'R', 'Returns the groups information a system is member of, for all the systems visible to the passed user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_system_groups_for_systems_with_entitlement' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_systems', 'R', 'Returns a list of all servers visible to the user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_systems_with_entitlement', 'R', 'Lists the systems that have the given entitlement'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_systems_with_entitlement' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_systems_with_extra_packages', 'R', 'List systems with extra packages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_systems_with_extra_packages' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_systems_with_package', 'R', 'Lists the systems that have the given installed package'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_systems_with_package' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_ungrouped_systems', 'R', 'List systems that are not associated with any system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_ungrouped_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_user_systems', 'R', 'List systems for a given user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_user_systems' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_virtual_guests', 'R', 'Lists the virtual guests for a given virtual host'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_virtual_guests' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.list_virtual_hosts', 'R', 'Lists the virtual hosts visible to the user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.list_virtual_hosts' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.obtain_reactivation_key', 'W', 'Obtains a reactivation key for this server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.obtain_reactivation_key' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provision_system', 'W', 'Provision a system using the specified kickstart/autoinstallation profile.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provision_system' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.provision_virtual_guest', 'W', 'Provision a guest on the host specified.  Defaults to:'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.provision_virtual_guest' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.refresh_pillar', 'W', 'refresh all the pillar data of a list of systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.refresh_pillar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.register_peripheral_server', 'W', 'Register foreign peripheral server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.register_peripheral_server' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.remove_entitlements', 'W', 'Remove addon entitlements from a server. Entitlements a server does'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.remove_entitlements' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_apply_errata', 'W', 'Schedules an action to apply errata updates to multiple systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_apply_errata' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_apply_highstate', 'W', 'Schedule highstate application for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_apply_highstate' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_apply_states', 'W', 'Schedule highstate application for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_apply_states' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_certificate_update', 'W', 'Schedule update of client certificate'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_certificate_update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_change_channels', 'W', 'Schedule an action to change the channels of the given system. Works for both traditional'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_change_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_co_co_attestation', 'W', 'Schedule Confidential Compute Attestation Action'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_co_co_attestation' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_dist_upgrade', 'W', 'Schedule a dist upgrade for a system. This call takes a list of channel'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_dist_upgrade' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_hardware_refresh', 'W', 'Schedule a hardware refresh for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_hardware_refresh' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_install', 'W', 'Schedule package installation for several systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_install' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_install_by_nevra', 'W', 'Schedule package installation for several systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_install_by_nevra' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_lock_change', 'W', 'Schedule package lock for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_lock_change' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_refresh', 'W', 'Schedule a package list refresh for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_refresh' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_remove', 'W', 'Schedule package removal for several systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_remove' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_remove_by_nevra', 'W', 'Schedule package removal for several systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_remove_by_nevra' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_package_update', 'W', 'Schedule full package update for several systems.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_package_update' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_product_migration', 'W', 'Schedule a Product migration for a system. This call is the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_product_migration' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_reboot', 'W', 'Schedule a reboot for a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_reboot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_s_p_migration', 'W', 'Schedule a Product migration for a system. This call is the'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_s_p_migration' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_script_run', 'W', 'Schedule a script to run.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_script_run' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.schedule_sync_packages_with_system', 'W', 'Sync packages from a source system to a target.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_sync_packages_with_system' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search_by_name', 'W', 'Returns a list of system IDs whose name matches'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search_by_name' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.send_osa_ping', 'W', 'send a ping to a system using OSA'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.send_osa_ping' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_base_channel', 'W', 'Assigns the server to a new base channel.  If the user provides an empty'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_base_channel' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_child_channels', 'W', 'Subscribe the given server to the child channels provided.  This'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_child_channels' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_co_co_attestation_config', 'W', 'Configure Confidential Compute Attestation for the given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_co_co_attestation_config' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_custom_values', 'W', 'Set custom values for the specified server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_custom_values' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_details', 'W', 'Set server details. All arguments are optional and will only be modified'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_group_membership', 'W', 'Set a servers membership in a given group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_group_membership' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_lock_status', 'W', 'Set server lock status.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_lock_status' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_pillar', 'W', 'Set pillar data of a system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_pillar' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_primary_fqdn', 'W', 'Sets new primary FQDN'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_primary_fqdn' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_primary_interface', 'W', 'Sets new primary network interface'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_primary_interface' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_profile_name', 'W', 'Set the profile name for the server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_profile_name' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.set_variables', 'W', 'Sets a list of kickstart variables in the cobbler system record'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.set_variables' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.tag_latest_snapshot', 'W', 'Tags latest system snapshot'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.tag_latest_snapshot' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.unentitle', 'W', 'Unentitle the system completely'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.unentitle' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.update_package_state', 'W', 'Update the package state of a given system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.update_package_state' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.update_peripheral_server_info', 'W', 'Update foreign peripheral server info.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.update_peripheral_server_info' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.upgrade_entitlement', 'W', 'Adds an entitlement to a given server.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.upgrade_entitlement' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.who_registered', 'W', 'Returns information about the user who registered the system'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.who_registered' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.monitoring.list_endpoints', 'R', 'Get the list of monitoring endpoint details.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.monitoring.list_endpoints' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.delete_xccdf_scan', 'W', 'Delete OpenSCAP XCCDF Scan from the #product() database. Note that'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.scap.delete_xccdf_scan' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.get_xccdf_scan_details', 'R', 'Get details of given OpenSCAP XCCDF scan.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.scap.get_xccdf_scan_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.get_xccdf_scan_rule_results', 'R', 'Return a full list of RuleResults for given OpenSCAP XCCDF scan.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.scap.get_xccdf_scan_rule_results' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.list_xccdf_scans', 'R', 'Return a list of finished OpenSCAP scans for a given system.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.scap.list_xccdf_scans' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.schedule_xccdf_scan', 'W', 'Schedule OpenSCAP scan.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.scap.schedule_xccdf_scan' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.device_description', 'R', 'List the systems which match the device description.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.device_description' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.device_driver', 'R', 'List the systems which match this device driver.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.device_driver' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.device_id', 'R', 'List the systems which match this device id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.device_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.device_vendor_id', 'R', 'List the systems which match this device vendor_id'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.device_vendor_id' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.hostname', 'R', 'List the systems which match this hostname'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.hostname' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.ip', 'R', 'List the systems which match this ip.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.ip' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.name_and_description', 'R', 'List the systems which match this name or description'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.name_and_description' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.search.uuid', 'R', 'List the systems which match this UUID'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.search.uuid' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.create_external_group_to_role_map', 'W', 'Externally authenticated users may be members of external groups. You'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.create_external_group_to_role_map' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.create_external_group_to_system_group_map', 'W', 'Externally authenticated users may be members of external groups. You'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.create_external_group_to_system_group_map' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.delete_external_group_to_role_map', 'W', 'Delete the role map for an external group. Can only be called'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.delete_external_group_to_role_map' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.delete_external_group_to_system_group_map', 'W', 'Delete the server group map for an external group. Can only be called'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.delete_external_group_to_system_group_map' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.get_default_org', 'R', 'Get the default org that users should be added in if orgunit from'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.get_default_org' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.get_external_group_to_role_map', 'R', 'Get a representation of the role mapping for an external group.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.get_external_group_to_role_map' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.get_external_group_to_system_group_map', 'R', 'Get a representation of the server group mapping for an external'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.get_external_group_to_system_group_map' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.get_keep_temporary_roles', 'R', 'Get whether we should keeps roles assigned to users because of'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.get_keep_temporary_roles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.get_use_org_unit', 'R', 'Get whether we place users into the organization that corresponds'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.get_use_org_unit' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.list_external_group_to_role_maps', 'R', 'List role mappings for all known external groups. Can only be called'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.list_external_group_to_role_maps' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.list_external_group_to_system_group_maps', 'R', 'List server group mappings for all known external groups. Can only be'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.list_external_group_to_system_group_maps' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.set_default_org', 'W', 'Set the default org that users should be added in if orgunit from'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.set_default_org' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.set_external_group_roles', 'W', 'Update the roles for an external group. Replace previously set roles'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.set_external_group_roles' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.set_external_group_system_groups', 'W', 'Update the server groups for an external group. Replace previously set'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.set_external_group_system_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.set_keep_temporary_roles', 'W', 'Set whether we should keeps roles assigned to users because of'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.set_keep_temporary_roles' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.external.set_use_org_unit', 'W', 'Set whether we place users into the organization that corresponds'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.external.set_use_org_unit' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.add_assigned_system_group', 'W', 'Add system group to user''s list of assigned system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.add_assigned_system_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.add_assigned_system_groups', 'W', 'Add system groups to user''s list of assigned system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.add_assigned_system_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.add_default_system_group', 'W', 'Add system group to user''s list of default system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.add_default_system_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.add_default_system_groups', 'W', 'Add system groups to user''s list of default system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.add_default_system_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.add_role', 'W', 'Adds a role to a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.add_role' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.create', 'W', 'Create a new user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.delete', 'W', 'Delete a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.disable', 'W', 'Disable a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.disable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.enable', 'W', 'Enable a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.enable' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.get_create_default_system_group', 'R', 'Returns the current value of the CreateDefaultSystemGroup setting.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.get_create_default_system_group' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.get_details', 'R', 'Returns the details about a given user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.get_details' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_assignable_roles', 'R', 'Returns a list of user roles that this user can assign to others.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_assignable_roles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_assigned_system_groups', 'R', 'Returns the system groups that a user can administer.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_assigned_system_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_default_system_groups', 'R', 'Returns a user''s list of default system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_default_system_groups' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_permissions', 'R', 'Lists the effective RBAC permissions of a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_permissions' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_roles', 'R', 'Returns a list of the user''s roles.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_roles' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.list_users', 'R', 'Returns a list of users in your organization.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.list_users' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.remove_assigned_system_group', 'W', 'Remove system group from the user''s list of assigned system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.remove_assigned_system_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.remove_assigned_system_groups', 'W', 'Remove system groups from a user''s list of assigned system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.remove_assigned_system_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.remove_default_system_group', 'W', 'Remove a system group from user''s list of default system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.remove_default_system_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.remove_default_system_groups', 'W', 'Remove system groups from a user''s list of default system groups.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.remove_default_system_groups' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.remove_role', 'W', 'Remove a role from a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.remove_role' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.set_create_default_system_group', 'W', 'Sets the value of the createDefaultSystemGroup setting.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.set_create_default_system_group' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.set_details', 'W', 'Updates the details of a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.set_details' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.set_errata_notifications', 'W', 'Enables/disables errata mail notifications for a specific user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.set_errata_notifications' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.set_read_only', 'W', 'Sets whether the target user should have only read-only API access or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.set_read_only' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.use_pam_authentication', 'W', 'Toggles whether or not a user uses PAM authentication or'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.use_pam_authentication' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.notifications.delete_notifications', 'W', 'Deletes multiple notifications'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.notifications.delete_notifications' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.notifications.get_notifications', 'R', 'Get all notifications from a user.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.notifications.get_notifications' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.notifications.set_all_notifications_read', 'W', 'Set all notifications from a user as read'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.notifications.set_all_notifications_read' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.user.notifications.set_notifications_read', 'W', 'Set notifications of the given user as read'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.user.notifications.set_notifications_read' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.create', 'W', 'Creates a Virtual Host Manager from given arguments'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.create' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.delete', 'W', 'Deletes a Virtual Host Manager with a given label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.delete' AND access_mode = 'W');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.get_detail', 'R', 'Gets details of a Virtual Host Manager with a given label'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.get_detail' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.get_module_parameters', 'R', 'Get a list of parameters for a virtual-host-gatherer module.'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.get_module_parameters' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.list_available_virtual_host_gatherer_modules', 'R', 'List all available modules from virtual-host-gatherer'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.list_available_virtual_host_gatherer_modules' AND access_mode = 'R');
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.virtualhostmanager.list_virtual_host_managers', 'R', 'Lists Virtual Host Managers visible to a user'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.virtualhostmanager.list_virtual_host_managers' AND access_mode = 'R');
