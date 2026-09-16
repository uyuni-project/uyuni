-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create or replace function
lookup_snapshot_invalid_reason(label_in in varchar2)
return number
is
	snapshot_invalid_reason_id number;
begin
    select id
      into snapshot_invalid_reason_id
      from rhnsnapshotinvalidreason
     where label = label_in;

    return snapshot_invalid_reason_id;
exception when no_data_found then
    rhn_exception.raise_exception('invalid_snapshot_invalid_reason');
end;
/
show errors
