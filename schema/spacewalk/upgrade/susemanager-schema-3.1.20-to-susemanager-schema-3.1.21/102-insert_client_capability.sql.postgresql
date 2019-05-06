-- oracle equivalent source sha1 f2b491caca6d4c831c063bd6eb6a6f1fe069d083

create or replace function
insert_client_capability(name_in in varchar)
returns numeric
as $$
declare
    cap_name_id     numeric;
begin
    cap_name_id := nextval('rhn_client_capname_id_seq');

    insert into rhnClientCapabilityName(id, name)
        values (cap_name_id, name_in)
        on conflict do nothing;

    select id
        into strict cap_name_id
        from rhnclientcapabilityname
        where name = name_in;

    return cap_name_id;
end;
$$ language plpgsql;
