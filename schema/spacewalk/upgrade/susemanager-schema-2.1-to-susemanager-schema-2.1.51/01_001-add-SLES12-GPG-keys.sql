insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '70af9e8139db7c82', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LINUX Products GmbH') from dual
      where 1 not in (select 1 from rhnPackageKey where key_id = '70af9e8139db7c82'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
    (select sequence_nextval('rhn_pkey_id_seq'), '5eaf444450a3dd1c', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LINUX Products GmbH') from dual
      where 1 not in (select 1 from rhnPackageKey where key_id = '5eaf444450a3dd1c'));
