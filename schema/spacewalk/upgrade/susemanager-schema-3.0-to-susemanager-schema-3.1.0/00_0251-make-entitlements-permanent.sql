-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnServerGroupType SET permanent = 'Y' WHERE label IN ('enterprise_entitled', 'salt_entitled');

