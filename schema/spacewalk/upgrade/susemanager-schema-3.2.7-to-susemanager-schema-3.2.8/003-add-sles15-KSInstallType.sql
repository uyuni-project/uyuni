insert into rhnKSInstallType (id, label, name)
(select sequence_nextval('rhn_ksinstalltype_id_seq'), 'sles15generic','SUSE Enterprise Linux 15' from dual
  where not exists (select 1 from rhnKSInstallType where label = 'sles15generic'));
