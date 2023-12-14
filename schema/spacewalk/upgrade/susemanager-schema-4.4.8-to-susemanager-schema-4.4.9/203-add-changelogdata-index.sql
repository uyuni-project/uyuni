create or replace function remove_duplicate_changelogdata()
returns void as
$$
begin
    with originals as (
        select
            min(id) as keep_id,
            array_remove(array_agg(id), min(id)) as duplicate_ids
        from rhnpackagechangelogdata
        group by name, text, time
        having count(*) > 1
    ), duplicate_rec as (
        update rhnpackagechangelogrec as rec
            set changelog_data_id = originals.keep_id
            from originals
            where rec.changelog_data_id = any(originals.duplicate_ids)
    )
    delete from rhnpackagechangelogdata as data
        using originals
        where data.id = any(originals.duplicate_ids);
end;
$$ language plpgsql;

select remove_duplicate_changelogdata();
drop function remove_duplicate_changelogdata();

drop index if exists rhn_pkg_cld_nt_idx;

create extension if not exists pgcrypto;
create unique index if not exists rhn_pkg_cld_ntt_idx
    on rhnpackagechangelogdata
    using btree(name, digest(text, 'sha512'::text), time);

