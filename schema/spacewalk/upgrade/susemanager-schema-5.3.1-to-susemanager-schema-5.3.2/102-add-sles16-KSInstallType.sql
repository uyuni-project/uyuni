insert into rhnKSInstallType (id, label, name) (
        select sequence_nextval('rhn_ksinstalltype_id_seq'),
               'sles16generic','SUSE Linux Enterprise 16'
        from dual
        where not exists (select 1 from rhnKSInstallType where label = 'sles16generic')
    );
