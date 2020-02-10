-- oracle equivalent source sha1 af9d3087adc3dc024e959fce3af12a9f595ba2bd

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
