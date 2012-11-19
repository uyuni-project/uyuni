
create or replace view allServerKeywordSinceReboot as
SELECT DISTINCT S.id, S.NAME,
       (SELECT 1
          FROM rhnServerFeaturesView SFV
         WHERE SFV.server_id = S.id
           AND SFV.label = 'ftr_system_grouping') AS selectable,
        S.org_id,
        EK.keyword,
        USP.user_id
  FROM rhnServer S,
       rhnErrata E,
       rhnServerInfo SI,
       rhnServerPackage SP,
       rhnPackage P,
       rhnErrataPackage EP,
       rhnerratakeyword EK,
       rhnUserServerPerms USP
 WHERE USP.server_id = S.id
   AND SI.server_id = S.id
   AND SP.server_id = S.id
   AND P.evr_id = SP.evr_id
   AND P.name_id = SP.name_id
   AND EP.errata_id = E.id
   AND EP.package_id = P.id
   AND (to_timestamp_tz('19700101 00:00:00 +0:00','yyyymmdd hh24:mi:ss tzh:tzm') + numtodsinterval(S.last_boot, 'second')) < SP.installtime
   AND E.id = EK.errata_id;
