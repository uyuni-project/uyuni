insert into rhnPackageProvider (id, name)
    (select sequence_nextval('rhn_package_provider_id_seq'), 'Rocky Linux' from dual
    where not exists (select 1 from rhnPackageProvider where name = 'Rocky Linux'));

-- Alma Linux 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '15af5dac6d745a60', lookup_package_key_type('gpg'),
    lookup_package_provider('Rocky Linux') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '15af5dac6d745a60'));
