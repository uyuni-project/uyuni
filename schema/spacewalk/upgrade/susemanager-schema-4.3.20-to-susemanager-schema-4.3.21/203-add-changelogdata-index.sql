create or replace function remove_duplicate_changelogdata()
returns void as
$$
declare original record;
declare duplicate record;
begin
    for original in select min(id) as id
            from rhnpackagechangelogdata
            group by name, text, time
            having count(*) > 1 loop
        for duplicate in select data2.id
                from rhnpackagechangelogdata data1, rhnpackagechangelogdata data2
                where data1.name = data2.name
                and data1.text = data2.text
                and data1.time = data2.time
                and data1.id != data2.id
                and data1.id = original.id loop
            update rhnpackagechangelogrec
                set changelog_data_id = original.id
                where changelog_data_id = duplicate.id;
            delete from rhnpackagechangelogdata
                where id = duplicate.id;
        end loop;
    end loop;
end;
$$ language plpgsql;

select remove_duplicate_changelogdata();
drop function remove_duplicate_changelogdata();

drop index if exists rhn_pkg_cld_nt_idx;

create extension if not exists pgcrypto;
create unique index if not exists rhn_pkg_cld_ntt_idx
    on rhnpackagechangelogdata
    using btree(name, digest(text, 'sha512'::text), time);

