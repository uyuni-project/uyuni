-- oracle equivalent source sha1 0a96d7c09dc378f5772669536dfb390d1cec81a4

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

