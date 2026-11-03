-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create or replace function
sequence_currval( seq_name regclass ) returns bigint as
$$
	select currval($1);
$$ language sql;
