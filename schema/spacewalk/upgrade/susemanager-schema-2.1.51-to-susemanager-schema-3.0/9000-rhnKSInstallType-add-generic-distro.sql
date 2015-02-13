insert into rhnKSInstallType (id, label, name)
    (select sequence_nextval('rhn_ksinstalltype_id_seq'), 'sles10generic','SUSE Enterprise Linux 10' from dual
     where not exists (select 1 from rhnKSInstallType where label = 'sles10generic'));

insert into rhnKSInstallType (id, label, name)
    (select sequence_nextval('rhn_ksinstalltype_id_seq'), 'sles11generic','SUSE Enterprise Linux 11' from dual
     where not exists (select 1 from rhnKSInstallType where label = 'sles11generic'));

insert into rhnKSInstallType (id, label, name)
    (select sequence_nextval('rhn_ksinstalltype_id_seq'), 'sles12generic','SUSE Enterprise Linux 12' from dual
     where not exists (select 1 from rhnKSInstallType where label = 'sles12generic'));
