-- oracle equivalent source sha1 d95c4df0674a641eeca523333aef1f5fa2bc58d3
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

