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

INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'activation_key_admin', 'Activation Key Administrator');
INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'image_admin', 'Image Administrator');
INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'config_admin', 'Configuration Administrator');
INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'channel_admin', 'Channel Administrator');
INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'system_group_admin', 'System Group Administrator');
INSERT INTO access.accessGroup(org_id, label, description) VALUES (null, 'regular_user', 'Regular User');
