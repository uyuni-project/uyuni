-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert
  into rhnPackageKey (id, key_id, key_type_id, provider_id)
values (sequence_nextval('rhn_pkey_id_seq'),
       '2eb161fa246110c1',
       lookup_package_key_type('gpg'),
       lookup_package_provider('Fedora'));
