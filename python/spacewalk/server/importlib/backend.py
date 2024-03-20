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
# Generic DB backend
#

import copy
from datetime import datetime

# pylint: disable-next=unused-import
import string
import sys

from uyuni.common.usix import raise_with_tb
from uyuni.common import rhn_rpm
from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.common.rhnLog import log_debug
from spacewalk.satellite_tools import syncLib
from spacewalk.server import rhnSQL, rhnChannel, taskomatic
from .importLib import (
    Diff,
    Package,
    IncompletePackage,
    Erratum,
    AlreadyUploadedError,
    InvalidPackageError,
    TransactionError,
    SourcePackage,
)
from .backendLib import (
    TableCollection,
    sanitizeValue,
    TableDelete,
    TableUpdate,
    TableLookup,
    addHash,
    TableInsert,
)


sequences = {
    "rhnPackageCapability": "rhn_pkg_capability_id_seq",
    "rhnPackage": "rhn_package_id_seq",
    "rhnSourceRPM": "rhn_sourcerpm_id_seq",
    "rhnPackageGroup": "rhn_package_group_id_seq",
    "rhnErrata": "rhn_errata_id_seq",
    "rhnChannel": "rhn_channel_id_seq",
    "rhnChannelProduct": "rhn_channelprod_id_seq",
    "rhnPackageSource": "rhn_package_source_id_seq",
    "rhnChannelFamily": "rhn_channel_family_id_seq",
    "rhnCVE": "rhn_cve_id_seq",
    "rhnChannelArch": "rhn_channel_arch_id_seq",
    "rhnPackageArch": "rhn_package_arch_id_seq",
    "rhnServerArch": "rhn_server_arch_id_seq",
    "rhnCPUArch": "rhn_cpu_arch_id_seq",
    "rhnErrataFile": "rhn_erratafile_id_seq",
    "rhnKickstartableTree": "rhn_kstree_id_seq",
    "rhnArchType": "rhn_archtype_id_seq",
    "rhnPackageChangeLogRec": "rhn_pkg_cl_id_seq",
    "rhnPackageChangeLogData": "rhn_pkg_cld_id_seq",
    "rhnContentSource": "rhn_chan_content_src_id_seq",
    "suseProductFile": "suse_prod_file_id_seq",
    "suseMdKeyword": "suse_mdkeyword_id_seq",
    "suseEula": "suse_eula_id_seq",
    "suseProducts": "suse_products_id_seq",
    "suseSCCRepository": "suse_sccrepository_id_seq",
    "suseProductSCCRepository": "suse_prdrepo_id_seq",
}


