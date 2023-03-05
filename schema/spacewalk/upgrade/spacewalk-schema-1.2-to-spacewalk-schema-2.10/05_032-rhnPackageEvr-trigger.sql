
create trigger
rhn_pack_evr_no_updel_trig
before update or delete on rhnPackageEvr
execute procedure no_operation_trig_fun();

