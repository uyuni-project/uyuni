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

DROP INDEX IF EXISTS access.access_group_label_uq;
CREATE UNIQUE INDEX IF NOT EXISTS access_group_org_label_uq
ON access.accessGroup(org_id, label);

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'activation_key_admin', 'Activation Key Administrator'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'activation_key_admin');

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'image_admin', 'Image Administrator'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'image_admin');

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'config_admin', 'Configuration Administrator'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'config_admin');

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'channel_admin', 'Channel Administrator'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'channel_admin');

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'system_group_admin', 'System Group Administrator'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'system_group_admin');

INSERT INTO access.accessGroup(org_id, label, description) SELECT null, 'regular_user', 'Regular User'
WHERE NOT EXISTS (SELECT 1 FROM access.accessGroup WHERE org_id IS NULL AND label = 'regular_user');
