
-- provide info about possible duplicate minion ids
select 'DUPLICATE MINION IDs FOUND:' || X.minion_id || ' Manual cleanup required!' message
from (select minion_id, count(server_id) dup
        from suseMinionInfo
       group by minion_id) X
where X.dup > 1;

select m.minion_id, s.name, s.id, s.org_id, s.os
  from rhnServer s
  join suseMinionInfo m on s.id = m.server_id
order by m.minion_id, s.name;

alter table suseMinionInfo drop CONSTRAINT IF EXISTS rhn_minion_info_miid_uq;

drop index if exists rhn_minion_info_miid_uq;

create unique index rhn_minion_info_miid_uq
    on suseMinionInfo (minion_id);
