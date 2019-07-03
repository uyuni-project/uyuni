-- oracle equivalent source sha1 893b032e20a1ced1cddc9cdba9284a7918088475

create or replace function
insert_tag_name(name_in in varchar)
returns numeric
as
$$
declare
    name_id numeric;
begin
    name_id := nextval('rhn_tagname_id_seq');

    insert into rhnTagName(id, name)
        values (name_id, name_in)
        on conflict do nothing;

    select id
        into strict name_id
        from rhnTagName
        where name = name_in;

    return name_id;
end;
$$ language plpgsql;
