-- Alibaba Cloud Linux 3.2104
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '53fe0598cac33616', lookup_package_key_type('gpg'),
    lookup_package_provider('Alibaba') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '53fe0598cac33616'));

commit;
