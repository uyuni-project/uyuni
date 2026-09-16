-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only


create or replace function web_user_si_timestamp_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
 	return new;
end;
$$ language plpgsql;

create trigger
web_user_si_timestamp
before insert or update on web_user_site_info
for each row
execute procedure web_user_si_timestamp_fun();


