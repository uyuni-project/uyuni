
create or replace function suse_pcdv_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
 	return new;
end;
$$ language plpgsql;

create trigger
suse_pcdv_mod_trig
before insert or update on suseProfileCustomDataValue
for each row
execute procedure suse_pcdv_mod_trig_fun();

