-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table web_customer
  add crash_file_sizelimit
      NUMERIC default(2048) not null
      constraint web_customer_sizelimit_chk check (crash_file_sizelimit >= 0);
