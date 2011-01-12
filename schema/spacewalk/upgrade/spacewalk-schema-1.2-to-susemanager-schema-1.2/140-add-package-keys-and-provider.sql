--
-- Copyright (c) 2010 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

update rhnPackageProvider set name = 'SUSE LINUX Products GmbH' where name = 'Suse';

insert into rhnPackageProvider (id, name) values
(sequence_nextval('rhn_package_provider_id_seq'), 'Novell Inc.' );

commit;

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), 'e3a5c360307e3d54', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LINUX Products GmbH'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '6c74ce73b37b98a9', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LINUX Products GmbH'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '2afe16421d061a62', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '14c28bc97e2e3b05', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '478a32e8a1912208', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '73d25d630dfb3188', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '8055f0400182b964', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.'));

commit;


