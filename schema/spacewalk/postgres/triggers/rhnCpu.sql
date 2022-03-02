
create or replace function rhn_cpu_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;

        return new;
end;
$$ language plpgsql;


create trigger
rhn_cpu_mod_trig
before insert or update on rhnCpu
for each row
execute procedure rhn_cpu_mod_trig_fun();

create or replace function rhn_cpu_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = new.server_id;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_cpu_up_trig
after update on rhnCpu
for each row
when (OLD.nrcpu is distinct from NEW.nrcpu OR OLD.nrsocket is distinct from NEW.nrsocket)
execute procedure rhn_cpu_up_trig_fun();
