-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- Remove things that don't need documentation

DROP TABLE IF EXISTS DUAL;

DROP TABLE IF EXISTS VersionInfo;

DROP FUNCTION IF EXISTS create_varnull_constriants;

DROP EXTENSION IF EXISTS dblink;
