insert into rhnPackageKey (id, key_id, key_type_id, provider_id)
  select sequence_nextval('rhn_pkey_id_seq'), '429476B4', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual
   where not exists (select 1 from rhnPackageKey where key_id = '429476B4');

