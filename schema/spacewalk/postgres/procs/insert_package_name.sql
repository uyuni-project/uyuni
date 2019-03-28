-- oracle equivalent source sha1 865d405e7f5618baa2e444e282fd8800296066f8

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
