
drop index if exists rhn_pkg_cap_name_version_uq;
drop index if exists rhn_pkg_cap_name_uq;

create or replace function fix_rhn_pkg_cap_name_version_uq()
returns void as
$$
declare original record;
declare duplicate record;
begin
  for original in select min(id) as id
          from rhnPackageCapability
      group by name, version
        having count(*) > 1 loop
    for duplicate in select cap2.id
             from rhnPackageCapability cap1, rhnPackageCapability cap2
            where
                  cap1.name = cap2.name
              and (cap1.version = cap2.version
               or (cap1.version is NULL and cap2.version is NULL))
              and cap1.id != cap2.id
              and cap1.id = original.id loop
      update rhnactionpackageremovalfailure set capability_id = original.id where capability_id = duplicate.id;
      begin
        update rhnPackageBreaks set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageConflicts set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageEnhances set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageFile set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageObsoletes set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackagePredepends set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageProvides set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageRecommends set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageRequires set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageSuggests set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnPackageSupplements set capability_id = original.id where capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnServerActionVerifyMissing set package_capability_id = original.id where package_capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      begin
        update rhnServerActionVerifyResult set package_capability_id = original.id where package_capability_id = duplicate.id;
      exception when unique_violation then
        -- do nothing as data already exists
        null;
      end;
      delete from rhnPackageCapability where id = duplicate.id;
    end loop;
  end loop;
end;
$$ language plpgsql;

select fix_rhn_pkg_cap_name_version_uq();

drop function fix_rhn_pkg_cap_name_version_uq();

CREATE UNIQUE INDEX rhn_pkg_cap_name_version_uq
    ON rhnPackageCapability (name, version)
 where version is not null;

create unique index rhn_pkg_cap_name_uq
    on rhnPackageCapability (name)
 where version is null;
