update rhnPackageProvider set name = 'SUSE LLC' where name = 'SUSE LINUX Products GmbH';

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '044adaee04881839', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '044adaee04881839'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '57da9a6804a29db0', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '57da9a6804a29db0'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '05b555b38483c65d', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '05b555b38483c65d'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '82562ea9ad986da3', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '82562ea9ad986da3'));
