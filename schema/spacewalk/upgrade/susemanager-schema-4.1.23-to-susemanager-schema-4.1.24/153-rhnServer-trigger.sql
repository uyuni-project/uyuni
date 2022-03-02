create or replace function rhn_server_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
          set scc_reg_required = 'Y'
        where server_id = old.id;
       return new;
end;
$$ language plpgsql;

drop trigger if exists rhn_server_up_trig on rhnServer;
create trigger
rhn_server_up_trig
after update on rhnServer
for each row
when (OLD.hostname is distinct from NEW.hostname or OLD.payg is distinct from NEW.payg)
execute procedure rhn_server_up_trig_fun();

