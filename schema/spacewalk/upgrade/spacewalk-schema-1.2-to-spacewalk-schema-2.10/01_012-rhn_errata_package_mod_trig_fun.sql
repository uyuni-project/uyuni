
create or replace function rhn_errata_package_mod_trig_fun() returns trigger
as
$$
begin
        if tg_op='INSERT' or tg_op='UPDATE' then
                new.modified := current_timestamp;
	        return new;
        end if;
        if tg_op='DELETE' then
                update rhnErrata
                set last_modified = current_timestamp
                where rhnErrata.id in ( old.errata_id );
	        return old;
        end if;
end;
$$ language plpgsql;

