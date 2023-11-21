-- Amazon Linux 2023
insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), 'e951904ad832c631', lookup_package_key_type('gpg'),
    lookup_package_provider('Amazon') from dual
    where not exists (select 1 from rhnPackageKey where key_id = 'e951904ad832c631'));

commit;