# pylint: disable-next=missing-class-docstring
class Backend:
    # This object is initialized by the specific subclasses (e.g.
    # OracleBackend)
    tables = TableCollection()
    # TODO: Some reason why we're passing a module in here? Seems to
    # always be rhnSQL anyhow...

    def __init__(self, dbmodule):
        self.dbmodule = dbmodule
        self.sequences = {}

    # TODO: Why is there a pseudo-constructor here instead of just using
    # __init__?
    def init(self):
        # Initializes the database connection objects
        # This function has to be called on a newly defined Backend object
        # Initialize sequences
        for k, v in list(sequences.items()):
            self.sequences[k] = self.dbmodule.Sequence(v)
        # TODO: Why do we return a reference to ourselves? If somebody called
        # this method they already have a reference...
        return self

    # pylint: disable-next=invalid-name,redefined-builtin
    def setDateFormat(self, format):
        # pylint: disable-next=consider-using-f-string
        sth = self.dbmodule.prepare("alter session set nls_date_format ='%s'" % format)
        sth.execute()
        sth = self.dbmodule.prepare(
            # pylint: disable-next=consider-using-f-string
            "alter session set nls_timestamp_format ='%s'"
            % format
        )
        sth.execute()

    # pylint: disable-next=invalid-name
    def processCapabilities(self, capabilityHash):
        if not capabilityHash:
            return

        sql = """
            WITH
              wanted (ordering, name, version) AS (
                VALUES %s
              ),
              missing AS (
                SELECT nextval('rhn_pkg_capability_id_seq') AS id, wanted.*
                  FROM wanted
                  LEFT JOIN rhnPackageCapability
                    ON rhnPackageCapability.name = wanted.name
                      AND rhnPackageCapability.version IS NOT DISTINCT FROM wanted.version
                  WHERE rhnPackageCapability.id IS NULL
              )
              INSERT INTO rhnPackageCapability(id, name, version)
                SELECT id, name, version
                  FROM missing
                  ORDER BY ordering
                ON CONFLICT DO NOTHING
        """
        sorted_capabilities = sorted(
            capabilityHash.keys(), key=lambda k: (k[0], k[1] or "")
        )
        wanted = [
            (i, key[0], None if key[1] == "" else key[1])
            for i, key in enumerate(sorted_capabilities)
        ]
        h = self.dbmodule.prepare(sql)
        h.execute_values(sql, wanted, fetch=False)

        sql = """
            WITH
              wanted (ordering, name, version) AS (
                VALUES %s
              )
              SELECT wanted.name, wanted.version, rhnPackageCapability.id
                FROM wanted
                JOIN rhnPackageCapability
                  ON rhnPackageCapability.name = wanted.name
                    AND rhnPackageCapability.version IS NOT DISTINCT FROM wanted.version
        """
        h = self.dbmodule.prepare(sql)
        capabilities = h.execute_values(sql, wanted)

        for capability in capabilities:
            capabilityHash[(capability[0], capability[1] or "")] = capability[2]

    # pylint: disable-next=invalid-name
    def processChangeLog(self, changelogHash):
        if (
            CFG.has_key("package_import_skip_changelog")
            and CFG.package_import_skip_changelog
        ):
            return

        if not changelogHash:
            return

        sql = """
            WITH wanted (ordering, name, time, text) AS (
              VALUES %s
            ),
            missing AS (
              SELECT nextval('rhn_pkg_cld_id_seq') AS id, wanted.*
                FROM wanted
                LEFT JOIN rhnPackageChangeLogData
                    ON rhnPackageChangeLogData.name = wanted.name
                      AND rhnPackageChangeLogData.time = wanted.time
                      AND rhnPackageChangeLogData.text = wanted.text
                WHERE rhnPackageChangeLogData.id IS NULL
            )
            INSERT INTO rhnPackageChangeLogData(id, name, time, text)
              SELECT id, name, time, text
                FROM missing
                ORDER BY ordering
              ON CONFLICT DO NOTHING
        """
        values = [
            (i, key[0], datetime.strptime(key[1], "%Y-%m-%d %H:%M:%S"), key[2])
            for i, key in enumerate(sorted(changelogHash.keys()))
        ]
        h = self.dbmodule.prepare(sql)
        h.execute_values(sql, values, fetch=False)

        sql = """
            WITH wanted (ordering, name, time, text) AS (
              VALUES %s
            )
            SELECT wanted.name, wanted.time, wanted.text, rhnPackageChangeLogData.id
              FROM wanted
              JOIN rhnPackageChangeLogData
                  ON rhnPackageChangeLogData.name = wanted.name
                    AND rhnPackageChangeLogData.time = wanted.time
                    AND rhnPackageChangeLogData.text = wanted.text
        """
        h = self.dbmodule.prepare(sql)
        changelogs = h.execute_values(sql, values)

        for changelog in changelogs:
            changelogHash[
                (changelog[0], changelog[1].strftime("%Y-%m-%d %H:%M:%S"), changelog[2])
            ] = changelog[3]

    # pylint: disable-next=invalid-name
    def processSuseProductFiles(self, prodfileHash):
        sql = """
            SELECT spf.id
              FROM suseProductFile spf
             WHERE spf.name = :name
               AND spf.evr_id = :evr_id
               AND spf.package_arch_id = :package_arch_id
               AND spf.vendor = :vendor
               AND spf.summary = :summary
               AND spf.description = :description
        """
        h = self.dbmodule.prepare(sql)
        toinsert = [[], [], [], [], [], [], []]
        for name, evr_id, package_arch_id, vendor, summary, description in list(
            prodfileHash.keys()
        ):
            val = {}
            _buildExternalValue(
                val,
                {
                    "name": name,
                    "evr_id": evr_id,
                    "package_arch_id": package_arch_id,
                    "vendor": vendor,
                    "summary": summary,
                    "description": description,
                },
                self.tables["suseProductFile"],
            )
            h.execute(
                name=val["name"],
                evr_id=val["evr_id"],
                package_arch_id=val["package_arch_id"],
                vendor=val["vendor"],
                summary=val["summary"],
                description=val["description"],
            )
            row = h.fetchone_dict()
            if row:
                prodfileHash[
                    (name, evr_id, package_arch_id, vendor, summary, description)
                ] = row["id"]
                continue

            # pylint: disable-next=redefined-builtin
            id = self.sequences["suseProductFile"].next()
            prodfileHash[
                (name, evr_id, package_arch_id, vendor, summary, description)
            ] = id

            toinsert[0].append(id)
            toinsert[1].append(val["name"])
            toinsert[2].append(val["evr_id"])
            toinsert[3].append(val["package_arch_id"])
            toinsert[4].append(val["vendor"])
            toinsert[5].append(val["summary"])
            toinsert[6].append(val["description"])

        if not toinsert[0]:
            # Nothing to do
            return

        sql = """
            INSERT INTO suseProductFile (id, name, evr_id, package_arch_id, vendor, summary, description)
            VALUES (:id, :name, :evr_id, :package_arch_id, :vendor, :summary, :description)"""
        h = self.dbmodule.prepare(sql)
        h.executemany(
            id=toinsert[0],
            name=toinsert[1],
            evr_id=toinsert[2],
            package_arch_id=toinsert[3],
            vendor=toinsert[4],
            summary=toinsert[5],
            description=toinsert[6],
        )

    # pylint: disable-next=invalid-name
    def processSuseEulas(self, eulaHash):
        query_lookup = """
            SELECT id
              FROM suseEula
             WHERE checksum = :checksum
        """
        h_lookup = self.dbmodule.prepare(query_lookup)

        query_insert = """
            INSERT INTO suseEula (id, text, checksum)
            VALUES (:id, :text, :checksum)"""
        h_insert = self.dbmodule.prepare(query_insert, blob_map={"text": "text"})

        for text, checksum in list(eulaHash.keys()):
            val = {}
            _buildExternalValue(
                val, {"text": text, "checksum": checksum}, self.tables["suseEula"]
            )
            h_lookup.execute(checksum=val["checksum"])
            row = h_lookup.fetchone_dict()
            if row:
                eulaHash[(text, checksum)] = row["id"]
                continue

            # pylint: disable-next=redefined-builtin
            id = self.sequences["suseEula"].next()
            eulaHash[(text, checksum)] = id
            h_insert.execute(id=id, text=str(val["text"]), checksum=val["checksum"])

    # pylint: disable-next=invalid-name
    def processCVEs(self, cveHash):
        # First figure out which CVE's are already inserted
        sql = "select id from rhnCVE where name = :name"
        h = self.dbmodule.prepare(sql)
        toinsert = [[], []]

        for cve_name in list(cveHash.keys()):
            h.execute(name=cve_name)
            row = h.fetchone_dict()

            if row:
                cveHash[cve_name] = row["id"]
                continue

            # Generate an id
            # pylint: disable-next=redefined-builtin
            id = self.sequences["rhnCVE"].next()

            cveHash[cve_name] = id

            toinsert[0].append(id)
            toinsert[1].append(cve_name)

        if not toinsert[0]:
            # Nothing to do
            return

        sql = "insert into rhnCVE (id, name) values (:id, :name)"
        h = self.dbmodule.prepare(sql)
        h.executemany(id=toinsert[0], name=toinsert[1])

    # pylint: disable-next=invalid-name
    def processExtraTags(self, extraTags):
        if not extraTags:
            return
        sql = """
            WITH wanted (ordering, name) AS (
              VALUES %s
            ),
            missing AS (
              SELECT nextval('rhn_package_extra_tags_keys_id_seq') AS id, wanted.*
                FROM wanted
                    LEFT JOIN rhnPackageExtraTagKey ON rhnPackageExtraTagKey.name = wanted.name
               WHERE rhnPackageExtraTagKey.id IS NULL
            )
            INSERT INTO rhnPackageExtraTagKey (id, name)
                SELECT id, name from missing
                ORDER BY ordering
                ON CONFLICT DO NOTHING
        """
        values = [(i, key) for i, key in enumerate(sorted(extraTags.keys()))]
        if not values:
            return
        h = self.dbmodule.prepare(sql)
        # pylint: disable-next=unused-variable
        r = h.execute_values(sql, values, fetch=False)

        sql = """
            WITH wanted (ordering, name) AS (
              VALUES %s
            )
            SELECT rhnPackageExtraTagKey.name, rhnPackageExtraTagKey.id
              FROM wanted
              JOIN rhnPackageExtraTagKey ON rhnPackageExtraTagKey.name = wanted.name
        """
        h = self.dbmodule.prepare(sql)
        tags = h.execute_values(sql, values)
        for tag in tags:
            extraTags[tag[0]] = tag[1]

    # pylint: disable-next=invalid-name,redefined-builtin
    def lookupErrataFileTypes(self, hash):
        hash.clear()
        h = self.dbmodule.prepare("select id, label from rhnErrataFileType")
        h.execute()
        while 1:
            row = h.fetchone_dict()
            if not row:
                break
            hash[row["label"]] = row["id"]
        return hash

    # pylint: disable-next=invalid-name
    def __lookupArches(self, archHash, table):
        if not archHash:
            return

        # pylint: disable-next=consider-using-f-string
        sql = "select id from %s where label = :name" % table
        h = self.dbmodule.prepare(sql)
        for k in list(archHash.keys()):
            h.execute(name=str(k))
            row = h.fetchone_dict()
            if row:
                archHash[k] = row["id"]
            # Else, it's an unsupported architecture

    # pylint: disable-next=invalid-name
    def lookupChannelArches(self, archHash):
        return self.__lookupArches(archHash, "rhnChannelArch")

    # pylint: disable-next=invalid-name
    def lookupPackageArches(self, archHash):
        return self.__lookupArches(archHash, "rhnPackageArch")

    # pylint: disable-next=invalid-name
    def lookupServerArches(self, archHash):
        return self.__lookupArches(archHash, "rhnServerArch")

    # pylint: disable-next=invalid-name
    def lookupArchTypes(self, arch_types_hash):
        h = self.dbmodule.prepare(
            "select id, name from rhnArchType where label = :label"
        )
        seq = self.sequences["rhnArchType"]
        updates = [[], []]
        inserts = [[], [], []]
        results = {}
        for label, name in list(arch_types_hash.items()):
            h.execute(label=label)
            row = h.fetchone_dict()
            if not row:
                next_id = seq.next()
                inserts[0].append(next_id)
                inserts[1].append(label)
                inserts[2].append(name)
                results[label] = next_id
                continue
            aid = row["id"]
            results[label] = aid
            if name == row["name"]:
                # Nothing to do
                continue
            updates[0].append(aid)
            updates[1].append(name)
        if inserts[0]:
            h = self.dbmodule.prepare(
                """
                    insert into rhnArchType (id, label, name)
                    values (:id, :label, :name)
            """
            )
            h.executemany(id=inserts[0], label=inserts[1], name=inserts[2])
        if updates[0]:
            h = self.dbmodule.prepare(
                """
                    update rhnArchType
                       set name = :name
                     where id = :id
            """
            )
            h.executemany(id=updates[0], name=updates[1])

        # Finally, update the hash
        arch_types_hash.update(results)

    # pylint: disable-next=invalid-name
    def lookupPackageArchType(self, pkg_arch_id):
        h = self.dbmodule.prepare(
            """
                select at.label
                  from rhnPackageArch pa
                  join rhnArchType at on pa.arch_type_id = at.id
                 where pa.id = :pkg_arch_id"""
        )
        h.execute(pkg_arch_id=pkg_arch_id)
        row = h.fetchone_dict()
        if row:
            return row["label"]
        return None

    # pylint: disable-next=invalid-name
    def _lookupOrg(self):
        # Returns the org id
        sql = "select min(id) as id from web_customer"
        h = self.dbmodule.prepare(sql)
        h.execute()
        rows = h.fetchall_dict()
        if not rows:
            raise ValueError("No user is created")
        return rows[0]["id"]

    # pylint: disable-next=invalid-name
    def lookupOrg(self, org_name=None):
        if not org_name:
            return self._lookupOrg()
        # Returns id of the org if found, None otherwise
        sql = "select id from web_customer where name = :name"
        h = self.dbmodule.prepare(sql)
        h.execute(name=org_name)
        row = h.fetchone_dict()
        if not row:
            return None
        return row["id"]

    # pylint: disable-next=invalid-name
    def lookupMaster(self, master_label):
        # Returns the master record (if it exists)
        sql = "select * from rhnISSMaster where label = :label"
        h = self.dbmodule.prepare(sql)
        h.execute(label=master_label)
        return h.fetchone_dict()

    # pylint: disable-next=invalid-name
    def createMaster(self, master_label):
        # Creates a master record with label master_label
        sql = """
            insert into rhnISSMaster (id, label)
            values (sequence_nextval('rhn_issmaster_seq'), :label)
        """
        h = self.dbmodule.prepare(sql)
        h.execute(label=master_label)

    # pylint: disable-next=invalid-name
    def createMasterOrgs(self, master, orgs):
        # Create master org records
        insert = [[], [], []]
        for org in orgs:
            insert[0].append(master)
            insert[1].append(org["id"])
            insert[2].append(org["name"])
        sql = """
            insert into rhnISSMasterOrgs
                   (id, master_id, master_org_id, master_org_name)
            values (sequence_nextval('rhn_issmasterorgs_seq'),
                   (select id from rhnISSMaster where label = :label),
                   :id, :name)
        """
        h = self.dbmodule.prepare(sql)
        h.executemany(label=insert[0], id=insert[1], name=insert[2])

    # pylint: disable-next=invalid-name
    def createOrgs(self, orgs):
        # Create local org records
        sql = """
            insert into web_customer (id, name)
            values (sequence_nextval('web_customer_id_seq'), :name)
        """
        h = self.dbmodule.prepare(sql)
        h.executemany(name=orgs)
        sql = "select id, name from web_customer"
        h = self.dbmodule.prepare(sql)
        h.execute()
        rows = h.fetchall_dict()
        ret = {}
        for row in rows:
            ret[row["name"]] = row["id"]
        return ret

    # pylint: disable-next=invalid-name
    def updateMasterOrgs(self, master_orgs):
        # Update the master org to local org mapping
        insert = [[], []]
        for org in master_orgs:
            insert[0].append(org["master_id"])
            insert[1].append(org["local_id"])
        sql = """
            update rhnISSMasterOrgs
               set local_org_id=:local
             where master_org_id=:master
        """
        h = self.dbmodule.prepare(sql)
        h.executemany(master=insert[0], local=insert[1])

    # pylint: disable-next=invalid-name
    def lookupOrgTrusts(self):
        # Return a hash of org trusts
        sql = "select org_id, org_trust_id from rhnTrustedOrgs"
        h = self.dbmodule.prepare(sql)
        h.execute()
        rows = h.fetchall_dict()
        ret = {}
        if rows:
            for row in rows:
                # pylint: disable-next=consider-iterating-dictionary
                if row["org_id"] not in list(ret.keys()):
                    ret[row["org_id"]] = []
                ret[row["org_id"]].append(row["org_trust_id"])
        return ret

    # pylint: disable-next=invalid-name
    def clearOrgTrusts(self, org_id):
        # Delete all trusts involving this org; trusts are always
        # bi-directional
        sql = """
        delete from rhnTrustedOrgs
              where org_id = :org_id
                 or org_trust_id = :org_id
        """
        h = self.dbmodule.prepare(sql)
        h.execute(org_id=org_id)

    # pylint: disable-next=invalid-name
    def createOrgTrusts(self, trusts):
        # Create org trusts
        insert = [[], []]
        for trust in trusts:
            insert[0].append(trust["org_id"])
            insert[1].append(trust["trust"])
        sql = """
            insert into rhnTrustedOrgs (org_id, org_trust_id)
            values (:id, :trust)
        """
        h = self.dbmodule.prepare(sql)
        h.executemany(id=insert[0], trust=insert[1])

    # pylint: disable-next=invalid-name
    def lookupOrgMap(self, master_label):
        sql = """
            select imo.master_org_id, imo.master_org_name, imo.local_org_id
              from rhnISSMasterOrgs imo,
                   rhnISSMaster im
             where im.id = imo.master_id
               and im.label = :master_label
        """
        h = self.dbmodule.prepare(sql)
        h.execute(master_label=master_label)
        rows = h.fetchall_dict()
        maps = {"master-name-to-master-id": {}, "master-id-to-local-id": {}}
        if not rows:
            return maps
        mn_to_mi = {}  # master org name to master org id map
        mi_to_li = {}  # master org id to local org id map
        for org in rows:
            if (
                "master_org_id" in list(org.keys())
                and "master_org_name" in list(org.keys())
                and org["master_org_id"]
                and org["master_org_name"]
            ):
                mn_to_mi[org["master_org_name"]] = org["master_org_id"]
            if (
                "master_org_id" in list(org.keys())
                and "local_org_id" in list(org.keys())
                and org["master_org_id"]
                and org["local_org_id"]
            ):
                mi_to_li[org["master_org_id"]] = org["local_org_id"]
        maps["master-name-to-master-id"] = mn_to_mi
        maps["master-id-to-local-id"] = mi_to_li
        return maps

    # pylint: disable-next=invalid-name,redefined-builtin
    def lookupChannels(self, hash):
        if not hash:
            return
        sql = "select id, channel_arch_id from rhnChannel where label = :label"
        h = self.dbmodule.prepare(sql)
        for k in list(hash.keys()):
            h.execute(label=k)
            row = h.fetchone_dict()
            if row:
                hash[k] = row
            # Else, it's an unsupported channel

    # pylint: disable-next=invalid-name
    def lookupChannelPackageArchCompat(self, channelArchHash):
        # Return all the arches compatible with each key of archHash
        sql = """
            select package_arch_id
            from rhnChannelPackageArchCompat
            where channel_arch_id = :channel_arch_id
        """
        h = self.dbmodule.prepare(sql)
        for channel_arch_id in list(channelArchHash.keys()):
            # pylint: disable-next=redefined-builtin
            dict = {}
            h.execute(channel_arch_id=channel_arch_id)
            while 1:
                row = h.fetchone_dict()
                if not row:
                    break
                dict[row["package_arch_id"]] = None
            channelArchHash[channel_arch_id] = dict

    # pylint: disable-next=invalid-name
    def lookupServerGroupTypes(self, entries_hash):
        sql = """
            select id
              from rhnServerGroupType
             where label = :label
        """
        h = self.dbmodule.prepare(sql)
        for sgt in list(entries_hash.keys()):
            h.execute(label=sgt)
            row = h.fetchone_dict()
            if not row:
                # server group not found
                continue
            entries_hash[sgt] = row["id"]

    # pylint: disable-next=invalid-name
    def lookupPackageNames(self, nameHash):
        if not nameHash:
            return
        sql = "select LOOKUP_PACKAGE_NAME(:name) id from dual"
        h = self.dbmodule.prepare(sql)
        for k in sorted(nameHash.keys()):
            h.execute(name=k)
            nameHash[k] = h.fetchone_dict()["id"]

    # pylint: disable-next=invalid-name
    def lookupErratum(self, erratum):
        if not erratum:
            return None

        sql = """
            select advisory
              from rhnErrata
             where advisory_name = :advisory_name
        """
        h = self.dbmodule.prepare(sql)
        h.execute(advisory_name=erratum["advisory_name"])
        return h.fetchone_dict()

    # pylint: disable-next=invalid-name
    def lookupErrataSeverityId(self, erratum):
        """
        for the given severity type retuns the id
        associated in the rhnErratSeverity table.
        """
        if not erratum:
            return None

        sql = """
            select id
              from rhnErrataSeverity
             where label = :severity
        """

        h = self.dbmodule.prepare(sql)

        if "security_impact" in erratum and erratum["security_impact"]:
            # concatenate the severity to reflect the db
            # bz-204374: rhnErrataSeverity tbl has lower case severity values,
            # so we convert severity in errata hash to lower case to lookup.
            severity_label = "errata.sev.label." + erratum["security_impact"].lower()
        elif "severity" in erratum and erratum["severity"]:
            severity_label = erratum["severity"]
        else:
            return None

        h.execute(severity=severity_label)
        row = h.fetchone_dict()

        if not row:
            # pylint: disable-next=consider-using-f-string
            log_debug(2, "Invalid severity: %s. Returning None." % severity_label)
            return None

        return row["id"]

    # pylint: disable-next=invalid-name
    def lookupEVRs(self, evrHash, ptype):
        sql = "select LOOKUP_EVR(:epoch, :version, :release, :ptype) id from dual"
        h = self.dbmodule.prepare(sql)
        for evr in sorted(evrHash.keys(), key=lambda k: (str(k[0] or 0), k[1], k[2])):
            epoch, version, release = evr
            if epoch == "" or epoch is None:
                epoch = None
            else:
                epoch = str(epoch)
            h.execute(epoch=epoch, version=version, release=release, ptype=ptype)
            row = h.fetchone_dict()
            if row:
                evrHash[evr] = row["id"]

    # pylint: disable-next=invalid-name
    def lookupChecksums(self, checksumHash):
        if not checksumHash:
            return

        sql = """
            WITH wanted (ordering, checksum_type, checksum) AS (
              VALUES %s
            ),
            missing AS (
              SELECT nextval('rhnchecksum_seq') AS id, rhnChecksumType.id AS checksum_type_id, wanted.*
                FROM wanted
                  JOIN rhnChecksumType ON wanted.checksum_type = rhnChecksumType.label
                  LEFT JOIN rhnChecksum
                    ON rhnChecksum.checksum_type_id = rhnChecksumType.id
                      AND rhnChecksum.checksum = wanted.checksum
                WHERE rhnChecksum.id IS NULL
            )
            INSERT INTO rhnChecksum(id, checksum_type_id, checksum)
              SELECT id, checksum_type_id, checksum
                FROM missing
                ORDER BY ordering
              ON CONFLICT DO NOTHING
        """
        sorted_checksums = sorted(checksumHash.keys(), key=lambda k: (k[0], k[1] or ""))
        values = [
            (i, key[0], key[1])
            for i, key in enumerate(sorted_checksums)
            if key[1] != ""
        ]

        if not values:
            return

        h = self.dbmodule.prepare(sql)
        # pylint: disable-next=unused-variable
        r = h.execute_values(sql, values, fetch=False)

        sql = """
        WITH wanted (ordering, checksum_type, checksum) AS (
            VALUES %s
          )
          SELECT wanted.checksum_type, wanted.checksum, rhnChecksum.id
            FROM wanted
              JOIN rhnChecksumType ON wanted.checksum_type = rhnChecksumType.label
              JOIN rhnChecksum
                ON rhnChecksum.checksum_type_id = rhnChecksumType.id
                  AND rhnChecksum.checksum = wanted.checksum
        """
        h = self.dbmodule.prepare(sql)
        checksums = h.execute_values(sql, values)

        for checksum in checksums:
            checksumHash[(checksum[0], checksum[1])] = checksum[2]

    # pylint: disable-next=invalid-name
    def lookupChecksumTypes(self, checksumTypeHash):
        if not checksumTypeHash:
            return
        sql = "select id from rhnChecksumType where label = :label"
        h = self.dbmodule.prepare(sql)
        for l in list(checksumTypeHash.keys()):
            h.execute(label=l)
            row = h.fetchone_dict()
            if row:
                checksumTypeHash[l] = row["id"]

    # pylint: disable-next=invalid-name
    def lookupPackageNEVRAs(self, nevraHash):
        sql = "select LOOKUP_PACKAGE_NEVRA(:name, :evr, :arch) id from dual"
        h = self.dbmodule.prepare(sql)
        for nevra in nevraHash:
            name, evr, arch = nevra
            if arch is None:
                arch = ""
            h.execute(name=name, evr=evr, arch=arch)
            row = h.fetchone_dict()
            if row:
                nevraHash[nevra] = row["id"]

    # pylint: disable-next=invalid-name
    def lookupPackagesByNEVRA(self, nevraHash):
        sql = """
              select id from rhnPackage
              where name_id = :name and
                    evr_id = :evr and
                    package_arch_id = :arch
              """
        h = self.dbmodule.prepare(sql)

        for nevra in nevraHash:
            name, evr, arch = nevra
            h.execute(name=name, evr=evr, arch=arch)
            row = h.fetchone_dict()
            if row:
                nevraHash[nevra] = row["id"]

    # pylint: disable-next=invalid-name
    def lookupPackageKeyId(self, header):
        lookup_keyid_sql = rhnSQL.prepare(
            """
           select pk.id
             from rhnPackagekey pk,
                  rhnPackageKeyType pkt,
                  rhnPackageProvider pp
            where pk.key_id = :key_id
              and pk.key_type_id = pkt.id
              and pk.provider_id = pp.id
        """
        )
        sigkeys = rhn_rpm.RPM_Header(header).signatures
        key_id = None  # _key_ids(sigkeys)[0]
        for sig in sigkeys:
            if sig["signature_type"] in ["gpg", "pgp", "rsa"]:
                key_id = sig["key_id"]

        lookup_keyid_sql.execute(key_id=key_id)
        keyid = lookup_keyid_sql.fetchall_dict()

        return keyid[0]["id"]

    # pylint: disable-next=invalid-name,redefined-builtin
    def lookupSourceRPMs(self, hash):
        self.__processHash("lookup_source_name", hash)

    # pylint: disable-next=invalid-name,redefined-builtin
    def lookupPackageGroups(self, hash):
        self.__processHash("lookup_package_group", hash)

    # pylint: disable-next=invalid-name
    def lookupPackages(self, packages, checksums, ignore_missing=0):
        # If nevra is enabled use checksum as primary key
        self.validate_pks()
        for package in packages:
            if not isinstance(package, IncompletePackage):
                raise TypeError(
                    # pylint: disable-next=consider-using-f-string
                    "Expected an IncompletePackage instance, found %s"
                    # pylint: disable-next=used-before-assignment
                    % str(type(package))
                )
        for package in packages:
            # here we need to figure out which checksum we have in the database
            not_found = None
            # pylint: disable-next=redefined-builtin
            for type, chksum in list(package["checksums"].items()):
                package["checksum_type"] = type
                package["checksum"] = chksum
                package["checksum_id"] = checksums[(type, chksum)]
                try:
                    self.__lookupObjectCollection([package], "rhnPackage")
                    not_found = None
                    break
                except InvalidPackageError:
                    e = sys.exc_info()[1]
                    not_found = (e, sys.exc_info()[2])
            if not_found and not ignore_missing:
                # package is not in database at all
                raise_with_tb(not_found[0], not_found[1])

    # pylint: disable-next=invalid-name,redefined-builtin
    def lookupChannelFamilies(self, hash):
        if not hash:
            return
        sql = "select id from rhnChannelFamily where label = :label"
        h = self.dbmodule.prepare(sql)
        for k in list(hash.keys()):
            h.execute(label=k)
            row = h.fetchone_dict()
            if row:
                hash[k] = row["id"]
            # Else, it's an unsupported channel

    # pylint: disable-next=redefined-builtin
    def lookup_kstree_types(self, hash):
        return self._lookup_in_table("rhnKSTreeType", "rhn_kstree_type_seq", hash)

    # pylint: disable-next=redefined-builtin
    def lookup_ks_install_types(self, hash):
        return self._lookup_in_table(
            "rhnKSInstallType", "rhn_ksinstalltype_id_seq", hash
        )

    # pylint: disable-next=redefined-builtin
    def _lookup_in_table(self, table_name, sequence_name, hash):
        t = self.dbmodule.Table(table_name, "label")
        seq = self.dbmodule.Sequence(sequence_name)
        to_insert = []
        to_update = []
        result = {}
        for label, name in list(hash.items()):
            row = t[label]
            if not row:
                row_id = seq.next()
                result[label] = row_id
                to_insert.append((label, name, row_id))
                continue
            row_id = row["id"]
            result[label] = row_id
            if row["name"] != name:
                to_update.append((label, name))
                continue
            # Entry found in the table - nothing more to do

        if to_insert:
            # Have to insert rows
            row_ids = []
            labels = []
            names = []
            for label, name, row_id in to_insert:
                row_ids.append(row_id)
                labels.append(label)
                names.append(name)

            sql = """
                insert into %s (id, label, name) values (:id, :label, :name)
            """
            h = self.dbmodule.prepare(sql % table_name)
            h.executemany(id=row_ids, label=labels, name=names)

        if to_update:
            labels = []
            names = []
            for label, name in to_update:
                labels.append(label)
                names.append(name)

            sql = """
                update %s set name = :name where label = :label
            """
            h = self.dbmodule.prepare(sql % table_name)
            h.executemany(label=labels, name=names)

        # Update the returning value
        hash.clear()
        hash.update(result)
        return hash

    # pylint: disable-next=invalid-name
    def processChannelArches(self, arches):
        self.__processObjectCollection(
            arches, "rhnChannelArch", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processPackageArches(self, arches):
        self.__processObjectCollection(
            arches, "rhnPackageArch", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processServerArches(self, arches):
        self.__processObjectCollection(
            arches, "rhnServerArch", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processCPUArches(self, arches):
        self.__processObjectCollection(
            arches, "rhnCPUArch", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processMasterOrgs(self, orgs):
        self.__processObjectCollection(
            orgs, "rhnISSMasterOrgs", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processOrgs(self, orgs):
        self.__processObjectCollection(
            orgs, "web_customer", uploadForce=4, ignoreUploaded=1, severityLimit=4
        )

    # pylint: disable-next=invalid-name
    def processServerPackageArchCompatMap(self, entries):
        self.__populateTable("rhnServerPackageArchCompat", entries, delete_extra=1)

    # pylint: disable-next=invalid-name
    def processServerChannelArchCompatMap(self, entries):
        self.__populateTable("rhnServerChannelArchCompat", entries, delete_extra=1)

    # pylint: disable-next=invalid-name
    def processChannelPackageArchCompatMap(self, entries):
        self.__populateTable("rhnChannelPackageArchCompat", entries, delete_extra=1)

    # pylint: disable-next=invalid-name
    def processServerGroupServerArchCompatMap(self, entries):
        self.__populateTable("rhnServerServerGroupArchCompat", entries, delete_extra=1)

    # pylint: disable-next=invalid-name
    def processPackages(
        self,
        packages,
        # pylint: disable-next=invalid-name
        uploadForce=0,
        # pylint: disable-next=invalid-name
        ignoreUploaded=0,
        # pylint: disable-next=invalid-name
        forceVerify=0,
        transactional=0,
    ):
        # Insert/update the packages
        self.validate_pks()

        # pylint: disable-next=invalid-name
        childTables = {
            "rhnPackageProvides": "package_id",
            "rhnPackageRequires": "package_id",
            "rhnPackageConflicts": "package_id",
            "rhnPackageObsoletes": "package_id",
            "rhnPackageRecommends": "package_id",
            "rhnPackageSuggests": "package_id",
            "rhnPackageSupplements": "package_id",
            "rhnPackageEnhances": "package_id",
            "rhnPackageBreaks": "package_id",
            "rhnPackagePredepends": "package_id",
            "rhnPackageFile": "package_id",
            "rhnPackageChangeLogRec": "package_id",
            "susePackageProductFile": "package_id",
            "susePackageEula": "package_id",
            "rhnPackageExtraTag": "package_id",
        }

        if (
            CFG.has_key("package_import_skip_changelog")
            and CFG.package_import_skip_changelog
        ):
            del childTables["rhnPackageChangeLogRec"]

        for package in packages:
            if not isinstance(package, Package):
                raise TypeError("Expected a Package instance")

            # pylint: disable-next=invalid-name
            tableList = copy.deepcopy(childTables)

            # older sat packages wont have these fields
            # avoid Null insertions
            if package["header_start"] is None:
                package["header_start"] = -1
                package["header_end"] = -1

            try:
                self.__processObjectCollection__(
                    [
                        package,
                    ],
                    "rhnPackage",
                    tableList,
                    uploadForce=uploadForce,
                    forceVerify=forceVerify,
                    ignoreUploaded=ignoreUploaded,
                    severityLimit=1,
                    transactional=transactional,
                )
            except Exception as e:
                syncLib.log(
                    0,
                    # pylint: disable-next=consider-using-f-string
                    "Error during processing package %s-%s-%s:%s.%s.\n%s"
                    % (
                        package["name"],
                        package["version"],
                        package["release"],
                        package["epoch"],
                        package["arch"],
                        str(e),
                    ),
                )
                raise

    # pylint: disable-next=invalid-name
    def processErrata(self, errata):
        # Insert/update the packages

        # pylint: disable-next=invalid-name
        childTables = [
            "rhnChannelErrata",
            "rhnErrataBugList",
            "rhnErrataFile",
            "rhnErrataKeyword",
            "rhnErrataPackage",
            "rhnErrataCVE",
        ]

        for erratum in errata:
            if not isinstance(erratum, Erratum):
                raise TypeError("Expected an Erratum instance")

        return self.__processObjectCollection(
            errata,
            "rhnErrata",
            childTables,
            "errata_id",
            uploadForce=4,
            ignoreUploaded=1,
            forceVerify=1,
            transactional=1,
        )

    def update_channels_affected_by_errata(self, dml):
        # identify errata that were affected
        affected_errata_ids = {}
        for op_type in ["insert", "update", "delete"]:
            op_values = getattr(dml, op_type)
            for table_name, values_hash in list(op_values.items()):
                if table_name == "rhnErrata":
                    field = "id"
                elif "errata_id" in values_hash:
                    field = "errata_id"

                # Now we know in which field to look for changes
                for erratum_id in values_hash[field]:
                    affected_errata_ids[erratum_id] = None

        # Get affected channels
        affected_channel_ids = {}
        h = self.dbmodule.prepare(
            """
            select channel_id
              from rhnChannelErrata
             where errata_id = :errata_id
        """
        )
        for errata_id in list(affected_errata_ids.keys()):
            h.execute(errata_id=errata_id)

            channel_ids = h.fetchall_dict() or []
            channel_ids = [x["channel_id"] for x in channel_ids]
            for channel_id in channel_ids:
                affected_channel_ids[channel_id] = errata_id

        # Now update the channels
        update_channel = self.dbmodule.Procedure("rhn_channel.update_channel")
        invalidate_ss = 0

        for channel_id in list(affected_channel_ids.keys()):
            update_channel(channel_id, invalidate_ss)
            h = self.dbmodule.prepare(
                """
                select advisory from rhnErrata where id = :errata_id
            """
            )
            h.execute(errata_id=affected_channel_ids[channel_id])
            advisory = h.fetchone()[0]

            channel = rhnChannel.Channel()
            channel.load_by_id(channel_id)
            taskomatic.add_to_repodata_queue(channel.get_label(), "errata", advisory)

    # pylint: disable-next=invalid-name
    def processKickstartTrees(self, ks_trees):
        # pylint: disable-next=invalid-name
        childTables = [
            "rhnKSTreeFile",
            #'rhnKSTreeType',
            #'rhnKSInstallType',
        ]
        self.__processObjectCollection(
            ks_trees,
            "rhnKickstartableTree",
            childTables,
            "kstree_id",
            uploadForce=4,
            forceVerify=1,
            ignoreUploaded=1,
            severityLimit=1,
            transactional=1,
        )

    def queue_errata(self, errata, timeout=0):
        # timeout is the numer of seconds we want the execution to be delayed
        if not errata:
            return
        # Figure out the errata ids
        errata_channel_ids = []
        for erratum in errata:
            if erratum.ignored:
                # Skip it
                continue
            if erratum.diff_result is not None:
                if erratum.diff_result.level != 0:
                    # New or modified in some way, queue it
                    # XXX we may not want to do this for trivial changes,
                    # but not sure what trivial is
                    for cid in erratum["channels"]:
                        errata_channel_ids.append((erratum.id, cid["channel_id"]))

        if not errata_channel_ids:
            # Nothing to do
            return

        hdel = self.dbmodule.prepare(
            """
            delete from rhnErrataQueue where errata_id = :errata_id
        """
        )

        h = self.dbmodule.prepare(
            """
            insert into rhnErrataQueue (errata_id, channel_id, next_action)
            values (:errata_id, :channel_id, current_timestamp + numtodsinterval(:timeout, 'second'))
        """
        )
        errata_ids = [x[0] for x in errata_channel_ids]
        channel_ids = [x[1] for x in errata_channel_ids]
        timeouts = [timeout] * len(errata_ids)
        hdel.executemany(errata_id=errata_ids)
        h.executemany(errata_id=errata_ids, channel_id=channel_ids, timeout=timeouts)

    # pylint: disable-next=invalid-name
    def processChannels(self, channels, base_channels):
        # pylint: disable-next=invalid-name
        childTables = [
            "rhnChannelFamilyMembers",
            "rhnReleaseChannelMap",
        ]
        if base_channels:
            childTables.append("rhnDistChannelMap")
        self.__processObjectCollection(
            channels,
            "rhnChannel",
            childTables,
            "channel_id",
            uploadForce=4,
            ignoreUploaded=1,
            forceVerify=1,
        )

    # pylint: disable-next=invalid-name
    def orgTrustExists(self, org_id, trust_id):
        sql = """
        select *
          from rhnTrustedOrgs
         where org_id = :org_id
           and org_trust_id = :trust_id
        """
        h = self.dbmodule.prepare(sql)
        h.execute(org_id=org_id, trust_id=trust_id)
        row = h.fetchone_dict()
        if row:
            return True
        return False

    # pylint: disable-next=invalid-name
    def clearChannelTrusts(self, label):
        sql = """
        delete from rhnChannelTrust where channel_id =
        (select id from rhnChannel where label = :label)
        """
        h = self.dbmodule.prepare(sql)
        h.execute(label=label)

    # pylint: disable-next=invalid-name
    def processChannelTrusts(self, channel_trusts):
        # Create channel trusts
        insert = [[], []]
        for trust in channel_trusts:
            insert[0].append(trust["channel-label"])
            insert[1].append(trust["org-id"])
        sql = """
            insert into rhnChannelTrust (channel_id, org_trust_id)
            values ((select id from rhnChannel where label = :label),
                    :org_id)
        """
        h = self.dbmodule.prepare(sql)
        h.executemany(label=insert[0], org_id=insert[1])

    # pylint: disable-next=invalid-name
    def processChannelFamilies(self, channels):
        # pylint: disable-next=invalid-name
        childTables = []
        self.__processObjectCollection(
            channels,
            "rhnChannelFamily",
            childTables,
            "channel_family_id",
            uploadForce=4,
            ignoreUploaded=1,
            forceVerify=1,
        )

    # pylint: disable-next=invalid-name
    def processChannelFamilyMembers(self, channel_families):
        # Channel families now contain channel memberships too
        h_lookup_cfid = self.dbmodule.prepare(
            """
            select channel_family_id
              from rhnChannelFamilyMembers
             where channel_id = :channel_id
        """
        )
        cf_ids = []
        c_ids = []
        for cf in channel_families:
            if "private-channel-family" in cf["label"]:
                # Its a private channel family and channel family members
                # will be different from server as this is most likely ISS
                # sync. Don't compare and delete custom channel families.
                continue
            for cid in cf["channel_ids"]:
                # Look up channel families for this channel
                h_lookup_cfid.execute(channel_id=cid)
                row = h_lookup_cfid.fetchone_dict()
                if row and row["channel_family_id"] == cf.id:
                    # Nothing to do here, we already have this mapping
                    continue
                # need to delete this entry and add the one for the new
                # channel family
                cf_ids.append(cf.id)
                c_ids.append(cid)
        if not c_ids:
            # We're done
            return

        hdel = self.dbmodule.prepare(
            """
            delete from rhnChannelFamilyMembers
             where channel_id = :channel_id
        """
        )
        hins = self.dbmodule.prepare(
            """
            insert into rhnChannelFamilyMembers (channel_id, channel_family_id)
            values (:channel_id, :channel_family_id)
        """
        )
        hdel.executemany(channel_id=c_ids)
        hins.executemany(channel_family_id=cf_ids, channel_id=c_ids)

    # pylint: disable-next=invalid-name
    def processChannelFamilyPermissions(self, channel_families):
        # Since this is not evaluated in rhn_entitlements anymore,
        # make channel families without org globally visible

        cf_ids = [
            cf.id
            for cf in channel_families
            if "private-channel-family" not in cf["label"]
        ]

        h_public_sel = self.dbmodule.prepare(
            """
            select channel_family_id from rhnPublicChannelFamily
        """
        )
        h_public_sel.execute()

        public_cf_in_db = [
            x["channel_family_id"] for x in h_public_sel.fetchall_dict() or []
        ]
        public_cf_to_insert = [x for x in cf_ids if x not in public_cf_in_db]

        h_public_ins = self.dbmodule.prepare(
            """
            insert into rhnPublicChannelFamily (channel_family_id)
            values (:channel_family_id)
        """
        )
        h_public_ins.executemany(channel_family_id=public_cf_to_insert)

    # pylint: disable-next=invalid-name
    def processDistChannelMap(self, dcms):
        # pylint: disable-next=invalid-name
        dcmTable = self.tables["rhnDistChannelMap"]
        lookup = TableLookup(dcmTable, self.dbmodule)
        dmlobj = DML([dcmTable.name], self.tables)

        for dcm in dcms:
            if dcm.ignored:
                # Skip it
                continue
            h = lookup.query(dcm)
            row = h.fetchone_dict()
            if not row:
                # pylint: disable-next=invalid-name
                extObject = {}
                _buildExternalValue(extObject, dcm, dcmTable)
                addHash(dmlobj.insert[dcmTable.name], extObject)
            # Since this table has all the columns in unique constraints, we
            # don't care much about updates

        self.__doDML(dmlobj)

    # pylint: disable-next=invalid-name
    def processChannelProduct(self, channel):
        """Associate product with channel"""

        channel["channel_product"] = channel["product_name"]
        channel["channel_product_version"] = channel["product_version"]
        channel["channel_product_beta"] = channel["product_beta"]
        channel["channel_product_id"] = self.lookupChannelProduct(channel)

        if not channel["channel_product_id"]:
            # If no channel product dont update
            return
        statement = self.dbmodule.prepare(
            """
            UPDATE rhnChannel
               SET channel_product_id = :channel_product_id
             WHERE id = :id
               AND (channel_product_id is NULL
                OR channel_product_id <> :channel_product_id)
        """
        )

        statement.execute(
            id=channel.id, channel_product_id=channel["channel_product_id"]
        )

    # pylint: disable-next=invalid-name
    def processChannelContentSources(self, channel):
        """Associate content sources with channel"""

        # Which content sources are assigned to this channel
        select_sql = self.dbmodule.prepare(
            """
            select source_id from rhnChannelContentSource
            where channel_id = :channel_id
        """
        )

        select_sql.execute(channel_id=channel.id)
        sources_in_db = [x["source_id"] for x in select_sql.fetchall_dict() or []]

        # Which content sources should be assigned to this channel
        sources_needed = []
        if "content-sources" in channel and channel["content-sources"]:
            for source in channel["content-sources"]:
                sources_needed.append(self.lookupContentSource(source["label"]))

        # What to delete and insert
        sources_to_delete = [x for x in sources_in_db if x not in sources_needed]
        sources_to_insert = [x for x in sources_needed if x not in sources_in_db]

        delete_sql = self.dbmodule.prepare(
            """
            delete from rhnChannelContentSource
            where source_id = :source_id
            and channel_id = :channel_id
        """
        )

        insert_sql = self.dbmodule.prepare(
            """
           insert into rhnChannelContentSource
           (source_id, channel_id)
           values (:source_id, :channel_id)
        """
        )

        for source_id in sources_to_delete:
            delete_sql.execute(source_id=source_id, channel_id=channel.id)

        for source_id in sources_to_insert:
            insert_sql.execute(source_id=source_id, channel_id=channel.id)

    # pylint: disable-next=invalid-name
    def processProductNames(self, batch):
        """Check if ProductName for channel in batch is already in DB.
        If not add it there.
        """
        statement = self.dbmodule.prepare(
            """
            insert into rhnProductName
                 (id, label, name)
              values (sequence_nextval('rhn_productname_id_seq'),
                      :product_label, :product_name)
        """
        )

        for channel in batch:
            if not self.lookupProductNames(channel["label"]):
                statement.execute(
                    product_label=channel["label"], product_name=channel["name"]
                )

    # pylint: disable-next=invalid-name
    def processContentSources(self, batch):
        """Insert content source into DB"""

        # pylint: disable-next=invalid-name
        childTables = ["rhnContentSourceSsl"]
        self.__processObjectCollection(
            batch,
            "rhnContentSource",
            childTables,
            "content_source_id",
            uploadForce=4,
            ignoreUploaded=1,
            forceVerify=1,
        )

    # pylint: disable-next=invalid-name
    def lookupContentSource(self, label):
        """Get id for given content source"""

        sql = self.dbmodule.prepare(
            """
            select id from rhnContentSource where label = :label and org_id is null
        """
        )

        sql.execute(label=label)

        content_source = sql.fetchone_dict()

        if content_source:
            return content_source["id"]

        return

    # pylint: disable-next=invalid-name
    def lookupContentSourceType(self, label):
        """Get id for given content type label"""

        sql = self.dbmodule.prepare(
            """
            select id from rhnContentSourceType where label = :label
        """
        )

        sql.execute(label=label)

        source_type = sql.fetchone_dict()

        if source_type:
            return source_type["id"]

        return

    # pylint: disable-next=invalid-name
    def lookupProductNames(self, label):
        """For given label of product return its id.
        If product do not exist return None
        """
        statement = self.dbmodule.prepare(
            """
            SELECT id
              FROM rhnProductName
             WHERE label = :label
        """
        )

        statement.execute(label=label)

        product = statement.fetchone_dict()

        if product:
            return product["id"]

        return

    # pylint: disable-next=invalid-name
    def processSupportInformation(self, batch):
        """Check if SupportInformation is already in DB.
        If not, add it
        """
        insert_support_info = self.dbmodule.prepare(
            """
            INSERT INTO suseMdData (channel_id, package_id, keyword_id)
            VALUES (:channel_id, :package_id, :keyword_id)
        """
        )
        delete_support_info = self.dbmodule.prepare(
            """
            DELETE FROM suseMdData
            WHERE channel_id = :channel_id
              AND package_id = :package_id
              AND keyword_id = :keyword_id
        """
        )
        # pylint: disable-next=invalid-name
        _query_keywords = self.dbmodule.prepare(
            """
            SELECT channel_id, package_id, keyword_id
              FROM suseMdData
        """
        )
        _query_keywords.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s-%s" % (x["channel_id"], x["package_id"], x["keyword_id"])
            for x in _query_keywords.fetchall_dict() or []
        ]
        toinsert = [[], [], []]
        todelete = [[], [], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s-%s" % (
                item["channel_id"],
                item["package_id"],
                item["keyword_id"],
            )
            if ident in existing_data:
                existing_data.remove(ident)
                continue
            toinsert[0].append(int(item["channel_id"]))
            toinsert[1].append(int(item["package_id"]))
            toinsert[2].append(int(item["keyword_id"]))
        for ident in existing_data:
            cid, pid, kid = ident.split("-")
            todelete[0].append(int(cid))
            todelete[1].append(int(pid))
            todelete[2].append(int(kid))
        if todelete[0]:
            delete_support_info.executemany(
                channel_id=todelete[0], package_id=todelete[1], keyword_id=todelete[2]
            )
        if toinsert[0]:
            insert_support_info.executemany(
                channel_id=toinsert[0], package_id=toinsert[1], keyword_id=toinsert[2]
            )

    # pylint: disable-next=invalid-name
    def processSuseProducts(self, batch):
        """Check if SUSE Product is already in DB.
        If yes, update it, if not add it.
        """
        insert_product = self.dbmodule.prepare(
            """
            INSERT INTO suseProducts (id, name, version, friendly_name, arch_type_id, release, product_id, free, base, release_stage, channel_family_id)
            VALUES (:pid, :name, :version, :friendly_name, :arch_type_id, :release, :product_id, :free, :base, :release_stage, :channel_family_id)
            """
        )
        delete_product = self.dbmodule.prepare(
            """
            DELETE FROM suseProducts WHERE product_id = :product_id
            """
        )
        update_product = self.dbmodule.prepare(
            """
            UPDATE suseProducts
               SET name = :name,
                   version = :version,
                   friendly_name = :friendly_name,
                   arch_type_id = :arch_type_id,
                   release = :release,
                   free = :free,
                   base = :base,
                   release_stage = :release_stage,
                   channel_family_id = :channel_family_id
             WHERE product_id = :product_id
             """
        )
        # pylint: disable-next=invalid-name
        _query_product = self.dbmodule.prepare(
            """
            SELECT product_id FROM suseProducts
            """
        )
        _query_product.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s" % (x["product_id"])
            for x in _query_product.fetchall_dict() or []
        ]
        toinsert = [[], [], [], [], [], [], [], [], [], [], []]
        todelete = [[]]
        toupdate = [[], [], [], [], [], [], [], [], [], []]
        for item in batch:
            if not item["channel_family_id"]:
                continue
            # pylint: disable-next=consider-using-f-string
            ident = "%s" % (item["product_id"])
            if ident in existing_data:
                existing_data.remove(ident)
                toupdate[0].append(item["name"])
                toupdate[1].append(item["version"])
                toupdate[2].append(item["friendly_name"])
                toupdate[3].append(item["arch_type_id"])
                toupdate[4].append(item["release"])
                toupdate[5].append(item["free"])
                toupdate[6].append(item["base"])
                toupdate[7].append(item["release_stage"])
                toupdate[8].append(int(item["channel_family_id"]))
                toupdate[9].append(int(item["product_id"]))
                continue
            toinsert[0].append(self.sequences["suseProducts"].next())
            toinsert[1].append(item["name"])
            toinsert[2].append(item["version"])
            toinsert[3].append(item["friendly_name"])
            toinsert[4].append(item["arch_type_id"])
            toinsert[5].append(item["release"])
            toinsert[6].append(item["free"])
            toinsert[7].append(item["base"])
            toinsert[8].append(item["release_stage"])
            toinsert[9].append(int(item["channel_family_id"]))
            toinsert[10].append(int(item["product_id"]))
        for ident in existing_data:
            todelete[0].append(int(ident))
        if todelete[0]:
            delete_product.executemany(product_id=todelete[0])
        if toinsert[0]:
            insert_product.executemany(
                pid=toinsert[0],
                name=toinsert[1],
                version=toinsert[2],
                friendly_name=toinsert[3],
                arch_type_id=toinsert[4],
                release=toinsert[5],
                free=toinsert[6],
                base=toinsert[7],
                release_stage=toinsert[8],
                channel_family_id=toinsert[9],
                product_id=toinsert[10],
            )
        if toupdate[0]:
            update_product.executemany(
                name=toupdate[0],
                version=toupdate[1],
                friendly_name=toupdate[2],
                arch_type_id=toupdate[3],
                release=toupdate[4],
                free=toupdate[5],
                base=toupdate[6],
                release_stage=toupdate[7],
                channel_family_id=toupdate[8],
                product_id=toupdate[9],
            )

    # pylint: disable-next=invalid-name
    def processSuseProductChannels(self, batch):
        """Check if the SUSE ProductChannel is already in DB.
        If yes, update it, if not add it. We get only "mandatory" product channels
        This is jut to update this flag.
        """
        insert_pc = self.dbmodule.prepare(
            """
            INSERT INTO suseProductChannel
                   (id, product_id, channel_id, mandatory)
            VALUES (sequence_nextval('suse_product_channel_id_seq'), :pid, :cid, 'Y')
            """
        )
        update_pc = self.dbmodule.prepare(
            """
            UPDATE suseProductChannel
               SET mandatory = :mand
             WHERE product_id = :pid
               AND channel_id = :cid
             """
        )
        # pylint: disable-next=invalid-name
        _query_pc = self.dbmodule.prepare(
            """
            SELECT product_id, channel_id FROM suseProductChannel
            """
        )
        _query_pc.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s" % (x["product_id"], x["channel_id"])
            for x in _query_pc.fetchall_dict() or []
        ]
        toinsert = [[], []]
        toupdate = [[], [], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s" % (item["product_id"], item["channel_id"])
            if ident in existing_data:
                existing_data.remove(ident)
                toupdate[0].append("Y")
                toupdate[1].append(item["product_id"])
                toupdate[2].append(item["channel_id"])
                continue
            toinsert[0].append(item["product_id"])
            toinsert[1].append(item["channel_id"])
        for ident in existing_data:
            pid, cid = ident.split("-", 1)
            toupdate[0].append("N")
            toupdate[1].append(int(pid))
            toupdate[2].append(int(cid))
        if toinsert[0]:
            insert_pc.executemany(pid=toinsert[0], cid=toinsert[1])
        if toupdate[0]:
            update_pc.executemany(mand=toupdate[0], pid=toupdate[1], cid=toupdate[2])

    # pylint: disable-next=invalid-name
    def processSuseUpgradePaths(self, batch):
        """Check if the SUSE Upgrade Paths are already in DB.
        If not add it.
        """
        insert_up = self.dbmodule.prepare(
            """
            INSERT INTO suseUpgradePath
                   (from_pdid, to_pdid)
            VALUES (:from_pdid, :to_pdid)
            """
        )
        delete_up = self.dbmodule.prepare(
            """
            DELETE FROM suseUpgradePath
             WHERE from_pdid = :from_pdid
               AND to_pdid = :to_pdid
            """
        )
        # pylint: disable-next=invalid-name
        _query_up = self.dbmodule.prepare(
            """
            SELECT from_pdid, to_pdid FROM suseUpgradePath
            """
        )
        _query_up.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s" % (x["from_pdid"], x["to_pdid"])
            for x in _query_up.fetchall_dict() or []
        ]
        toinsert = [[], []]
        todelete = [[], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s" % (item["from_pdid"], item["to_pdid"])
            if ident in existing_data:
                existing_data.remove(ident)
                continue
            toinsert[0].append(item["from_pdid"])
            toinsert[1].append(item["to_pdid"])
        for ident in existing_data:
            fpdid, tpdid = ident.split("-", 1)
            todelete[0].append(int(fpdid))
            todelete[1].append(int(tpdid))
        if todelete[0]:
            delete_up.executemany(from_pdid=todelete[0], to_pdid=todelete[1])
        if toinsert[0]:
            insert_up.executemany(from_pdid=toinsert[0], to_pdid=toinsert[1])

    # pylint: disable-next=invalid-name
    def processSuseProductExtensions(self, batch):
        """Check if the SUSE Extensions are already in DB.
        If not add it.
        """
        insert_pe = self.dbmodule.prepare(
            """
            INSERT INTO suseProductExtension
                   (base_pdid, root_pdid, ext_pdid, recommended)
            VALUES (:product_id, :root_id, :ext_id, :recommended)
            """
        )
        delete_pe = self.dbmodule.prepare(
            """
            DELETE FROM suseProductExtension
             WHERE base_pdid = :product_id
               AND root_pdid = :root_id
               AND ext_pdid = :ext_id
            """
        )
        update_pe = self.dbmodule.prepare(
            """
            UPDATE suseProductExtension
               SET recommended = :recommended
             WHERE base_pdid = :product_id
               AND root_pdid = :root_id
               AND ext_pdid = :ext_id
        """
        )
        # pylint: disable-next=invalid-name
        _query_pe = self.dbmodule.prepare(
            """
            SELECT base_pdid, root_pdid, ext_pdid FROM suseProductExtension
            """
        )
        _query_pe.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s-%s" % (x["base_pdid"], x["root_pdid"], x["ext_pdid"])
            for x in _query_pe.fetchall_dict() or []
        ]
        toinsert = [[], [], [], []]
        todelete = [[], [], []]
        toupdate = [[], [], [], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s-%s" % (
                item["product_pdid"],
                item["root_pdid"],
                item["ext_pdid"],
            )
            if ident in existing_data:
                existing_data.remove(ident)
                toupdate[0].append(item["recommended"])
                toupdate[1].append(item["product_pdid"])
                toupdate[2].append(item["root_pdid"])
                toupdate[3].append(item["ext_pdid"])
                continue
            toinsert[0].append(item["product_pdid"])
            toinsert[1].append(item["root_pdid"])
            toinsert[2].append(item["ext_pdid"])
            toinsert[3].append(item["recommended"])
        for ident in existing_data:
            product_id, root_id, ext_id = ident.split("-", 2)
            todelete[0].append(int(product_id))
            todelete[1].append(int(root_id))
            todelete[2].append(int(ext_id))
        if todelete[0]:
            delete_pe.executemany(
                product_id=todelete[0], root_id=todelete[1], ext_id=todelete[2]
            )
        if toinsert[0]:
            insert_pe.executemany(
                product_id=toinsert[0],
                root_id=toinsert[1],
                ext_id=toinsert[2],
                recommended=toinsert[3],
            )
        if toupdate[0]:
            update_pe.executemany(
                product_id=toupdate[1],
                root_id=toupdate[2],
                ext_id=toupdate[3],
                recommended=toupdate[0],
            )

    # pylint: disable-next=invalid-name
    def processSuseProductRepositories(self, batch):
        """Check if the SUSE Product Repositories are already in DB.
        If not add it.
        """
        insert_pr = self.dbmodule.prepare(
            """
            INSERT INTO suseProductSCCRepository
                   (id, product_id, root_product_id, repo_id, channel_label, parent_channel_label,
                    channel_name, mandatory, update_tag)
            VALUES (:id, :product_id, :root_id, :repo_id, :channel_label, :parent_channel_label,
                    :channel_name, :mandatory, :update_tag)
            """
        )
        delete_pr = self.dbmodule.prepare(
            """
            DELETE FROM suseProductSCCRepository
             WHERE product_id = :product_id
               AND root_product_id = :root_id
               AND repo_id = :repo_id
            """
        )
        update_pr = self.dbmodule.prepare(
            """
            UPDATE suseProductSCCRepository
               SET channel_label = :channel_label,
                   parent_channel_label = :parent_channel_label,
                   channel_name = :channel_name,
                   mandatory = :mandatory,
                   update_tag = :update_tag
             WHERE product_id = :product_id
               AND root_product_id = :root_id
               AND repo_id = :repo_id
        """
        )
        # pylint: disable-next=invalid-name
        _query_pr = self.dbmodule.prepare(
            """
            SELECT product_id, root_product_id, repo_id FROM suseProductSCCRepository
            """
        )
        _query_pr.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s-%s" % (x["product_id"], x["root_product_id"], x["repo_id"])
            for x in _query_pr.fetchall_dict() or []
        ]
        toinsert = [[], [], [], [], [], [], [], [], []]
        todelete = [[], [], []]
        toupdate = [[], [], [], [], [], [], [], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s-%s" % (
                item["product_pdid"],
                item["root_pdid"],
                item["repo_pdid"],
            )
            if ident in existing_data:
                existing_data.remove(ident)
                toupdate[0].append(item["channel_label"])
                toupdate[1].append(item["parent_channel_label"])
                toupdate[2].append(item["channel_name"])
                toupdate[3].append(item["mandatory"])
                toupdate[4].append(item["update_tag"])
                toupdate[5].append(int(item["product_pdid"]))
                toupdate[6].append(int(item["root_pdid"]))
                toupdate[7].append(int(item["repo_pdid"]))
                continue
            toinsert[0].append(self.sequences["suseProductSCCRepository"].next())
            toinsert[1].append(int(item["product_pdid"]))
            toinsert[2].append(int(item["root_pdid"]))
            toinsert[3].append(int(item["repo_pdid"]))
            toinsert[4].append(item["channel_label"])
            toinsert[5].append(item["parent_channel_label"])
            toinsert[6].append(item["channel_name"])
            toinsert[7].append(item["mandatory"])
            toinsert[8].append(item["update_tag"])
        for ident in existing_data:
            product_id, rootid, repo_id = ident.split("-", 2)
            todelete[0].append(int(product_id))
            todelete[1].append(int(rootid))
            todelete[2].append(int(repo_id))
        if todelete[0]:
            delete_pr.executemany(
                product_id=todelete[0], root_id=todelete[1], repo_id=todelete[2]
            )
        if toinsert[0]:
            insert_pr.executemany(
                id=toinsert[0],
                product_id=toinsert[1],
                root_id=toinsert[2],
                repo_id=toinsert[3],
                channel_label=toinsert[4],
                parent_channel_label=toinsert[5],
                channel_name=toinsert[6],
                mandatory=toinsert[7],
                update_tag=toinsert[8],
            )
        if toupdate[0]:
            update_pr.executemany(
                product_id=toupdate[5],
                root_id=toupdate[6],
                repo_id=toupdate[7],
                channel_label=toupdate[0],
                parent_channel_label=toupdate[1],
                channel_name=toupdate[2],
                mandatory=toupdate[3],
                update_tag=toupdate[4],
            )

    # pylint: disable-next=invalid-name
    def processSCCRepositories(self, batch):
        """Check if SCC Repository is already in DB.
        If yes, update it, if not add it.
        """
        # suseSCCRepositoryAuth should be empty in ISS case
        self.dbmodule.prepare(
            """
            DELETE FROM suseSCCRepositoryAuth
        """
        ).execute()
        insert_repo = self.dbmodule.prepare(
            """
            INSERT INTO suseSCCRepository (id, scc_id, autorefresh, name, distro_target, description, url, signed, installer_updates)
            VALUES (:rid, :sccid, :autorefresh, :name, :target, :description, :url, :signed, :installer_updates)
            """
        )
        delete_repo = self.dbmodule.prepare(
            """
            DELETE FROM suseSCCRepository WHERE scc_id = :sccid
            """
        )
        update_repo = self.dbmodule.prepare(
            """
            UPDATE suseSCCRepository
               SET name = :name,
                   autorefresh = :autorefresh,
                   distro_target = :target,
                   description = :description,
                   url = :url,
                   signed = :signed,
                   installer_updates = :installer_updates
             WHERE scc_id = :sccid
             """
        )
        # pylint: disable-next=invalid-name
        _query_repo = self.dbmodule.prepare(
            """
            SELECT scc_id FROM suseSCCRepository
            """
        )
        _query_repo.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s" % (x["scc_id"])
            for x in _query_repo.fetchall_dict() or []
        ]
        toinsert = [[], [], [], [], [], [], [], [], []]
        todelete = [[]]
        toupdate = [[], [], [], [], [], [], [], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s" % item["sccid"]
            if ident in existing_data:
                existing_data.remove(ident)
                toupdate[0].append(item["name"])
                toupdate[1].append(item["autorefresh"])
                toupdate[2].append(item["distro_target"])
                toupdate[3].append(item["description"])
                toupdate[4].append(item["url"])
                toupdate[5].append(item["signed"])
                toupdate[6].append(item["installer_updates"])
                toupdate[7].append(item["sccid"])
                continue
            toinsert[0].append(self.sequences["suseSCCRepository"].next())
            toinsert[1].append(item["sccid"])
            toinsert[2].append(item["autorefresh"])
            toinsert[3].append(item["name"])
            toinsert[4].append(item["distro_target"])
            toinsert[5].append(item["description"])
            toinsert[6].append(item["url"])
            toinsert[7].append(item["signed"])
            toinsert[8].append(item["installer_updates"])
        for ident in existing_data:
            todelete[0].append(int(ident))
        if todelete[0]:
            delete_repo.executemany(sccid=todelete[0])
        if toinsert[0]:
            insert_repo.executemany(
                rid=toinsert[0],
                sccid=toinsert[1],
                autorefresh=toinsert[2],
                name=toinsert[3],
                target=toinsert[4],
                description=toinsert[5],
                url=toinsert[6],
                signed=toinsert[7],
                installer_updates=toinsert[8],
            )
        if toupdate[0]:
            update_repo.executemany(
                name=toupdate[0],
                autorefresh=toupdate[1],
                target=toupdate[2],
                description=toupdate[3],
                url=toupdate[4],
                signed=toupdate[5],
                installer_updates=toupdate[6],
                sccid=toupdate[7],
            )

    # pylint: disable-next=invalid-name
    def processClonedChannels(self, batch):
        """Check if cloned channel info is already in DB.
        If not add it.
        """
        insert_cc = self.dbmodule.prepare(
            """
            INSERT INTO rhnChannelCloned
                   (original_id, id)
            VALUES (:orig_id, :id)
            """
        )
        delete_cc = self.dbmodule.prepare(
            """
            DELETE FROM rhnChannelCloned
             WHERE original_id = :orig_id
               AND id = :id
            """
        )
        # pylint: disable-next=invalid-name
        _query_cc = self.dbmodule.prepare(
            """
            SELECT original_id orig_id, id FROM rhnChannelCloned
            """
        )
        _query_cc.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s" % (x["orig_id"], x["id"])
            for x in _query_cc.fetchall_dict() or []
        ]
        toinsert = [[], []]
        todelete = [[], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s" % (item["orig_id"], item["id"])
            if ident in existing_data:
                existing_data.remove(ident)
                continue
            toinsert[0].append(item["orig_id"])
            toinsert[1].append(item["id"])
        for ident in existing_data:
            fpdid, tpdid = ident.split("-", 1)
            todelete[0].append(int(fpdid))
            todelete[1].append(int(tpdid))
        if todelete[0]:
            delete_cc.executemany(orig_id=todelete[0], id=todelete[1])
        if toinsert[0]:
            insert_cc.executemany(orig_id=toinsert[0], id=toinsert[1])

    # pylint: disable-next=invalid-name
    def processSuseSubscriptions(self, batch):
        """Check if the Subscriptions are already in DB.
        If yes, update it, if not add it.
        """
        insert_pcf = self.dbmodule.prepare(
            """
            INSERT INTO rhnPrivateChannelFamily
                   (channel_family_id, org_id)
            VALUES (:cfid, :org_id)
            """
        )
        # pylint: disable-next=invalid-name
        _query_pcf = self.dbmodule.prepare(
            """
            SELECT channel_family_id, org_id FROM rhnPrivateChannelFamily
            """
        )
        _query_pcf.execute()
        existing_data = [
            # pylint: disable-next=consider-using-f-string
            "%s-%s" % (x["channel_family_id"], x["org_id"])
            for x in _query_pcf.fetchall_dict() or []
        ]
        toinsert = [[], []]
        for item in batch:
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s" % (item["channel_family_id"], item["org_id"])
            if ident in existing_data:
                existing_data.remove(ident)
                continue
            if item["channel_family_id"]:
                toinsert[0].append(item["channel_family_id"])
                toinsert[1].append(item["org_id"])
        if toinsert[0]:
            insert_pcf.executemany(cfid=toinsert[0], org_id=toinsert[1])

    # pylint: disable-next=invalid-name
    def lookupPackageIdFromPackage(self, package):
        if not isinstance(package, IncompletePackage):
            raise TypeError(
                # pylint: disable-next=consider-using-f-string
                "Expected an IncompletePackage instance, found %s"
                # pylint: disable-next=used-before-assignment
                % str(type(package))
            )
        statement = self.dbmodule.prepare(
            """
            SELECT p.id
              FROM rhnPackage p
              JOIN rhnPackageName pn ON p.name_id = pn.id
              JOIN rhnPackageEVR pe ON p.evr_id = pe.id
              JOIN rhnPackageArch pa ON p.package_arch_id = pa.id
              JOIN rhnChecksumView cv ON p.checksum_id = cv.id
             WHERE pn.name = :name
               AND ( pe.epoch  = :epoch or
                     ( pe.epoch is null and :epoch is null )
                   )
               AND pe.version = :version
               AND pe.release = :release
               AND pa.label = :arch
               AND cv.checksum = :checksum
               AND cv.checksum_type = :checksum_type
        """
        )

        # pylint: disable-next=redefined-builtin
        for type, chksum in list(package["checksums"].items()):
            if not package["epoch"]:
                package["epoch"] = None
            statement.execute(
                name=package["name"],
                epoch=package["epoch"],
                version=package["version"],
                release=package["release"],
                arch=package["arch"],
                checksum=chksum,
                checksum_type=type,
            )
            pkgid = statement.fetchone_dict() or None
            if pkgid:
                package.id = pkgid["id"]
                return

    # pylint: disable-next=invalid-name
    def lookupSuseProductIdByProductId(self, pid):
        # pylint: disable-next=invalid-name
        _query = self.dbmodule.prepare(
            """
            SELECT id FROM suseProducts WHERE product_id = :pid
        """
        )
        _query.execute(pid=pid)
        res = _query.fetchone_dict()
        if res:
            return res["id"]
        return None

    # pylint: disable-next=invalid-name
    def lookupRepoIdBySCCRepoId(self, rid):
        # pylint: disable-next=invalid-name
        _query = self.dbmodule.prepare(
            """
            SELECT id FROM suseSCCRepository WHERE scc_id = :rid
        """
        )
        _query.execute(rid=rid)
        res = _query.fetchone_dict()
        if res:
            return res["id"]
        return None

    # pylint: disable-next=invalid-name
    def lookupKeyword(self, keyword):
        statement = self.dbmodule.prepare(
            """
            SELECT id
              FROM suseMdKeyword
             WHERE label = :label
        """
        )
        statement.execute(label=keyword)
        kid = statement.fetchone_dict()

        if kid:
            return kid["id"]
        kid = self.sequences["suseMdKeyword"].next()
        statement = self.dbmodule.prepare(
            """
            INSERT INTO suseMdKeyword (id, label)
            VALUES (:kid, :label)
        """
        )
        statement.execute(kid=kid, label=keyword)
        return kid

    # bug #528227
    # pylint: disable-next=invalid-name
    def lookupChannelOrg(self, label):
        """For given label of channel return its org_id.
        If channel with given label does not exist or is NULL, return None.
        """
        statement = self.dbmodule.prepare(
            """
            SELECT org_id
              FROM rhnChannel
             WHERE label = :label
        """
        )

        statement.execute(label=label)
        org_id = statement.fetchone_dict()

        if org_id:
            return org_id

        return

    # pylint: disable-next=invalid-name
    def lookupChannelProduct(self, channel):
        statement = self.dbmodule.prepare(
            """
            SELECT id
              FROM rhnChannelProduct
             WHERE product = :product
               AND version = :version
               AND beta = :beta
        """
        )

        statement.execute(
            product=channel["channel_product"],
            version=channel["channel_product_version"],
            beta=channel["channel_product_beta"],
        )

        product = statement.fetchone_dict()

        if product:
            return product["id"]

        return self.createChannelProduct(channel)

    # pylint: disable-next=invalid-name
    def createChannelProduct(self, channel):
        # pylint: disable-next=redefined-builtin
        id = self.sequences["rhnChannelProduct"].next()

        statement = self.dbmodule.prepare(
            """
            INSERT
              INTO rhnChannelProduct
                   (id, product, version, beta)
            VALUES (:id, :product, :version, :beta)
        """
        )

        statement.execute(
            id=id,
            product=channel["channel_product"],
            version=channel["channel_product_version"],
            beta=channel["channel_product_beta"],
        )

        return id

    # pylint: disable-next=invalid-name
    def subscribeToChannels(self, packages, strict=0):
        # pylint: disable-next=redefined-builtin
        hash = {
            "package_id": [],
            "channel_id": [],
        }
        # Keep a list of packages for a channel too, so we can easily compare
        # what's extra, if strict is 1
        channel_packages = {}
        sql = """
            select channel_id
            from rhnChannelPackage
            where package_id = :package_id"""
        affected_channels = {}
        statement = self.dbmodule.prepare(sql)
        for package in packages:
            if package.ignored:
                # Skip it
                continue
            if package.id is None:
                raise InvalidPackageError(package, "Invalid package")
            # Look it up first
            statement.execute(package_id=package.id)
            channels = {}
            while 1:
                row = statement.fetchone_dict()
                if not row:
                    break
                channels[row["channel_id"]] = None

            # pylint: disable-next=invalid-name
            for channelId in list(package["channels"].keys()):
                # Build the channel-package list
                if channelId in channel_packages:
                    cp = channel_packages[channelId]
                else:
                    channel_packages[channelId] = cp = {}
                cp[package.id] = None

                if channelId in channels:
                    # Already subscribed
                    continue
                # pylint: disable-next=redefined-builtin
                dict = {
                    "package_id": package.id,
                    "channel_id": channelId,
                }
                if channelId not in affected_channels:
                    modified_packages = ([], [])
                    affected_channels[channelId] = modified_packages
                else:
                    modified_packages = affected_channels[channelId]
                # Package was added to this channel
                modified_packages[0].append(package.id)
                addHash(hash, dict)

        # Packages we'd have to delete
        extra_cp = {
            "package_id": [],
            "channel_id": [],
        }
        if strict:
            # if strict remove the extra packages from the DB
            sql = """
                select package_id
                  from rhnChannelPackage
                 where channel_id = :channel_id
            """
        else:
            # or at least we should delete packages from different org
            sql = """
                select package_id
                  from rhnChannelPackage cp
                  join rhnPackage p
                    on p.id = cp.package_id
                  join rhnChannel c
                    on c.id = cp.channel_id
                 where cp.channel_id = :channel_id
                   and c.org_id != p.org_id
            """

        statement = self.dbmodule.prepare(sql)
        for channel_id, pid_hash in list(channel_packages.items()):
            statement.execute(channel_id=channel_id)
            while 1:
                row = statement.fetchone_dict()
                if not row:
                    break
                package_id = row["package_id"]
                if package_id not in pid_hash:
                    # Have to remove it
                    extra_cp["package_id"].append(package_id)
                    extra_cp["channel_id"].append(channel_id)
                    # And mark this channel as being affected
                    if channel_id not in affected_channels:
                        modified_packages = ([], [])
                        affected_channels[channel_id] = modified_packages
                    else:
                        modified_packages = affected_channels[channel_id]
                    # Package was deletef from this channel
                    modified_packages[1].append(package_id)

        self.__doDeleteTable("rhnChannelPackage", extra_cp)
        self.__doInsertTable("rhnChannelPackage", hash)
        # This function returns the channels that were affected
        return affected_channels

    # pylint: disable-next=dangerous-default-value
    def update_newest_package_cache(self, caller, affected_channels, name_ids=[]):
        # affected_channels is a hash keyed on the channel id, and with a
        # tuple (added_package_list, deleted_package_list) as values
        refresh_newest_package = self.dbmodule.Procedure(
            "rhn_channel.refresh_newest_package"
        )
        update_channel = self.dbmodule.Procedure("rhn_channel.update_channel")
        # pylint: disable-next=unused-variable
        for channel_id, (added_packages_list, deleted_packages_list) in list(
            affected_channels.items()
        ):
            try:
                if name_ids:
                    # pylint: disable-next=redefined-builtin
                    for id in name_ids:
                        refresh_newest_package(channel_id, caller, id)
                else:
                    refresh_newest_package(channel_id, caller, None)
            except rhnSQL.SQLError:
                e = sys.exc_info()[1]
                raise_with_tb(rhnFault(23, str(e[1]), explain=0), sys.exc_info()[2])
            if deleted_packages_list:
                invalidate_ss = 1
            else:
                invalidate_ss = 0
            update_channel(channel_id, invalidate_ss)

    # pylint: disable-next=invalid-name
    def processSourcePackages(
        self,
        packages,
        # pylint: disable-next=invalid-name
        uploadForce=0,
        # pylint: disable-next=invalid-name
        ignoreUploaded=0,
        # pylint: disable-next=invalid-name
        forceVerify=0,
        transactional=0,
    ):
        # Insert/update the packages

        # pylint: disable-next=invalid-name
        childTables = []

        for package in packages:
            if not isinstance(package, SourcePackage):
                raise TypeError("Expected a Package instance")

        # Process the packages

        self.__processObjectCollection(
            packages,
            "rhnPackageSource",
            childTables,
            "package_id",
            uploadForce=uploadForce,
            forceVerify=forceVerify,
            ignoreUploaded=ignoreUploaded,
            severityLimit=1,
            transactional=transactional,
        )

    def commit(self):
        self.dbmodule.commit()

    def rollback(self):
        self.dbmodule.rollback()

    # pylint: disable-next=invalid-name,redefined-builtin
    def __processHash(self, lookup, hash):
        if not hash:
            # Nothing to do
            return

        h = rhnSQL.prepare("select " + lookup + "(:name) from dual")
        for k in list(hash.keys()):
            h.execute(name=k)
            # saving id
            hash[k] = h.fetchone_dict().popitem()[1]

    # pylint: disable-next=invalid-name
    def __buildQueries(self, childTables):
        # pylint: disable-next=invalid-name
        childTableLookups = {}
        # pylint: disable-next=invalid-name
        queryTempl = "select * from %s where %s = :id"
        # pylint: disable-next=invalid-name
        for childTableName in childTables:
            childTableLookups[childTableName] = self.dbmodule.prepare(
                queryTempl % (childTableName, childTables[childTableName])
            )
        return childTableLookups

    # pylint: disable-next=invalid-name,dangerous-default-value
    def __processObjectCollection(
        self,
        # pylint: disable-next=invalid-name
        objColl,
        # pylint: disable-next=invalid-name
        parentTable,
        # pylint: disable-next=invalid-name
        childTables=[],
        colname=None,
        **kwargs,
    ):
        # Returns the DML object that was processed
        # This helps identify what the changes were

        # XXX this is a total hack keeping tranlating the old interface into the
        # new interface to keep me from having to change all the places in the
        # code that call this method, as there are 10 of them...

        # pylint: disable-next=invalid-name
        childDict = {}

        for tbl in childTables:
            childDict[tbl] = colname

        return self.__processObjectCollection__(
            objColl, parentTable, childDict, **kwargs
        )

    # pylint: disable-next=invalid-name,dangerous-default-value
    def __processObjectCollection__(
        self,
        # pylint: disable-next=invalid-name
        objColl,
        # pylint: disable-next=invalid-name
        parentTable,
        # pylint: disable-next=invalid-name
        childTables={},
        **kwargs,
    ):
        # Returns the DML object that was processed
        # This helps identify what the changes were

        # FIXME I need to break this method apart into smaller method calls that
        # will allow *different* colname fields for different childTables
        # NOTE objColl == packages
        # Process the object collection, starting with parentTable, having
        # colname as a link column between the parent and child tables
        #
        # We create a DML object for the operations we're supposed to perform
        # on the database
        kwparams = {
            # The 'upload force'
            "uploadForce": 0,
            # Raises exceptions when the object is already uploaded
            "ignoreUploaded": 0,
            # Forces a full object verification - including the child tables
            "forceVerify": 0,
            # When the severity is below this limit, the object is not
            # updated
            "severityLimit": 0,
            # All-or-nothing
            "transactional": 0,
        }

        for k, v in list(kwargs.items()):
            if k not in kwparams:
                # pylint: disable-next=consider-using-f-string
                raise TypeError("Unknown keyword parameter %s" % k)
            if v is not None:
                # Leave the default values in case of a None
                kwparams[k] = v

        # pylint: disable-next=invalid-name
        uploadForce = kwparams["uploadForce"]
        # pylint: disable-next=invalid-name
        ignoreUploaded = kwparams["ignoreUploaded"]
        # pylint: disable-next=invalid-name
        severityLimit = kwparams["severityLimit"]
        transactional = kwparams["transactional"]
        # pylint: disable-next=invalid-name
        forceVerify = kwparams["forceVerify"]

        # All the tables affected
        tables = [parentTable] + list(childTables.keys())

        # Build the hash for the operations on the tables
        dml = DML(tables, self.tables)
        # Reverse hash: object id to object for already-uploaded objects
        # pylint: disable-next=invalid-name
        uploadedObjects = {}
        # Information related to the parent table
        # pylint: disable-next=invalid-name
        parentTableObj = self.tables[parentTable]
        # pylint: disable-next=invalid-name
        ptFields = parentTableObj.getFields()
        # pylint: disable-next=invalid-name
        severityHash = parentTableObj.getSeverityHash()

        # A flag that indicates if something has to be modified beyond the
        # current severity limit
        # pylint: disable-next=invalid-name
        brokenTransaction = 0

        # Lookup object
        lookup = TableLookup(parentTableObj, self.dbmodule)
        # XXX
        # pylint: disable-next=invalid-name
        childTableLookups = self.__buildQueries(childTables)
        # For each valid object in the collection, look it up
        #   if it doesn't exist, insert all the associated information
        #   if it already exists:
        #       save it in the uploadedObjects hash for later processing
        #       the object's diff member will contain data if that object
        #         failed to push; the content should be explicit enough about
        #         what failed
        #   The object's diff_result should reliably say if the object was
        #       different in any way, or if it was new. Each field that gets
        #       compared will present its won severity field (or the default
        #       one if not explicitly specified). The "global" severity is the
        #       max of all severities.
        #   New objects will have a diff level of -1
        # pylint: disable-next=redefined-builtin
        for object in objColl:
            if object.ignored:
                # Skip it
                continue
            h = lookup.query(object)
            row = h.fetchone_dict()
            if not row:
                # Object does not exist
                # pylint: disable-next=redefined-builtin
                id = self.sequences[parentTable].next()
                object.id = id
                # pylint: disable-next=invalid-name
                extObject = {"id": id}
                _buildExternalValue(extObject, object, parentTableObj)
                addHash(dml.insert[parentTable], extObject)

                # Insert child table information
                for tname in childTables:
                    tbl = self.tables[tname]
                    # Get the list of objects for this package
                    entry_list = object[tbl.getAttribute()]
                    if entry_list is None:
                        continue
                    seq_col = tbl.sequenceColumn
                    new_ids = (
                        self.sequences[tbl.name].next_many(len(entry_list))
                        if seq_col
                        else []
                    )
                    for i, entry in enumerate(entry_list):
                        # pylint: disable-next=invalid-name
                        extObject = {childTables[tname]: id}
                        if seq_col:
                            # This table has to insert values in a sequenced
                            # column; since it's a child table and the entry
                            # in the master table is not created yet, there
                            # shouldn't be a problem with uniqueness
                            # constraints
                            new_id = new_ids[i]
                            extObject[seq_col] = new_id
                            # Make sure we initialize the object's sequenced
                            # column as well
                            entry[seq_col] = new_id
                        _buildExternalValue(extObject, entry, tbl)
                        addHash(dml.insert[tname], extObject)
                object.diff_result = Diff()
                # New object
                object.diff_result.level = -1
                continue

            # Already uploaded
            if not ignoreUploaded:
                raise AlreadyUploadedError(object, "Already uploaded")

            # XXX package id set here!!!!!!!!!!
            object.id = row["id"]
            # And save the object and the row for later processing
            uploadedObjects[row["id"]] = [object, row]

        # Deal with already-uploaded objects
        for objid, (object, row) in list(uploadedObjects.items()):
            # Build the external value
            # pylint: disable-next=invalid-name
            extObject = {"id": row["id"]}
            _buildExternalValue(extObject, object, parentTableObj)
            # Build the DB value
            row = _buildDatabaseValue(row, ptFields)
            # compare them
            object.diff = object.diff_result = Diff()
            diffval = computeDiff(extObject, row, severityHash, object.diff)
            if not forceVerify:
                # If there is enough karma, force the full object check
                # maybe they want the object overwritten
                if uploadForce < object.diff.level and diffval <= severityLimit:
                    # Same object, or not different enough
                    # not enough karma either
                    continue

            # pylint: disable-next=invalid-name
            localDML = self.__processUploaded(
                objid, object, childTables, childTableLookups
            )

            if uploadForce < object.diff.level:
                # Not enough karma
                if object.diff.level > severityLimit:
                    # Broken transaction - object is too different
                    # pylint: disable-next=invalid-name
                    brokenTransaction = 1
                continue

            # Clean up the object diff since we pushed the package
            object.diff = None

            if diffval:
                # Different parent object
                localDML["update"][parentTable] = [extObject]

            # And transfer the local DML to the global one
            for k, tablehash in list(localDML.items()):
                dmlhash = getattr(dml, k)
                for tname, vallist in list(tablehash.items()):
                    for val in vallist:
                        addHash(dmlhash[tname], val)

        if transactional and brokenTransaction:
            raise TransactionError("Error uploading package source batch")
        return self.__doDML(dml)

    # pylint: disable-next=invalid-name,redefined-builtin
    def __processUploaded(self, objid, object, childTables, childTableLookups):
        # Store the DML operations locally
        # pylint: disable-next=invalid-name
        localDML = {
            "insert": {},
            "update": {},
            "delete": {},
        }

        # Grab the rest of the information
        # pylint: disable-next=invalid-name
        childTablesInfo = self.__getChildTablesInfo(
            objid, list(childTables.keys()), childTableLookups
        )

        # Start computing deltas
        # pylint: disable-next=invalid-name
        for childTableName in childTables:
            # Init the local hashes
            for k in ["insert", "update", "delete"]:
                localDML[k][childTableName] = []

            dbside = childTablesInfo[childTableName]
            # The child table object
            # pylint: disable-next=invalid-name
            childTableObj = self.tables[childTableName]
            # The name of the attribute in the parent object
            parentattr = childTableObj.getAttribute()
            # The list of entries associated with the attribute linked to
            # this table
            entrylist = object[parentattr]
            fields = childTableObj.getFields()
            pks = childTableObj.getPK()
            # pylint: disable-next=invalid-name
            childSeverityHash = childTableObj.getSeverityHash()
            if entrylist is None:
                continue
            for ent in entrylist:
                # Build the primary key
                key = []
                for f in pks:
                    if f == childTables[childTableName]:
                        # Special-case it
                        key.append(objid)
                        continue
                    datatype = fields[f]
                    # Get the proper attribute name for this column
                    attr = childTableObj.getObjectAttribute(f)
                    key.append(sanitizeValue(ent[attr], datatype))
                key = tuple(key)
                # Build the value
                val = {childTables[childTableName]: objid}
                if childTableObj.sequenceColumn:
                    # Initialize the sequenced column with a dummy value
                    ent[childTableObj.sequenceColumn] = None
                _buildExternalValue(val, ent, childTableObj)

                # Look this value up
                if key not in dbside:
                    if childTableObj.sequenceColumn:
                        # Initialize the sequence column too
                        sc = childTableObj.sequenceColumn
                        nextid = self.sequences[childTableName].next()
                        val[sc] = ent[sc] = nextid
                    # This entry has to be inserted
                    object.diff.append((parentattr, val, None))
                    # XXX change to a default value
                    object.diff.setLevel(4)

                    localDML["insert"][childTableName].append(val)
                    continue

                # Already exists in the DB
                dbval = _buildDatabaseValue(dbside[key], fields)

                if childTableObj.sequenceColumn:
                    # Copy the sequenced value - we dpn't want it updated
                    sc = childTableObj.sequenceColumn
                    val[sc] = ent[sc] = dbval[sc]
                # check for updates
                diffval = computeDiff(
                    val, dbval, childSeverityHash, object.diff, parentattr
                )
                if not diffval:
                    # Same value
                    del dbside[key]
                    continue

                # Different value; have to update the entry
                localDML["update"][childTableName].append(val)
                del dbside[key]

            # Anything else should be deleted
            for key, val in list(dbside.items()):
                # Send only the PKs
                # pylint: disable-next=redefined-builtin
                hash = {}
                for k in pks:
                    hash[k] = val[k]

                # XXX change to a default value
                object.diff.setLevel(4)

                localDML["delete"][childTableName].append(hash)
                object.diff.append((parentattr, None, val))

        return localDML

    # pylint: disable-next=invalid-name
    def __doDML(self, dml):
        self.__doDelete(dml.delete, dml.tables)
        self.__doUpdate(dml.update, dml.tables)
        self.__doInsert(dml.insert, dml.tables)
        return dml

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doInsert(self, hash, tables):
        for tname in tables:
            # pylint: disable-next=redefined-builtin
            dict = hash[tname]
            try:
                self.__doInsertTable(tname, dict)
            except rhnSQL.SQLError:
                e = sys.exc_info()[1]
                raise_with_tb(rhnFault(54, str(e[1]), explain=0), sys.exc_info()[2])

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doInsertTable(self, table, hash):
        if not hash:
            return
        tab = self.tables[table]
        k = list(hash.keys())[0]
        if not hash[k]:
            # Nothing to do
            return

        # pylint: disable-next=invalid-name
        insertObj = TableInsert(tab, self.dbmodule)
        insertObj.query(hash)
        return

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doDelete(self, hash, tables):
        for tname in tables:
            # pylint: disable-next=redefined-builtin
            dict = hash[tname]
            self.__doDeleteTable(tname, dict)

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doDeleteTable(self, tname, hash):
        if not hash:
            return
        tab = self.tables[tname]
        # Need to extract the primary keys and look for items to delete only
        # in those columns, the other ones may not be populated
        # See bug 154216 for details (misa 2005-04-08)
        pks = tab.getPK()
        k = pks[0]
        if not hash[k]:
            # Nothing to do
            return
        # pylint: disable-next=invalid-name
        deleteObj = TableDelete(tab, self.dbmodule)
        deleteObj.query(hash)

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doUpdate(self, hash, tables):
        for tname in tables:
            # pylint: disable-next=redefined-builtin
            dict = hash[tname]
            self.__doUpdateTable(tname, dict)

    # pylint: disable-next=invalid-name,redefined-builtin
    def __doUpdateTable(self, tname, hash):
        if not hash:
            return
        tab = self.tables[tname]
        # See bug 154216 for details (misa 2005-04-08)
        pks = tab.getPK()
        k = pks[0]
        if not hash[k]:
            # Nothing to do
            return
        # pylint: disable-next=invalid-name
        updateObj = TableUpdate(tab, self.dbmodule)
        updateObj.query(hash)
        return

    # pylint: disable-next=invalid-name
    def __lookupObjectCollection(self, objColl, tableName, ignore_missing=0):
        # Looks the object up in tableName, and fills in its id
        lookup = TableLookup(self.tables[tableName], self.dbmodule)
        # pylint: disable-next=redefined-builtin
        for object in objColl:
            if object.ignored:
                # Skip it
                continue
            h = lookup.query(object)
            row = h.fetchone_dict()
            if not row:
                if ignore_missing:
                    # Ignore the missing objects
                    object.ignored = 1
                    continue
                # Invalid
                raise InvalidPackageError(
                    object,
                    # pylint: disable-next=consider-using-f-string
                    "Could not find object %s in table %s" % (object, tableName),
                )
            object.id = row["id"]

    # pylint: disable-next=invalid-name,redefined-builtin
    def __getChildTablesInfo(self, id, tables, queries):
        # Returns a hash with the information about package id from tables
        result = {}
        for tname in tables:
            tableobj = self.tables[tname]
            fields = tableobj.getFields()
            q = queries[tname]
            q.execute(id=id)
            # pylint: disable-next=redefined-builtin
            hash = {}
            while 1:
                row = q.fetchone_dict()
                if not row:
                    break
                pks = tableobj.getPK()
                key = []
                for f in pks:
                    value = row[f]
                    datatype = fields[f]
                    value = sanitizeValue(value, datatype)
                    key.append(value)
                val = {}
                for f, datatype in list(fields.items()):
                    value = row[f]
                    value = sanitizeValue(value, datatype)
                    val[f] = value
                hash[tuple(key)] = val

            result[tname] = hash
        return result

    # pylint: disable-next=invalid-name
    def __populateTable(self, table_name, data, delete_extra=1):
        table = self.tables[table_name]
        fields = table.getFields()
        # Build a hash with the incoming data
        incoming = {}
        for entry in data:
            t = hash2tuple(entry, fields)
            incoming[t] = entry

        # Build the query to dump the table's contents
        # pylint: disable-next=consider-using-f-string
        h = self.dbmodule.prepare("select * from %s" % table.name)
        h.execute()
        deletes = {}
        inserts = {}
        for f in list(fields.keys()):
            inserts[f] = []
            deletes[f] = []

        while 1:
            row = h.fetchone_dict()
            if not row:
                break

            t = hash2tuple(row, fields)
            if t in incoming:
                # we already have this value uploaded
                del incoming[t]
                continue
            addHash(deletes, row)

        for row in list(incoming.values()):
            addHash(inserts, row)

        if delete_extra:
            self.__doDeleteTable(table.name, deletes)
        self.__doInsertTable(table.name, inserts)

    # This function does a diff on the specified table name for the presented
    # data, using pk_fields as unique fields
    def _do_diff(self, data, table_name, uq_fields, fields):
        first_uq_col = uq_fields[0]
        uq_col_values = {}
        all_fields = uq_fields + fields
        for entry in data:
            for f in all_fields:
                if f not in entry:
                    # pylint: disable-next=broad-exception-raised,consider-using-f-string
                    raise Exception("Missing field %s" % f)
            val = entry[first_uq_col]
            if val not in uq_col_values:
                valhash = {}
                uq_col_values[val] = valhash
            else:
                valhash = uq_col_values[val]
            key = build_key(entry, uq_fields)
            valhash[key] = entry

        # pylint: disable-next=consider-using-f-string
        query = "select %s from %s where %s = :%s" % (
            ", ".join(all_fields),
            table_name,
            first_uq_col,
            first_uq_col,
        )
        h = self.dbmodule.prepare(query)
        updates = []
        deletes = []
        for val, valhash in list(uq_col_values.items()):
            params = {first_uq_col: val}
            h.execute(**params)
            while 1:
                row = h.fetchone_dict()
                if not row:
                    break
                key = build_key(row, uq_fields)
                if key not in valhash:
                    # Need to delete this one
                    deletes.append(row)
                    continue
                entry = valhash[key]
                for f in fields:
                    if entry[f] != row[f]:
                        # Different, we have to update
                        break
                else:
                    # Same value, remove it from valhash
                    del valhash[key]
                    continue
                # Need to update
                updates.append(entry)

        inserts = []
        list(
            map(
                inserts.extend, [list(x.values()) for x in list(uq_col_values.values())]
            )
        )

        if deletes:
            params = transpose(deletes, uq_fields)
            # pylint: disable-next=consider-using-f-string
            query = "delete from %s where %s" % (
                table_name,
                # pylint: disable-next=consider-using-f-string
                " and ".join(["%s = :%s" % (x, x) for x in uq_fields]),
            )
            h = self.dbmodule.prepare(query)
            h.executemany(**params)
        if inserts:
            params = transpose(inserts, all_fields)
            # pylint: disable-next=consider-using-f-string
            query = "insert into %s (%s) values (%s)" % (
                table_name,
                ", ".join(all_fields),
                ", ".join([":" + x for x in all_fields]),
            )
            h = self.dbmodule.prepare(query)
            h.executemany(**params)
        if updates:
            params = transpose(updates, all_fields)
            # pylint: disable-next=consider-using-f-string
            query = "update % set %s where %s" % (
                table_name,
                ", ".join(["%s = :s" + (x, x) for x in fields]),
                # pylint: disable-next=consider-using-f-string
                " and ".join(["%s = :%s" % (x, x) for x in uq_fields]),
            )
            h = self.dbmodule.prepare(query)
            h.executemany(**params)

    def validate_pks(self):
        # If nevra is enabled use checksum as primary key
        tbs = self.tables["rhnPackage"]
        if not CFG.ENABLE_NVREA:
            # remove checksum from a primary key if nevra is disabled.
            if "checksum_id" in tbs.pk:
                tbs.pk.remove("checksum_id")


# Returns a tuple for the hash's values


# pylint: disable-next=redefined-builtin
def build_key(hash, fields):
    return tuple(map(lambda x, h=hash: h[x], fields))


def transpose(arrhash, fields):
    params = {}
    for f in fields:
        params[f] = []
    for h in arrhash:
        for f in fields:
            params[f].append(h[f])
    return params


# pylint: disable-next=redefined-builtin
def hash2tuple(hash, fields):
    # Converts the hash into a tuple, with the fields ordered as presented in
    # the fields list
    result = []
    for fname, ftype in list(fields.items()):
        result.append(sanitizeValue(hash[fname], ftype))
    return tuple(result)


# pylint: disable-next=missing-class-docstring
class DML:
    # pylint: disable-next=invalid-name
    def __init__(self, tables, tableHash):
        self.update = {}
        self.delete = {}
        self.insert = {}
        self.tables = tables
        for k in ("insert", "update", "delete"):
            dmlhash = {}
            setattr(self, k, dmlhash)
            for tname in tables:
                # pylint: disable-next=redefined-builtin
                hash = {}
                for f in list(tableHash[tname].getFields().keys()):
                    hash[f] = []
                dmlhash[tname] = hash


# pylint: disable-next=invalid-name
def _buildDatabaseValue(row, fieldsHash):
    # Returns a dictionary containing the interesting values of the row,
    # sanitized
    # pylint: disable-next=redefined-builtin
    dict = {}
    for f, datatype in list(fieldsHash.items()):
        dict[f] = sanitizeValue(row.get(f), datatype)
    return dict


# pylint: disable-next=invalid-name,redefined-builtin
def _buildExternalValue(dict, entry, tableObj):
    # updates dict with values from entry
    # entry is a hash-like object (non-db)
    for f, datatype in list(tableObj.getFields().items()):
        if f in dict:
            # initialized somewhere else
            continue
        # Get the attribute's name
        attr = tableObj.getObjectAttribute(f)
        # Sanitize the value according to its datatype
        if attr not in entry:
            entry[attr] = None
        dict[f] = sanitizeValue(entry[attr], datatype)


# pylint: disable-next=invalid-name
def computeDiff(hash1, hash2, diffHash, diffobj, prefix=None):
    # Compare if the key-values of hash1 are a subset of hash2's
    difference = 0
    ignore_keys = ["last_modified"]

    for k, v in list(hash1.items()):
        if k in ignore_keys:
            # Dont decide the diff based on last_modified
            # as this obviously wont match due to our db
            # other triggers.
            continue
        if hash2[k] == v:
            # Same values
            continue
        if k == "installed_size" and v is not None and hash2[k] is None:
            # Skip installed_size which might not have been populated
            continue
        if k in diffHash:
            diffval = diffHash[k]
            if diffval == 0:
                # Completely ignore this key
                continue
        else:
            diffval = diffobj.level + 1

        if prefix:
            diffkey = prefix + "::" + k
        else:
            diffkey = k

        diffobj.setLevel(diffval)
        diffobj.append((diffkey, v, hash2[k]))

        difference = diffobj.level

    return difference
