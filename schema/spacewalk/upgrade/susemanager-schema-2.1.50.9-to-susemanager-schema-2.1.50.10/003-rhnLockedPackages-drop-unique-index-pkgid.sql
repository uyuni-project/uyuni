-- oracle equivalent source sha1 c7dcdbf8488490c46155d11fb2de92032aeb9af4
drop index rhn_lp_pkg_id_uq;

create index rhn_lp_pkg_id_idx
  on rhnLockedPackages (pkg_id);
