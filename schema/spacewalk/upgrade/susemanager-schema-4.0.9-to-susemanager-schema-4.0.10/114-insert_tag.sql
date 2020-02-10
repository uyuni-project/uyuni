-- oracle equivalent source sha1 1bc937c6a7a4323dc72c90dc79ec0ec0e6fdacb0

create or replace function
insert_tag(org_id_in in numeric, tag_name_id_in in numeric)
returns numeric
as $$
declare
    tag_id  numeric;
begin
    tag_id := nextval('rhn_tag_id_seq');

    insert into rhnTag(id, org_id, name_id)
        values (tag_id, org_id_in, tag_name_id_in)
        on conflict do nothing;

    select id
        into strict tag_id
        from rhnTag
        where org_id = org_id_in and
            name_id = tag_name_id_in;

    return tag_id;
end;
$$ language plpgsql;
