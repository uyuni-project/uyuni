-- oracle equivalent source sha1 1a06d95f25bbd8dc9eeae101877119949156328b

create or replace function rhn_act_apply_states_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_act_apply_states_mod_trig
before insert or update on rhnActionApplyStates
for each row
execute procedure rhn_act_apply_states_mod_trig_fun();
