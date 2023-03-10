
create or replace function rhn_ram_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	       
	return new;
end;
$$ language plpgsql;

create trigger
rhn_ram_mod_trig
before insert or update on rhnRam
for each row
execute procedure rhn_ram_mod_trig_fun();

create or replace function rhn_ram_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = new.server_id;
        return new;
end;
$$ language plpgsql;

create trigger
rhn_ram_up_trig
after update on rhnRam
for each row
when (OLD.ram is distinct from NEW.ram)
execute procedure rhn_ram_up_trig_fun();


