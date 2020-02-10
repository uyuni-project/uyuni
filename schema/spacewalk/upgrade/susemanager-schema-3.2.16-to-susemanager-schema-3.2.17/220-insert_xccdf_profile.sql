-- oracle equivalent source sha1 790b20d085c594409001151e850f9bc7b0e7000b

create or replace function
insert_xccdf_profile(identifier_in in varchar, title_in in varchar)
returns numeric
as
$$
declare
    profile_id numeric;
begin
    profile_id := nextval('rhn_xccdf_profile_id_seq');

    insert into rhnXccdfProfile (id, identifier, title)
        values (profile_id, identifier_in, title_in)
        on conflict do nothing;

    select id
        into profile_id
        from rhnXccdfProfile
        where identifier = identifier_in and title = title_in;

    return profile_id;
end;
$$ language plpgsql;
