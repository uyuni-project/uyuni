insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '5054e4a45a6340b3', lookup_package_key_type('gpg'), lookup_package_provider('Red Hat Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '5054e4a45a6340b3'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'bc4d06a08d8b756f', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = 'bc4d06a08d8b756f'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a7dd07088b4efbe6', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a7dd07088b4efbe6'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8efe1bc4d4ade9c3', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '8efe1bc4d4ade9c3'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '97a636db0bad8ecc', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '97a636db0bad8ecc'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'f74f09bc3fa1d6ce', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'f74f09bc3fa1d6ce'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a1bfc02bd588dc46', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a1bfc02bd588dc46'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '46dfa05c6f5da62b', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '46dfa05c6f5da62b'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '09461c70af5425f7', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '09461c70af5425f7'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '9c214d4065176565', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '9c214d4065176565'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '3c90731ed78c6b69', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '3c90731ed78c6b69'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '35a2f86e29b700a4', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '35a2f86e29b700a4'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8a49eb0325db7ae0', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '8a49eb0325db7ae0'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '177086fab0f9c64f', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '177086fab0f9c64f'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2ae81e8aced7258b', lookup_package_key_type('gpg'), lookup_package_provider('AlmaLinux') from dual where not exists (select 1 from rhnPackageKey where key_id = '2ae81e8aced7258b'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'd36cb86cb86b3716', lookup_package_key_type('gpg'), lookup_package_provider('AlmaLinux') from dual where not exists (select 1 from rhnPackageKey where key_id = 'd36cb86cb86b3716'));

