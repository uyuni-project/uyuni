-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create table rhnISSSlaveOrgs (
    slave_id NUMERIC not null constraint rhn_isssorg_sid_fk references rhnISSSlave(id) on delete cascade,
    org_id NUMERIC not null constraint rhn_isssorg_oid_fk references web_customer(id) on delete cascade,
    created TIMESTAMPTZ default (current_timestamp) not null
);
