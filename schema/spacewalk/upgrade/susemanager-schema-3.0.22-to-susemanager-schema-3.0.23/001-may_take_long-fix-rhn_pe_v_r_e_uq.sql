-- oracle equivalent source sha1 b14267384bc104605623a41b755e68e0103b5aa8

drop index if exists rhn_pe_v_r_e_uq;
drop index if exists rhn_pe_v_r_uq;

create or replace function fix_rhn_pe_v_r_e_uq()
returns void as
$$
declare original record;
declare duplicate record;
declare nevra_orig record;
declare nevra_dup record;
declare snapshotitem record;
declare serverpackage record;
begin
  for original in select min(id) as id
          from rhnpackageevr
      group by version, release, epoch
        having count(*) > 1 loop
    for duplicate in select evr2.id
             from rhnpackageevr evr1, rhnpackageevr evr2
            where evr1.version = evr2.version
              and evr1.release = evr2.release
              and (evr1.epoch = evr2.epoch
               or (evr1.epoch is NULL and evr2.epoch is NULL))
              and evr1.id != evr2.id
              and evr1.id = original.id loop
      update rhnactionpackage set evr_id = original.id where evr_id = duplicate.id;
      update rhnactionpackageremovalfailure set evr_id = original.id where evr_id = duplicate.id;
      update rhnLockedPackages set evr_id = original.id where evr_id = duplicate.id;
      update rhnchannelnewestpackage set evr_id = original.id where evr_id = duplicate.id;
      update rhnpackage set evr_id = original.id where evr_id = duplicate.id;
      select id into nevra_orig from rhnpackagenevra where evr_id = original.id;
      if NOT FOUND then
        update rhnpackagenevra set evr_id = original.id where evr_id = duplicate.id;
      else
        for nevra_dup in select id
                           from rhnpackagenevra
                          where evr_id = duplicate.id loop
          for snapshotitem in select snapshot_id
                                from rhnsnapshotpackage
                               where nevra_id = nevra_dup.id loop
            begin
              update rhnsnapshotpackage set nevra_id = nevra_orig.id where nevra_id = nevra_dup.id and snapshot_id = snapshotitem.snapshot_id;
            exception when unique_violation then
              delete from rhnsnapshotpackage where nevra_id = nevra_dup.id and snapshot_id = snapshotitem.snapshot_id;
            end;
          end loop;
        end loop;
        delete from rhnpackagenevra where evr_id = duplicate.id;
      end if;
      update rhnproxyinfo set proxy_evr_id = original.id where proxy_evr_id = duplicate.id;
      update rhnserveractionverifymissing set package_evr_id = original.id where package_evr_id = duplicate.id;
      update rhnserveractionverifyresult set package_evr_id = original.id where package_evr_id = duplicate.id;
      update rhnsatelliteinfo set evr_id = original.id where evr_id = duplicate.id;
      update rhnservercrash set package_evr_id = original.id where package_evr_id = duplicate.id;
      update rhnserverprofilepackage set evr_id = original.id where evr_id = duplicate.id;
      update rhntransactionpackage set evr_id = original.id where evr_id = duplicate.id;
      update rhnversioninfo set evr_id = original.id where evr_id = duplicate.id;

      for serverpackage in select * from rhnserverpackage where evr_id = duplicate.id loop
          begin
            update rhnserverpackage set evr_id = original.id where evr_id = duplicate.id and
                server_id = serverpackage.server_id and
                name_id = serverpackage.name_id and
                evr_id = serverpackage.evr_id and
                package_arch_id = serverpackage.package_arch_id;
          exception when unique_violation then
            delete from rhnserverpackage where evr_id = duplicate.id and
                server_id = serverpackage.server_id and
                name_id = serverpackage.name_id and
                evr_id = serverpackage.evr_id and
                package_arch_id = serverpackage.package_arch_id;
          end;
      end loop;

      update susePackageState set evr_id = original.id where evr_id = duplicate.id;
      update suseproductfile set evr_id = original.id where evr_id = duplicate.id;
      delete from rhnpackageevr where id = duplicate.id;
    end loop;
  end loop;
end;
$$ language plpgsql;

drop trigger if exists rhn_pack_evr_no_updel_trig on rhnpackageevr;

select fix_rhn_pe_v_r_e_uq();

drop function fix_rhn_pe_v_r_e_uq();

create trigger
rhn_pack_evr_no_updel_trig
before update or delete on rhnpackageevr
execute procedure no_operation_trig_fun();

create unique index rhn_pe_v_r_e_uq
    on rhnpackageevr (version, release, epoch)
 where epoch is not null;

create unique index rhn_pe_v_r_uq
    on rhnpackageevr (version, release)
 where epoch is null;
