
create or replace function
insert_config_filename(name_in in varchar)
returns numeric as
$$
declare
    name_id     numeric;
begin
    name_id := nextval('rhn_cfname_id_seq');

    insert into rhnConfigFileName (id, path)
        values (name_id, name_in)
        on conflict do nothing;

    select id
        into strict name_id
        from rhnconfigfilename
        where path = name_in;

    return name_id;
end;
$$ language plpgsql;
