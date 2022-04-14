
create or replace function suse_mgr_info_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	       
	return new;
end;
$$ language plpgsql;

create trigger
suse_mgr_info_mod_trig
before insert or update on suseMgrServerInfo
for each row
execute procedure suse_mgr_info_mod_trig_fun();
