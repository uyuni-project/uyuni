-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM rhnUserGroupType WHERE label = 'coma_admin';
DELETE FROM rhnUserGroupType WHERE label = 'coma_author';
DELETE FROM rhnUserGroupType WHERE label = 'coma_publisher';
