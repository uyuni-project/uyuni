-- oracle equivalent source sha1 751721892b36de7119b033cc88751359ea32b1c1
create or replace function rhn_actiondupprod_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actiondupprod_mod_trig
before insert or update on rhnActionDupProduct
for each row
execute procedure rhn_actiondupprod_mod_trig_fun();

