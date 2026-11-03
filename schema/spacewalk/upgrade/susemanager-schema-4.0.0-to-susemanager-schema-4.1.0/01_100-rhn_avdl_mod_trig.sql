-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- oracle equivalent source none
create or replace function rhn_avdl_mod_trig_fun() returns trigger as
$$
begin
       new.modified := current_timestamp;
       return new;
end;
$$ language plpgsql;

drop trigger if exists rhn_avdl_mod_trig on rhnActionVirtDelete;
create trigger
rhn_avdl_mod_trig
before insert or update on rhnActionVirtDelete
for each row
execute procedure rhn_avdl_mod_trig_fun();
