-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table suseKiwiProfile add column if not exists
    kiwi_options  VARCHAR(1024);
