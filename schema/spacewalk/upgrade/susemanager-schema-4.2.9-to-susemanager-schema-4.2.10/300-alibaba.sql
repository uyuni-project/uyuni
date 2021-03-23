--EPEL 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '21ea45ab2f86d6a1', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '21ea45ab2f86d6a1'));

insert into rhnPackageProvider (id, name) values
(sequence_nextval('rhn_package_provider_id_seq'), 'Alibaba' );

-- Alibaba Cloud Linux 2.1903
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'eb801c41873141a8', lookup_package_key_type('gpg'), lookup_package_provider('Alibaba') from dual where not exists (select 1 from rhnPackageKey where key_id = 'eb801c41873141a8'));

commit;
