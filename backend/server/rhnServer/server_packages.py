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

import string
import sys
import time
from spacewalk.common.usix import DictType

from spacewalk.common.usix import raise_with_tb
from spacewalk.common import rhn_rpm
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.rhnException import rhnFault
from spacewalk.server import rhnSQL, rhnAction
from .server_lib import snapshot_server, check_entitlement

UNCHANGED = 0
ADDED = 1
DELETED = 2
UPDATED = 3


class dbPackage:

    """ A small class that helps us represent things about a
        database package. In this structure "real" means that we have an
        entry in the database for it.
    """

    def __init__(self, pdict, real=0, name_id=None, evr_id=None,
                 package_arch_id=None):
        if type(pdict) != DictType:
            return None
        if ('arch' not in pdict) or (pdict['arch'] is None):
            pdict['arch'] = ""
        if string.lower(str(pdict['epoch'])) == "(none)" or pdict['epoch'] == "" or pdict['epoch'] is None:
            pdict['epoch'] = None
        else:
            pdict['epoch'] = str(pdict['epoch'])
        for k in ('name', 'version', 'release', 'arch'):
            if pdict[k] is None:
                return None
        self.n = str(pdict['name'])
        self.v = str(pdict['version'])
        self.r = str(pdict['release'])
        self.e = pdict['epoch']
        self.a = str(pdict['arch'])
        if 'installtime' in pdict:
            self.installtime = pdict['installtime']
        else:
            self.installtime = None
        # nvrea is a tuple; we can use tuple as dictionary keys since they are
        # immutable
        self.nvrea = (self.n, self.v, self.r, self.e, self.a)
        self.real = real
        self.name_id = name_id
        self.evr_id = evr_id
        self.package_arch_id = package_arch_id
        if real:
            self.status = UNCHANGED
        else:
            self.status = ADDED

    def setval(self, value):
        self.status = value

    def add(self):
        if self.status == DELETED:
            if self.real:
                self.status = UNCHANGED  # real entries remain unchanged
            else:
                self.status = ADDED  # others are added
        return

    def delete(self):
        if self.real:
            self.status = DELETED
        else:
            self.status = UNCHANGED  # we prefer unchanged for the non-real packages
        return

    def __str__(self):
        return "server.rhnServer.dbPackage instance %s" % {
            'n': self.n,
            'v': self.v,
            'r': self.r,
            'e': self.e,
            'a': self.a,
            'installtime': self.installtime,
            'real': self.real,
            'name_id': self.name_id,
            'evr_id': self.evr_id,
            'package_arch_id': self.package_arch_id,
            'status': self.status,
        }
    __repr__ = __str__


