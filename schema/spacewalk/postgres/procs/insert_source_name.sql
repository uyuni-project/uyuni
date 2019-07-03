-- oracle equivalent source sha1 961eb765a11aacb9649ca6546c7d71ff06221eab

create or replace function
insert_source_name(name_in in varchar)
returns numeric
as
$$
declare
    source_id   numeric;
begin
    source_id := nextval('rhn_sourcerpm_id_seq');

    insert into rhnSourceRPM(id, name)
        values (source_id, name_in)
        on conflict do nothing;

    select id
        into strict source_id
        from rhnSourceRPM
        where name = name_in;

    return source_id;
end;
$$ language plpgsql;
