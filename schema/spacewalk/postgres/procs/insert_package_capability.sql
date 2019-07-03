-- oracle equivalent source sha1 a397d522dd41720dcd8b77e0a20d999ea5365355

create or replace function
insert_package_capability(name_in in varchar, version_in in varchar default null)
returns numeric
as
$$
declare
    name_id numeric;
begin
    name_id := nextval('rhn_pkg_capability_id_seq');

    insert into rhnPackageCapability(id, name, version)
        values (name_id, name_in, version_in)
        on conflict do nothing;

    if version_in is null then
        select id
            into strict name_id
            from rhnpackagecapability
            where name = name_in and version is null;
    else
        select id
            into strict name_id
            from rhnpackagecapability
            where name = name_in and version = version_in;
    end if;

    return name_id;
end;
$$ language plpgsql;
