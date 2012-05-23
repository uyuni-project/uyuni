-- oracle equivalent source sha1 8bb186553c10671f68156abb0ade5c26ffc0971b
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

