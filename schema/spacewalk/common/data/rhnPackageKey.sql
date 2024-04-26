--
-- Copyright (c) 2008--2017 Red Hat, Inc.
-- Copyright (c) 2010--2021 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--
--

-- Red Hat
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '5326810137017186', lookup_package_key_type('gpg'), lookup_package_provider('Red Hat Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '5326810137017186'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '219180cddb42a60e', lookup_package_key_type('gpg'), lookup_package_provider('Red Hat Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '219180cddb42a60e'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '5054e4a45a6340b3', lookup_package_key_type('gpg'), lookup_package_provider('Red Hat Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '5054e4a45a6340b3'));

-- RHEL 6
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '199e2f91fd431d51', lookup_package_key_type('gpg'), lookup_package_provider('Red Hat Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '199e2f91fd431d51'));

-- Fedora
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b44269d04f2a6fd2', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b44269d04f2a6fd2'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '1dc5c758d22e77f2', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '1dc5c758d22e77f2'));

-- Fedora 12
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '9d1cc34857bbccba', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '9d1cc34857bbccba'));
-- Fedora 13
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '7edc6ad6e8e40fde', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '7edc6ad6e8e40fde'));
-- Fedora 14
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '421caddb97a1071f', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '421caddb97a1071f'));
-- Fedora 15
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b4ebf579069c8460', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b4ebf579069c8460'));
-- Fedora 16
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '067f00b6a82ba4b7', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '067f00b6a82ba4b7'));
-- Fedora 17
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '50e94c991aca3465', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '50e94c991aca3465'));
-- Fedora 18
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '0983129322b3b81a', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '0983129322b3b81a'));
-- Fedora 19
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '07477e65fb4b18e6', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '07477e65fb4b18e6'));
-- Fedora 20
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2eb161fa246110c1', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '2eb161fa246110c1'));
-- Fedora 20 (secondary)
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'dbeae2e4efe550f5', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'dbeae2e4efe550f5'));
-- Fedora 21
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '89ad4e8795a43f54', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '89ad4e8795a43f54'));
-- Fedora 21 (secondary)
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '636dea19a0a7badb', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '636dea19a0a7badb'));
-- Fedora 22
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '11adc0948e1431d5', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '11adc0948e1431d5'));
-- Fedora 22 (secondary)
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'd8d1fa8ca29cb19c', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'd8d1fa8ca29cb19c'));
-- Fedora 23
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '32474cf834ec9cba', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '32474cf834ec9cba'));
-- Fedora 23 (secondary)
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b4bb871c873529b8', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b4bb871c873529b8'));
-- Fedora 24
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '73bde98381b46521', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = '73bde98381b46521'));
-- Fedora 24 (secondary)
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b8635eeb030d5aed', lookup_package_key_type('gpg'), lookup_package_provider('Fedora') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b8635eeb030d5aed'));
-- Fedora 25
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), 'FDB19C98', lookup_package_key_type('gpg'), lookup_package_provider('Fedora'));
-- Fedora 26
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '64DAB85D', lookup_package_key_type('gpg'), lookup_package_provider('Fedora'));
-- Fedora 27
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), 'F5282EE4', lookup_package_key_type('gpg'), lookup_package_provider('Fedora'));
-- Fedora 28
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '9DB62FB1', lookup_package_key_type('gpg'), lookup_package_provider('Fedora'));
-- Fedora 29
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) values
(sequence_nextval('rhn_pkey_id_seq'), '429476B4', lookup_package_key_type('gpg'), lookup_package_provider('Fedora'));

-- CentOS
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a8a447dce8562897', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a8a447dce8562897'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2802e89216ff0e46', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '2802e89216ff0e46'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a53d0bab443e1821', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a53d0bab443e1821'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '7049e44d025e513b', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '7049e44d025e513b'));
-- CentOS 6
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '0946fca2c105b9de', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '0946fca2c105b9de'));
-- CentOS 7
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '24c6a8a7f4a80eb5', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '24c6a8a7f4a80eb5'));
-- CentOS-Official
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '05b555b38483c65d', lookup_package_key_type('gpg'), lookup_package_provider('CentOS') from dual where not exists (select 1 from rhnPackageKey where key_id = '05b555b38483c65d'));

-- Scientific Linux
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '25dbef78a7048f8d', lookup_package_key_type('gpg'), lookup_package_provider('Scientific Linux') from dual where not exists (select 1 from rhnPackageKey where key_id = '25dbef78a7048f8d'));
-- Scientific Linux 6
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '915d75e09b1fd350', lookup_package_key_type('gpg'), lookup_package_provider('Scientific Linux') from dual where not exists (select 1 from rhnPackageKey where key_id = '915d75e09b1fd350'));
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b0b4183f192a7d7d', lookup_package_key_type('gpg'), lookup_package_provider('Scientific Linux') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b0b4183f192a7d7d'));


-- Oracle EL
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '66ced3de1e5e0159', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '66ced3de1e5e0159'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2e2bcdbcb38a8516', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '2e2bcdbcb38a8516'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '72f97b74ec551f03', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '72f97b74ec551f03'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '82562ea9ad986da3', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '82562ea9ad986da3'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'bc4d06a08d8b756f', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = 'bc4d06a08d8b756f'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a7dd07088b4efbe6', lookup_package_key_type('gpg'), lookup_package_provider('Oracle Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a7dd07088b4efbe6'));

-- Novell
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2afe16421d061a62', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '2afe16421d061a62'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '14c28bc97e2e3b05', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '14c28bc97e2e3b05'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '478a32e8a1912208', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '478a32e8a1912208'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '73d25d630dfb3188', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '73d25d630dfb3188'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '044adaee04881839', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '044adaee04881839'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '57da9a6804a29db0', lookup_package_key_type('gpg'), lookup_package_provider('Novell Inc.') from dual where not exists (select 1 from rhnPackageKey where key_id = '57da9a6804a29db0'));

