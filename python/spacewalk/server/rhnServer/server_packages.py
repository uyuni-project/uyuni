#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2017 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#
# This file contains classes and functions that save and retrieve package
# profiles.
#


from spacewalk.server import rhnSQL


# pylint: disable-next=invalid-name
def processPackageKeyAssociations(header, checksum_type, checksum):
    provider_sql = rhnSQL.prepare("""
        insert into rhnPackageKeyAssociation
            (package_id, key_id) values
            (:package_id, :key_id)
    """)

    insert_keyid_sql = rhnSQL.prepare("""
        insert into rhnPackagekey
            (id, key_id, key_type_id) values
            (sequence_nextval('rhn_pkey_id_seq'), :key_id, :key_type_id)
            on conflict do nothing
    """)

    lookup_keyid_sql = rhnSQL.prepare("""
       select pk.id
         from rhnPackagekey pk
        where pk.key_id = :key_id
    """)

    lookup_keytype_id = rhnSQL.prepare("""
       select id
         from rhnPackageKeyType
        where LABEL in ('gpg', 'pgp', 'rsa')
    """)

    lookup_pkgid_sql = rhnSQL.prepare("""
        select p.id
          from rhnPackage p,
               rhnChecksumView c
         where c.checksum = :csum
           and c.checksum_type = :ctype
           and p.checksum_id = c.id
    """)

    lookup_pkgkey_sql = rhnSQL.prepare("""
        select 1
          from rhnPackageKeyAssociation
         where package_id = :package_id
           and key_id = :key_id
    """)

    lookup_pkgid_sql.execute(ctype=checksum_type, csum=checksum)
    pkg_ids = lookup_pkgid_sql.fetchall_dict()

    if not pkg_ids:
        # No package to associate, continue with next
        return

    sigkeys = header.signatures
    key_id = None  # _key_ids(sigkeys)[0]
    for sig in sigkeys:
        if sig["signature_type"] in ("gpg", "pgp", "rsa"):
            key_id = sig["key_id"]

    if not key_id:
        # package is not signed, skip gpg key insertion
        return

    lookup_keyid_sql.execute(key_id=key_id)
    keyid = lookup_keyid_sql.fetchall_dict()

    if not keyid:
        lookup_keytype_id.execute()
        key_type_id = lookup_keytype_id.fetchone_dict()
        insert_keyid_sql.execute(key_id=key_id, key_type_id=key_type_id["id"])
        lookup_keyid_sql.execute(key_id=key_id)
        keyid = lookup_keyid_sql.fetchall_dict()

    for pkg_id in pkg_ids:
        lookup_pkgkey_sql.execute(key_id=keyid[0]["id"], package_id=pkg_id["id"])
        exists_check = lookup_pkgkey_sql.fetchall_dict()

        if not exists_check:
            provider_sql.execute(key_id=keyid[0]["id"], package_id=pkg_id["id"])
