
create or replace function rhn_ksscript_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

drop trigger rhn_ksscript_mod_trig on rhnKickstartSession;

create trigger
rhn_ksscript_mod_trig
before insert or update on rhnKickstartScript
for each row
execute procedure rhn_ksscript_mod_trig_fun();

