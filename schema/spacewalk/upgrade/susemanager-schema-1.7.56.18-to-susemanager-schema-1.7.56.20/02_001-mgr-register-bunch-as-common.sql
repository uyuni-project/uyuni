-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhntaskobunch set org_bunch=null where name = 'mgr-register-bunch';
