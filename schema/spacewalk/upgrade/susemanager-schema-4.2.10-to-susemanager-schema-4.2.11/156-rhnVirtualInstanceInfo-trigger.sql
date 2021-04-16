create or replace function rhn_virtinst_info_iud_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = (select virtual_system_id from rhnvirtualinstance WHERE id = new.instance_id);
        return new;
end;
$$ language plpgsql;

drop trigger if exists rhn_virtinst_info_iud_trig on rhnVirtualInstanceInfo;
create trigger
rhn_virtinst_info_iud_trig
after update on rhnVirtualInstanceInfo
for each row
when (OLD.instance_type is distinct from NEW.instance_type)
execute procedure rhn_virtinst_info_iud_trig_fun();
