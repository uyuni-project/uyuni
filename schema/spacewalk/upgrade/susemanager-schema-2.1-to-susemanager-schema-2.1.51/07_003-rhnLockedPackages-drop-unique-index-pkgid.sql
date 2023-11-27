drop index rhn_lp_pkg_id_uq;

create index rhn_lp_pkg_id_idx
  on rhnLockedPackages (pkg_id);
