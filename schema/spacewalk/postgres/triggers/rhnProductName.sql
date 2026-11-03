-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only


create or replace function product_name_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	       
	return new;
end;
$$ language plpgsql;

create trigger
product_name_mod_trig
before insert or update on rhnProductName
for each row
execute procedure product_name_mod_trig_fun();


