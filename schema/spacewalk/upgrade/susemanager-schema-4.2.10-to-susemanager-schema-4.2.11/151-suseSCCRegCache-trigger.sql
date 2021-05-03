
create or replace function suse_sccregcache_mod_trig_fun() returns trigger as
$$
begin
        new.modified = current_timestamp;
        return new;
end;
$$ language plpgsql;

drop trigger if exists suse_sccregcache_mod_trig on suseSCCRegCache;
create trigger
suse_sccregcache_mod_trig
before insert or update on suseSCCRegCache
for each row
execute procedure suse_sccregcache_mod_trig_fun();
