-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table web_contact
    add read_only char(1) default('N') not null
        constraint web_contact_ro_ck check (read_only in ('Y', 'N'));
