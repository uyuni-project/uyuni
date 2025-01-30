drop trigger if exists rhn_cpu_up_trig on rhnCpu;

create trigger
rhn_cpu_up_trig
after update on rhnCpu
for each row
when (OLD.nrcpu is distinct from NEW.nrcpu OR OLD.nrsocket is distinct from NEW.nrsocket OR OLD.nrcore is distinct from NEW.nrcore OR OLD.nrthread is distinct from NEW.nrthread OR OLD.arch_specs is distinct from NEW.arch_specs)
execute procedure rhn_cpu_up_trig_fun();
