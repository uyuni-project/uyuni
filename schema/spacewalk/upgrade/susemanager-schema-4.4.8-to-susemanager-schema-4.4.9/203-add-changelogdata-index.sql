create or replace function remove_duplicate_changelogdata()
returns void as
$$
begin
    with originals as (
        select
            min(id) as keep_id,
            array_agg(id) as duplicate_ids
        from rhnpackagechangelogdata
        group by name, text, time
        having count(*) > 1
    ), redundant as (
        select id
        from rhnpackagechangelogdata as data, originals as org
        where data.id = any(org.duplicate_ids)
            and data.id <> org.keep_id
    ), update_changelogdatarec as (
        update rhnpackagechangelogrec as rec
            set changelog_data_id = originals.keep_id
            from redundant, originals
            where rec.changelog_data_id = redundant.id
    )
    delete from rhnpackagechangelogdata as data
        using redundant
        where data.id = redundant.id;
end;
$$ language plpgsql;

select remove_duplicate_changelogdata();
drop function remove_duplicate_changelogdata();

drop index if exists rhn_pkg_cld_nt_idx;

create extension if not exists pgcrypto;
create unique index if not exists rhn_pkg_cld_ntt_idx
    on rhnpackagechangelogdata
    using btree(name, digest(text, 'sha512'::text), time);

