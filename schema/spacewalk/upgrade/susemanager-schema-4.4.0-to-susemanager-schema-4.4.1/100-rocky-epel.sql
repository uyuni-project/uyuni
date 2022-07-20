-- Rocky Linux 9
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '702d426d350d275d', lookup_package_key_type('gpg'),
    lookup_package_provider('Rocky Linux') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '702d426d350d275d'));

-- EPEL 9
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '8a3872bf3228467c', lookup_package_key_type('gpg'),
    lookup_package_provider('EPEL') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '8a3872bf3228467c'));
