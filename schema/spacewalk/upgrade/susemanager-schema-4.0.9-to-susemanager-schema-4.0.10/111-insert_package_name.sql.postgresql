-- oracle equivalent source sha1 63cf9ec1a31f4c751d88acc08d832ec4e4d925ca

create or replace function
insert_package_name(name_in in varchar)
returns numeric
as
$$
declare
    name_id     numeric;
begin
    name_id := nextval('rhn_pkg_name_seq');

    insert into rhnPackageName(id, name)
        values (name_id, name_in)
        on conflict do nothing;

    select id
        into strict name_id
        from rhnPackageName
        where name = name_in;

    return name_id;
end;
$$ language plpgsql;
