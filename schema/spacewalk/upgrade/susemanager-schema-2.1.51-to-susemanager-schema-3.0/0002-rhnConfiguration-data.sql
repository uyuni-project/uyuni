-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnConfiguration (key, description, default_value) VALUES ('extauth_keep_temproles', 'Keep temporary user roles granted due to the external authentication setup for subsequent logins using password.', 'false');
