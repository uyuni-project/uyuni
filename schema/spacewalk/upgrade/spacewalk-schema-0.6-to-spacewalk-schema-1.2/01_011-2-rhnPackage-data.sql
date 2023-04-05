-- 650129: we need to disable the trigger to prevent it from
-- automatically updating rhnPackage.last_modified values
alter trigger rhn_package_mod_trig disable;

declare
 md5_id number;
begin
  select id
    into md5_id
    from rhnChecksumType
   where label = 'md5';

  insert into rhnChecksum (id, checksum_type_id, checksum)
         (select rhnChecksum_seq.nextval, md5_id, csum
            from (select distinct md5sum as csum
                    from rhnPackage
                   minus
                  select checksum as csum
                    from rhnChecksum
                   where checksum_type_id = md5_id));
  commit;
  update rhnPackage p
     set checksum_id = (select id
                          from rhnChecksum c
                         where checksum_type_id = md5_id
                           and p.md5sum =  c.checksum);
  commit;
end;
/

-- enable the trigger again
alter trigger rhn_package_mod_trig enable;
