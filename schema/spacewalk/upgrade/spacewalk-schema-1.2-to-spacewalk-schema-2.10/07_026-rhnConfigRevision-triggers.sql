
create or replace function rhn_confrevision_del_trig_fun() returns trigger 
as
$$
declare
        cr_removed numeric := lookup_snapshot_invalid_reason('cr_removed');
begin
        update rhnSnapshot
           set invalid = cr_removed
         where id in (select snapshot_id
                        from rhnSnapshotConfigRevision
                       where config_revision_id = old.id);
        delete from rhnSnapshotConfigRevision
         where config_revision_id = old.id
           and snapshot_id in (select snapshot_id
                                 from rhnSnapshotConfigRevision
                                where config_revision_id = old.id);
        return old;
end;
$$ language plpgsql;
