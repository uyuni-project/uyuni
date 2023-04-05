
--update pg_setting
update pg_settings set setting = 'rpm,' || setting where name = 'search_path';

   create or replace FUNCTION vercmp(
        e1 VARCHAR, v1 VARCHAR, r1 VARCHAR, 
        e2 VARCHAR, v2 VARCHAR, r2 VARCHAR)
    RETURNS INTEGER as $$
    declare
        rc INTEGER;
          ep1 INTEGER;
          ep2 INTEGER;
          BEGIN
            if e1 is null or e1 = '' then
              ep1 := 0;
            else
              ep1 := e1::integer;
            end if;
            if e2 is null or e2 = '' then
              ep2 := 0;
            else
              ep2 := e2::integer;
            end if;
            -- Epochs are non-null; compare them
            if ep1 < ep2 then return -1; end if;
            if ep1 > ep2 then return 1; end if;
            rc := rpm.rpmstrcmp(v1, v2);
            if rc != 0 then return rc; end if;
           return rpm.rpmstrcmp(r1, r2);
         END;
         $$ language 'plpgsql';

-- restore the original setting
update pg_settings set setting = overlay( setting placing '' from 1 for (length('rpm')+1) ) where name = 'search_path';
