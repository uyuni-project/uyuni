
create or replace function suse_srvcocoatt_rep_mod_trig_fun() returns trigger as
$$
begin
	new.modified = current_timestamp;
 	return new;
end;
$$ language plpgsql;

create trigger
suse_srvcocoatt_rep_mod_trig
before insert or update on suseServerCoCoAttestationReport
for each row
execute procedure suse_srvcocoatt_rep_mod_trig_fun();