class Packages:

    def __init__(self):
        self.__p = {}
        # Have we loaded the packages or not?
        self.__loaded = 0
        self.__changed = 0

    def add_package(self, sysid, entry):
        log_debug(4, sysid, entry)
        p = dbPackage(entry)
        if p is None:
            # Not a valid package spec
            return -1
        if not self.__loaded:
            self.reload_packages_byid(sysid)
        if p.nvrea in self.__p:
            if self.__p[p.nvrea].installtime != p.installtime:
                self.__p[p.nvrea].installtime = p.installtime
                self.__p[p.nvrea].status = UPDATED
            else:
                self.__p[p.nvrea].add()
            self.__changed = 1
            return 0
        self.__p[p.nvrea] = p
        self.__changed = 1
        return 0

    def delete_package(self, sysid, entry):
        """ delete a package from the list """
        log_debug(4, sysid, entry)
        p = dbPackage(entry)
        if p is None:
            # Not a valid package spec
            return -1
        if not self.__loaded:
            self.reload_packages_byid(sysid)
        if p.nvrea in self.__p:
            log_debug(4, "  Package deleted")
            self.__p[p.nvrea].delete()
            self.__changed = 1
        # deletion is always successfull
        return 0

    def dispose_packages(self, sysid):
        """ delete all packages and get an empty package list """
        log_debug(4, sysid)
        if not self.__loaded:
            self.reload_packages_byid(sysid)
        for k in list(self.__p.keys()):
            self.__p[k].delete()
            self.__changed = 1
        return 0

    def get_packages(self):
        """ produce a list of packages """
        return [a.nvrea for a in [a for a in list(self.__p.values()) if a.status != DELETED]]

    def __expand_installtime(self, installtime):
        """ Simulating the ternary operator, one liner is ugly """
        if installtime:
            return time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(installtime))
        else:
            return None

    def save_packages_byid(self, sysid, schedule=1):
        """ save the package list """
        log_debug(3, sysid, "Errata cache to run:", schedule,
                  "Changed:", self.__changed, "%d total packages" % len(self.__p))

        if not self.__changed:
            return 0

        commits = 0

        # get rid of the deleted packages
        dlist = [a for a in list(self.__p.values()) if a.real and a.status in (DELETED, UPDATED)]
        if dlist:
            log_debug(4, sysid, len(dlist), "deleted packages")
            h = rhnSQL.prepare("""
            delete from rhnServerPackage
            where server_id = :sysid
            and name_id = :name_id
            and evr_id = :evr_id
            and ((:package_arch_id is null and package_arch_id is null)
                or package_arch_id = :package_arch_id)
            """)
            h.execute_bulk({
                'sysid': [sysid] * len(dlist),
                'name_id': [a.name_id for a in dlist],
                'evr_id': [a.evr_id for a in dlist],
                'package_arch_id': [a.package_arch_id for a in dlist],
            })
            commits = commits + len(dlist)
            del dlist

        # And now add packages
        alist = [a for a in list(self.__p.values()) if a.status in (ADDED, UPDATED)]
        if alist:
            log_debug(4, sysid, len(alist), "added packages")
            h = rhnSQL.prepare("""
            insert into rhnServerPackage
            (server_id, name_id, evr_id, package_arch_id, installtime)
            values (:sysid, LOOKUP_PACKAGE_NAME(:n), LOOKUP_EVR(:e, :v, :r),
                LOOKUP_PACKAGE_ARCH(:a), TO_TIMESTAMP(:instime, 'YYYY-MM-DD HH24:MI:SS')
            )
            """)
            # some fields are not allowed to contain empty string (varchar)

            def lambdaae(a):
                if a.e == '':
                    return None
                else:
                    return a.e
            package_data = {
                'sysid': [sysid] * len(alist),
                'n': [a.n for a in alist],
                'v': [a.v for a in alist],
                'r': [a.r for a in alist],
                'e': list(map(lambdaae, alist)),
                'a': [a.a for a in alist],
                'instime': [self.__expand_installtime(a.installtime) for a in alist],
            }
            try:
                h.execute_bulk(package_data)
                rhnSQL.commit()
            except rhnSQL.SQLSchemaError:
                e = sys.exc_info()[1]
                # LOOKUP_PACKAGE_ARCH failed
                if e.errno == 20243:
                    log_debug(2, "Unknown package arch found", e)
                    raise_with_tb(rhnFault(45, "Unknown package arch found"), sys.exc_info()[2])

            commits = commits + len(alist)
            del alist

        if schedule:
            # queue this server for an errata update
            update_errata_cache(sysid)

        # if provisioning box, and there was an actual delta, snapshot
        ents = check_entitlement(sysid)
        if commits and "enterprise_entitled" in ents:
            snapshot_server(sysid, "Package profile changed")

        # Our new state does not reflect what's on the database anymore
        self.__loaded = 0
        self.__changed = 0
        return 0

    _query_get_package_arches = rhnSQL.Statement("""
        select id, label
          from rhnPackageArch
    """)

    def get_package_arches(self):
        # None gets automatically converted to empty string
        package_arches_hash = {None: ''}
        h = rhnSQL.prepare(self._query_get_package_arches)
        h.execute()
        while 1:
            row = h.fetchone_dict()
            if not row:
                break
            package_arches_hash[row['id']] = row['label']
        return package_arches_hash

    def reload_packages_byid(self, sysid):
        """ reload the packages list from the database """
        log_debug(3, sysid)
        # First, get the package arches
        package_arches_hash = self.get_package_arches()
        # XXX we could achieve the same thing with an outer join but that's
        # more expensive
        # Now load packages
        h = rhnSQL.prepare("""
        select
            rpn.name,
            rpe.version,
            rpe.release,
            rpe.epoch,
            sp.name_id,
            sp.evr_id,
            sp.package_arch_id,
            TO_CHAR(sp.installtime, 'YYYY-MM-DD HH24:MI:SS') installtime
        from
            rhnServerPackage sp,
            rhnPackageName rpn,
            rhnPackageEVR rpe
        where sp.server_id = :sysid
        and sp.name_id = rpn.id
        and sp.evr_id = rpe.id
        """)
        h.execute(sysid=sysid)
        self.__p = {}
        while 1:
            t = h.fetchone_dict()
            if not t:
                break
            t['arch'] = package_arches_hash[t['package_arch_id']]
            if 'installtime' in t and t['installtime'] is not None:
                t['installtime'] = time.mktime(time.strptime(t['installtime'],
                                                             "%Y-%m-%d %H:%M:%S"))
            p = dbPackage(t, real=1, name_id=t['name_id'], evr_id=t['evr_id'],
                          package_arch_id=t['package_arch_id'])
            self.__p[p.nvrea] = p
        log_debug(4, "Loaded %d packages for server %s" % (len(self.__p), sysid))
        self.__loaded = 1
        self.__changed = 0
        return 0

    _query_product_packages = rhnSQL.Statement("""
    select rp.name_id, rp.package_arch_id arch_id, X.name
      from
        (
          select pn.name,
                 latest.name_id,
                 lookup_evr((latest.evr).epoch, (latest.evr).version, (latest.evr).release) AS evr_id,
                 latest.arch_label AS ARCH,
                 latest.arch_id
            from
               (
                SELECT p.name_id AS name_id,
                       max(pe.evr) AS evr,
                       pa.label AS arch_label,
                       pa.id AS arch_id
                  FROM
                       rhnPackageEVR pe
                  JOIN rhnPackage p ON p.evr_id = pe.id
                  JOIN rhnChannelPackage cp ON cp.package_id = p.id
                  JOIN rhnPackageArch pa ON pa.id = p.package_arch_id
                  JOIN rhnPackageProvides pv on p.id = pv.package_id
                  JOIN rhnPackageCapability c on pv.capability_id = c.id
                  JOIN rhnServerChannel sc on cp.channel_id = sc.channel_id
                 WHERE
                       sc.server_id = :server_id
                   and c.name = 'product()'
              GROUP BY p.name_id, pa.label, pa.id
         ) latest
         JOIN rhnPackageName pn ON pn.id = latest.name_id
        where pn.name not like '%-migration'
          and NOT EXISTS (
                 SELECT 1
                   FROM rhnServerPackage SP
                  WHERE SP.server_id = :server_id
                    AND SP.name_id = latest.name_id
                    AND (SP.package_arch_id = latest.arch_id OR SP.package_arch_id IS NULL)
          )
          and NOT EXISTS (
                 select 1
                   FROM rhnServerPackage SP
                   JOIN rhnPackage p_p ON SP.name_id = p_p.name_id and SP.evr_id = p_p.evr_id and SP.package_arch_id = p_p.package_arch_id
                   JOIN rhnPackageProvides p_pv on p_p.id = p_pv.package_id
                   JOIN rhnPackageCapability p_c on p_pv.capability_id = p_c.id
                  WHERE SP.server_id = :server_id
                    and p_c.name = pn.name
              )
        ) X
        JOIN rhnPackage rp ON rp.name_id = X.name_id
             AND rp.evr_id = X.evr_id
             AND rp.package_arch_id = X.arch_id
    """)

    def install_missing_product_packages(self):
        '''
        Find missing products and schedule an action to install them
        '''
        h = rhnSQL.prepare(self._query_product_packages)
        package_names = {}
        h.execute(server_id=self.server['id'])
        while True:
            row = h.fetchone_dict()
            if not row:
                break
            pn_id = row['name_id']
            pa_id = row['arch_id']
            package_names[(pn_id, pa_id)] = row['name']

        if not package_names:
            return None

        package_arch_ids = list(package_names.keys())

        action_id = rhnAction.schedule_server_packages_update_by_arch(self.server['id'],
                                                                      package_arch_ids,
                                                                      org_id=self.server['org_id'],
                                                                      action_name="Product Package Auto-Install")
        for p in list(package_names.values()):
            log_debug(1, "Scheduled for install:  '%s'" % p)

        rhnSQL.commit()

        return action_id


