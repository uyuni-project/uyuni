-- oracle equivalent source sha1 3a243557dc0c065deb58725a0deb2014574b748a

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
