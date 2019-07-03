-- oracle equivalent source sha1 9dcda626b05182af31c7aa7adcbd4cfad975cdf7

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
