create or replace function rhn_actionpackagedetails_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actionpackagedetails_mod_trig
before insert or update on rhnActionPackageDetails
for each row
execute procedure rhn_actionpackagedetails_mod_trig_fun();

