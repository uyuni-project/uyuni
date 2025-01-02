CREATE OR REPLACE function suse_minion_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        RETURN new;
END;
$$ language plpgsql;

CREATE TRIGGER
suse_minion_info_mod_trig
BEFORE INSERT OR UPDATE ON suseMinionInfo
FOR EACH ROW
EXECUTE PROCEDURE suse_minion_info_mod_trig_fun();

create or replace function suse_minion_info_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = new.server_id;
	return new;
end;
$$ language plpgsql;

create trigger
suse_minion_info_up_trig
after update on suseMinionInfo
for each row
when (OLD.container_runtime is distinct from NEW.container_runtime OR OLD.uname is distinct from NEW.uname)
execute procedure suse_minion_info_up_trig_fun();