-- SUSE
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a84edae89c800aca', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a84edae89c800aca'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'e3a5c360307e3d54', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'e3a5c360307e3d54'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '6c74ce73b37b98a9', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '6c74ce73b37b98a9'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '70af9e8139db7c82', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '70af9e8139db7c82'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '5eaf444450a3dd1c', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '5eaf444450a3dd1c'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8efe1bc4d4ade9c3', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '8efe1bc4d4ade9c3'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '97a636db0bad8ecc', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '97a636db0bad8ecc'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'f74f09bc3fa1d6ce', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'f74f09bc3fa1d6ce'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'a1bfc02bd588dc46', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = 'a1bfc02bd588dc46'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '46dfa05c6f5da62b', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '46dfa05c6f5da62b'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '09461c70af5425f7', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '09461c70af5425f7'));

-- openSUSE

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'b88b2fd43dbdc284', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = 'b88b2fd43dbdc284'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '9c214d4065176565', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '9c214d4065176565'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '3c90731ed78c6b69', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '3c90731ed78c6b69'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '35a2f86e29b700a4', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '35a2f86e29b700a4'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8a49eb0325db7ae0', lookup_package_key_type('gpg'), lookup_package_provider('openSUSE') from dual where not exists (select 1 from rhnPackageKey where key_id = '8a49eb0325db7ae0'));

-- Liberty / SLES-ES

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8055f0400182b964', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '8055f0400182b964'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '177086fab0f9c64f', lookup_package_key_type('gpg'), lookup_package_provider('SUSE LLC') from dual where not exists (select 1 from rhnPackageKey where key_id = '177086fab0f9c64f'));


-- Spacewalk
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '95423d4e430a1c35', lookup_package_key_type('gpg'), lookup_package_provider('Spacewalk') from dual where not exists (select 1 from rhnPackageKey where key_id = '95423d4e430a1c35'));
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'ed635379b3892132', lookup_package_key_type('gpg'), lookup_package_provider('Spacewalk') from dual where not exists (select 1 from rhnPackageKey where key_id = 'ed635379b3892132'));
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '0e646f68863a853d', lookup_package_key_type('gpg'), lookup_package_provider('Spacewalk') from dual where not exists (select 1 from rhnPackageKey where key_id = '0e646f68863a853d'));
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '41605346066e5810', lookup_package_key_type('gpg'), lookup_package_provider('Spacewalk') from dual where not exists (select 1 from rhnPackageKey where key_id = '41605346066e5810'));
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'dcc981cdb8002de1', lookup_package_key_type('gpg'), lookup_package_provider('Spacewalk') from dual where not exists (select 1 from rhnPackageKey where key_id = 'dcc981cdb8002de1'));


-- EPEL 5
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '119cc036217521f6', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '119cc036217521f6'));
-- EPEL 6
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '3b49df2a0608b895', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '3b49df2a0608b895'));
-- EPEL 7
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '6a2faea2352c64e5', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '6a2faea2352c64e5'));
-- EPEL 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '21ea45ab2f86d6a1', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '21ea45ab2f86d6a1'));
-- EPEL 9
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '8a3872bf3228467c', lookup_package_key_type('gpg'), lookup_package_provider('EPEL') from dual where not exists (select 1 from rhnPackageKey where key_id = '8a3872bf3228467c'));

-- Aliyun Linux 2.1903
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'eb801c41873141a8', lookup_package_key_type('gpg'), lookup_package_provider('Alibaba') from dual where not exists (select 1 from rhnPackageKey where key_id = 'eb801c41873141a8'));

-- Uyuni
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '972e5d6c0d20833e', lookup_package_key_type('gpg'), lookup_package_provider('Uyuni') from dual where not exists (select 1 from rhnPackageKey where key_id = '972e5d6c0d20833e'));

-- Alma Linux 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '488fcf7c3abb34f8', lookup_package_key_type('gpg'), lookup_package_provider('AlmaLinux') from dual where not exists (select 1 from rhnPackageKey where key_id = '488fcf7c3abb34f8'));

insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '2ae81e8aced7258b', lookup_package_key_type('gpg'), lookup_package_provider('AlmaLinux') from dual where not exists (select 1 from rhnPackageKey where key_id = '2ae81e8aced7258b'));

-- Alma Linux 9
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'd36cb86cb86b3716', lookup_package_key_type('gpg'), lookup_package_provider('AlmaLinux') from dual where not exists (select 1 from rhnPackageKey where key_id = 'd36cb86cb86b3716'));


-- Amazon Linux 2
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '11cf1f95c87f5b1a', lookup_package_key_type('gpg'), lookup_package_provider('Amazon') from dual where not exists (select 1 from rhnPackageKey where key_id = '11cf1f95c87f5b1a'));

-- Amazon Linux 2023
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), 'e951904ad832c631', lookup_package_key_type('gpg'), lookup_package_provider('Amazon') from dual where not exists (select 1 from rhnPackageKey where key_id = 'e951904ad832c631'));

-- Rocky Linux 8
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '15af5dac6d745a60', lookup_package_key_type('gpg'), lookup_package_provider('Rocky Linux') from dual where not exists (select 1 from rhnPackageKey where key_id = '15af5dac6d745a60'));
-- Rocky Linux 9
insert into rhnPackageKey (id, key_id, key_type_id, provider_id) (select sequence_nextval('rhn_pkey_id_seq'), '702d426d350d275d', lookup_package_key_type('gpg'), lookup_package_provider('Rocky Linux') from dual where not exists (select 1 from rhnPackageKey where key_id = '702d426d350d275d'));

commit;

