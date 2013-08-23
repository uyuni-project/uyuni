-- oracle equivalent source sha1 ae5dfce5e6a272c34824d2ad374671acb8134add
create or replace function rhn_actiondup_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actiondup_mod_trig
before insert or update on rhnActionDup
for each row
execute procedure rhn_actiondup_mod_trig_fun();

