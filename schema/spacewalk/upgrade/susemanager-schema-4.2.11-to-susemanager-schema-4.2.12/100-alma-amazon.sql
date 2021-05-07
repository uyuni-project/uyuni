insert into rhnPackageProvider (id, name)
    (select sequence_nextval('rhn_package_provider_id_seq'), 'AlmaLinux' from dual
    where not exists (select 1 from rhnPackageProvider where name = 'AlmaLinux'));

-- Alma Linux 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '488fcf7c3abb34f8', lookup_package_key_type('gpg'),
    lookup_package_provider('AlmaLinux') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '488fcf7c3abb34f8'));

insert into rhnPackageProvider (id, name)
    (select sequence_nextval('rhn_package_provider_id_seq'), 'Amazon' from dual
    where not exists (select 1 from rhnPackageProvider where name = 'Amazon'));

-- Amazon Linux 2
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '11cf1f95c87f5b1a', lookup_package_key_type('gpg'),
    lookup_package_provider('Amazon') from dual
    where not exists (select 1 from rhnPackageKey where key_id = '11cf1f95c87f5b1a'));

commit;
