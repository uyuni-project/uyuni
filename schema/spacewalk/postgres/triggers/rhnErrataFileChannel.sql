-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create or replace function rhn_efilec_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_efilec_mod_trig
before insert or update on rhnErrataFileChannel
for each row
execute procedure rhn_efilec_mod_trig_fun();
