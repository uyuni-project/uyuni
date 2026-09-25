-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create table rhnIssMaster (
    id NUMERIC not null constraint rhn_iss_master_id_pk primary key,
    label VARCHAR(256) not null constraint rhn_iss_master_label_uq unique,
    created TIMESTAMPTZ default (current_timestamp) not null
);

create sequence rhn_issmaster_seq;
