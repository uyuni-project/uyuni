-- oracle equivalent source sha1 39afdd1b896fabd1a7be60619144b2c40dd397ed

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
   AND (to_date('1970-01-01', 'YYYY-MM-DD') + numtodsinterval(S.last_boot, 'second')) < SP.installtime at time zone 'UTC'
   AND E.id = EK.errata_id;
