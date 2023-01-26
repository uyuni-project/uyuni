drop trigger if exists rhn_cpu_up_trig on rhnCpu;
create trigger
rhn_cpu_up_trig
after update on rhnCpu
for each row
when (OLD.nrcpu is distinct from NEW.nrcpu OR OLD.nrsocket is distinct from NEW.nrsocket OR OLD.nrcore is distinct from NEW.nrcore OR OLD.nrthread is distinct from NEW.nrthread)
execute procedure rhn_cpu_up_trig_fun();


create or replace function rhn_ram_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = new.server_id;
        return new;
end;
$$ language plpgsql;

drop trigger if exists rhn_ram_up_trig on rhnRam;
create trigger
rhn_ram_up_trig
after update on rhnRam
for each row
when (OLD.ram is distinct from NEW.ram)
execute procedure rhn_ram_up_trig_fun();

create or replace function rhn_virtinst_iud_trig_fun() returns trigger
as
$$
begin
        if tg_op='INSERT' or tg_op='UPDATE' then
                if new.host_system_id is not null and new.virtual_system_id is not null then
                        update suseSCCRegCache
                           set scc_reg_required = 'Y'
                         where server_id = new.host_system_id;
                end if;
                return new;
        end if;
        if tg_op='DELETE' then
                if old.host_system_id is not null and old.virtual_system_id is not null then
                        update suseSCCRegCache
                           set scc_reg_required = 'Y'
                         where server_id = old.host_system_id;
                end if;
                return old;
        end if;
end;
$$ language plpgsql;

drop trigger if exists rhn_virtinst_iud_trig on rhnVirtualInstance;
create trigger
rhn_virtinst_iud_trig
after insert or update or delete on rhnVirtualInstance
execute procedure rhn_virtinst_iud_trig_fun();
