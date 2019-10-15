-- oracle equivalent source sha1 da49e184ecfbbc965fc65549e7ffecef9f1a2ffc

create or replace function
insert_xccdf_ident(ident_sys_id_in in numeric, identifier_in in varchar)
returns numeric
as
$$
declare
    xccdf_ident_id numeric;
begin
    xccdf_ident_id := nextval('rhn_xccdf_ident_id_seq');

    insert into rhnXccdfIdent (id, identsystem_id, identifier)
        values (xccdf_ident_id, ident_sys_id_in, identifier_in)
        on conflict do nothing;

    select id
        into strict xccdf_ident_id
        from rhnXccdfIdent
        where identsystem_id = ident_sys_id_in and identifier = identifier_in;

    return xccdf_ident_id;
end;
$$ language plpgsql;
