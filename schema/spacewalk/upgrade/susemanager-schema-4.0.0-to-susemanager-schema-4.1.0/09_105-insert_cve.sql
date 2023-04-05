
create or replace function
insert_cve(name_in in varchar)
returns numeric
as $$
declare
    name_id     numeric;
begin
    name_id := nextval('rhn_cve_id_seq');

    insert into rhnCVE (id, name)
        values (name_id, name_in)
        on conflict do nothing;

    select id
        into strict name_id
        from rhnCVE
        where name = name_in;

    return name_id;
end;
$$ language plpgsql;
