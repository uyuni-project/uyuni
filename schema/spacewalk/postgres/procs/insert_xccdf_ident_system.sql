-- oracle equivalent source sha1 ec9ba49c1f25d81a100625db72795bba81976913

create or replace function
insert_xccdf_ident_system(system_in in varchar)
returns numeric
as
$$
declare
    ident_sys_id numeric;
begin
    ident_sys_id := nextval('rhn_xccdf_identsytem_id_seq');

    insert into rhnXccdfIdentSystem (id, system)
        values (ident_sys_id, system_in)
        on conflict do nothing;

    select id
        into strict ident_sys_id
        from rhnXccdfIdentSystem
        where system = system_in;

    return ident_sys_id;
end;
$$ language plpgsql;