def update_errata_cache(server_id):
    """ Queue an update the the server's errata cache. This queues for
        Taskomatic instead of doing it in-line because updating many servers
        at once was problematic and lead to unresponsive Satellite and
        incorrectly reporting failed actions when they did not fail (see
        bz 1119460).
    """
    log_debug(2, "Queueing the errata cache update", server_id)
    update_needed_cache = rhnSQL.Procedure("queue_server")
    update_needed_cache(server_id, 0)


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
    """)

    lookup_keyid_sql = rhnSQL.prepare("""
       select pk.id
         from rhnPackagekey pk
        where pk.key_id = :key_id
    """)

    lookup_keytype_id = rhnSQL.prepare("""
       select id
         from rhnPackageKeyType
        where LABEL in ('gpg', 'pgp')
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
        if sig['signature_type'] in ['gpg', 'pgp']:
            key_id = sig['key_id']

    if not key_id:
        # package is not signed, skip gpg key insertion
        return

    lookup_keyid_sql.execute(key_id=key_id)
    keyid = lookup_keyid_sql.fetchall_dict()

    if not keyid:
        lookup_keytype_id.execute()
        key_type_id = lookup_keytype_id.fetchone_dict()
        insert_keyid_sql.execute(key_id=key_id, key_type_id=key_type_id['id'])
        lookup_keyid_sql.execute(key_id=key_id)
        keyid = lookup_keyid_sql.fetchall_dict()

    for pkg_id in pkg_ids:
        lookup_pkgkey_sql.execute(key_id=keyid[0]['id'],
                                  package_id=pkg_id['id'])
        exists_check = lookup_pkgkey_sql.fetchall_dict()

        if not exists_check:
            provider_sql.execute(key_id=keyid[0]['id'], package_id=pkg_id['id'])


