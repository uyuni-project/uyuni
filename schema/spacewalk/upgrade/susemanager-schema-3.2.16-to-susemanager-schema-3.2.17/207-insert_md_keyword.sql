-- oracle equivalent source sha1 60edd88d0e8f4bb54a90f8151e11c8f99269e1de

create or replace function
insert_md_keyword(label_in in varchar)
returns numeric
as
$$
declare
    md_keyword_id numeric;
begin
    md_keyword_id := nextval('suse_mdkeyword_id_seq');

    insert into suseMdKeyword (id, label)
        values (md_keyword_id, label_in)
        on conflict do nothing;

    select id
        into strict md_keyword_id
        from suseMdKeyword
        where label = label_in;

    return md_keyword_id;
end;
$$ language plpgsql;
