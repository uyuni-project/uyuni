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
