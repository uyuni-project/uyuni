-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnPackage
   set summary = rtrim(summary, chr(10))
 where summary <> rtrim(summary, chr(10));
