
create or replace function
insert_package_delta(n_in in varchar)
returns numeric
as
$$
declare
    name_id     numeric;
begin
    name_id := nextval('rhn_packagedelta_id_seq');

    insert into rhnPackageDelta(id, label)
        values (name_id, n_in)
        on conflict do nothing;

    select id
        into strict name_id
        from rhnpackagedelta
        where label = n_in;

    return name_id;
end;
$$ language plpgsql;
