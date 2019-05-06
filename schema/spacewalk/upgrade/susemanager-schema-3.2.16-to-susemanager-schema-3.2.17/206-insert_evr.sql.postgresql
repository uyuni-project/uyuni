-- oracle equivalent source sha1 4e2700d50093e37c44abcaeed16abc6f79298e2e

create or replace function
insert_evr(e_in in varchar, v_in in varchar, r_in in varchar)
returns numeric
as
$$
declare
    evr_id  numeric;
begin
    evr_id := nextval('rhn_pkg_evr_seq');

    insert into rhnPackageEVR(id, epoch, version, release, evr)
        values (evr_id, e_in, v_in, r_in, evr_t(e_in, v_in, r_in))
        on conflict do nothing;

    select id
        into strict evr_id
        from rhnPackageEVR
        where ((epoch is null and e_in is null) or (epoch = e_in)) and
           version = v_in and release = r_in;

    return evr_id;
end;
$$ language plpgsql;
