INSERT INTO rhnKSInstallType (id, label, name)
SELECT sequence_nextval('rhn_ksinstalltype_id_seq'), 'opensuse16.1','SUSE Linux Enterprise 16'
ON CONFLICT (label) DO NOTHING;
