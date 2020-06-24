
create or replace function suse_clusters_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
suse_clusters_mod_trig
before insert or update on suseClusters
for each row
execute procedure suse_clusters_mod_trig_fun();