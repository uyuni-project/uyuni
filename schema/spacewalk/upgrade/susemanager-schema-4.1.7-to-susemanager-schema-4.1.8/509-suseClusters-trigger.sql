
create or replace function suse_clusters_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

drop trigger if exists suse_clusters_mod_trig on suseClusters;
create trigger
suse_clusters_mod_trig
before insert or update on suseClusters
for each row
execute procedure suse_clusters_mod_trig_fun();