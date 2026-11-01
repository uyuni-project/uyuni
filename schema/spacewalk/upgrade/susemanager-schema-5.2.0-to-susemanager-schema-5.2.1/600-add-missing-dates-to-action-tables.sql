-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only


-- add dates to rhnActionScap
alter table rhnActionScap add column if not exists
    created TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;
alter table rhnActionScap add column if not exists
    modified TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;


-- add dates to rhnActionImageDeploy
alter table rhnActionImageDeploy add column if not exists
    created TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;
alter table rhnActionImageDeploy add column if not exists
    modified TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;


-- add dates to rhnActionPackage
alter table rhnActionPackage add column if not exists
    created TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;
alter table rhnActionPackage add column if not exists
    modified TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL;
