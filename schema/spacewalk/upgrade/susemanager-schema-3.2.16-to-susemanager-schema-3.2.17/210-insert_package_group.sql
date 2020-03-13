
create or replace function
insert_package_group(name_in in varchar)
returns numeric
as
$$
declare
    package_id   numeric;
begin
    package_id := nextval('rhn_package_group_id_seq');

    insert into rhnPackageGroup(id, name)
        values (package_id, name_in)
        on conflict do nothing;

    select id
        into strict package_id
        from rhnPackageGroup
        where name = name_in;

    return package_id;
end;
$$ language plpgsql;
