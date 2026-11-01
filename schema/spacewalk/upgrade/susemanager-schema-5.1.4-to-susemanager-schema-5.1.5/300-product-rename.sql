-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnUserInfo SET web_theme = 'suse-light' WHERE web_theme = 'susemanager-light';
UPDATE rhnUserInfo SET web_theme = 'suse-dark' WHERE web_theme = 'susemanager-dark';
