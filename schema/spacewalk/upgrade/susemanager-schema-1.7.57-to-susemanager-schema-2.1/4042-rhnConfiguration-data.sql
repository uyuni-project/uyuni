-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnConfiguration (key, description) VALUES ('extauth_default_orgid', 'Organization id, where externally authenticated users will be created.');
INSERT INTO rhnConfiguration (key, description, default_value) VALUES ('extauth_use_orgunit', 'Use Org. Unit IPA setting as organization name to create externally authenticated users in.', 'false');
