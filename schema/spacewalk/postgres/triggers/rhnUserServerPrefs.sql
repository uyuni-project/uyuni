-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create or replace function rhn_u_s_prefs_mod_trig_fun() returns trigger as
$$
begin
	new.modified = current_timestamp;
	new.value := upper (new.value);
 	return new;
end;
$$ language plpgsql;

create trigger
rhn_u_s_prefs_mod_trig
before insert or update on rhnUserServerPrefs
for each row
execute procedure rhn_u_s_prefs_mod_trig_fun();