def package_delta(list1, list2):
    """ Compares list1 and list2 (each list is a tuple (n, v, r, e)
        returns two lists
        (install, remove)
        XXX upgrades and downgrades are simulated by a removal and an install
    """
    # Package registry - canonical versions for all packages
    package_registry = {}
    hash1 = _package_list_to_hash(list1, package_registry)
    hash2 = _package_list_to_hash(list2, package_registry)
    del package_registry

    installs = []
    removes = []
    for pn, ph1 in list(hash1.items()):
        if pn not in hash2:
            removes.extend(list(ph1.keys()))
            continue

        ph2 = hash2[pn]
        del hash2[pn]

        # Now, compute the differences between ph1 and ph2
        for p in list(ph1.keys()):
            if p not in ph2:
                # We have to remove it
                removes.append(p)
            else:
                del ph2[p]
        # Everything else left in ph2 has to be installed
        installs.extend(list(ph2.keys()))

    # Whatever else is left in hash2 should be installed
    for ph2 in list(hash2.values()):
        installs.extend(list(ph2.keys()))

    installs.sort()
    removes.sort()
    return installs, removes


def _package_list_to_hash(package_list, package_registry):
    """ Converts package_list into a hash keyed by name
        package_registry contains the canonical version of the package
        for instance, version 51 and 0051 are indentical, but that would break the
        list comparison in Python. package_registry is storing representatives for
        each equivalence class (where the equivalence relationship is rpm's version
        comparison algorigthm
        Side effect: Modifies second argument!
    """
    hash = {}
    for e in package_list:
        e = tuple(e)
        pn = e[0]
        if pn not in package_registry:
            # Definitely new equivalence class
            _add_to_hash(package_registry, pn, e)
            _add_to_hash(hash, pn, e)
            continue

        # Look for a match for this package name in the registry
        plist = list(package_registry[pn].keys())
        for p in plist:
            if rhn_rpm.nvre_compare(p, e) == 0:
                # Packages are identical
                e = p
                break
        else:
            # Package not found in the global registry - add it
            _add_to_hash(package_registry, pn, e)

        # Add it to the hash too
        _add_to_hash(hash, pn, e)

    return hash


def _add_to_hash(hash, key, value):
    if key not in hash:
        hash[key] = {value: None}
    else:
        hash[key][value] = None
