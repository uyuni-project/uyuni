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

-- WebUI permissions

-- Namespace: systems.autoinstallation.*
-- Namespace: systems.config.*
-- Namespace: config.*
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
