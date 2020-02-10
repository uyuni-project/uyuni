-- oracle equivalent source sha1 f2794675b98155044a14dd41669af2aa258d2b70
-- This file is intentionally left empty.

create or replace function
insert_package_nevra(
        name_id_in in numeric,
        evr_id_in in numeric,
        package_arch_id_in in numeric
) returns numeric
as
$$
declare
    nevra_id numeric;
begin
    nevra_id := nextval('rhn_pkgnevra_id_seq');

    insert into rhnPackageNEVRA(id, name_id, evr_id, package_arch_id)
        values (nevra_id, name_id_in, evr_id_in, package_arch_id_in)
        on conflict do nothing;

    select id
        into strict nevra_id
        from rhnPackageNEVRA
        where name_id = name_id_in and evr_id = evr_id_in and
            (package_arch_id = package_arch_id_in or
            (package_arch_id is null and package_arch_id_in is null));

    return nevra_id;
end;
$$ language plpgsql;
