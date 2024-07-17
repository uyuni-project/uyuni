#  pylint: disable=missing-module-docstring

import re
import sys
from datetime import datetime
from dateutil.parser import parse as parse_date
from dateutil.tz import tzutc

from spacewalk.satellite_tools.syncLib import log, log2
from spacewalk.server import rhnChannel, rhnSQL
from spacewalk.server.importlib import importLib
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.errataImport import ErrataImport
from uyuni.common.context_managers import cfg_component

default_log_location = "/var/log/rhn/"

errata_typemap = {
    "security": "Security Advisory",
    "recommended": "Bug Fix Advisory",
    "bugfix": "Bug Fix Advisory",
    "optional": "Product Enhancement Advisory",
    "feature": "Product Enhancement Advisory",
    "enhancement": "Product Enhancement Advisory",
}

# namespace prefixes for parsing SUSE patches XML files
YUM = "{http://linux.duke.edu/metadata/common}"


# pylint: disable-next=missing-class-docstring
class LzRepoSync(object):
    def __init__(
        self,
        channel_label,
        repo_type=None,
        fail=False,
        no_errata=False,
        force_all_errata=False,
        deep_verify=False,
        force_null_org_content=False,
    ):
        self.channel_label = channel_label
        self.fail = fail  # Information used when handling exceptions
        self.no_errata = no_errata
        self.force_all_errata = force_all_errata
        self.deep_verify = deep_verify
        self.regen = False  # TODO what's this ?
        self.all_errata = set()
        self.error_messages = []
        # TODO: !!IMPORTANT: add self.available_packages[ident] = 1 when importing packages (see reposync.py line 1375-1382)
        self.available_packages = {}

        rhnSQL.initDB()

        # setup logging
        # TODO

        self.channel = self.load_channel()
        if not self.channel:
            # pylint: disable-next=consider-using-f-string
            log(0, "Channel '%s' does not exist." % channel_label)
            sys.exit(1)

        if repo_type:
            self.repo_type_label = repo_type
        else:
            self.repo_type_label = "yum"

        if not self.channel["org_id"] or force_null_org_content:
            self.org_id = None
        else:
            self.org_id = int(self.channel["org_id"])

        self.arches = self.get_compatible_arches(int(self.channel["id"]))

    def load_channel(self):
        return rhnChannel.channel_info(self.channel_label)

    def import_updates(self, notices, notices_type="updateinfo"):
        log(0, "")
        # pylint: disable-next=consider-using-f-string
        log(0, "  Patches in repo: %s." % len(notices))
        if notices:
            processed_updates_count = 0
            if notices_type == "updateinfo":
                processed_updates_count = self.upload_updates(notices)
            elif notices_type == "patches":
                # TODO: Not handled
                return

            if processed_updates_count:
                #  some errata could get retracted and this needs to be reflected in the newest package cache
                log(
                    3,
                    # pylint: disable-next=consider-using-f-string
                    "Updating channel newest cache for channel ID %s."
                    % self.channel["id"],
                )
                refresh_newest_package = rhnSQL.Procedure(
                    "rhn_channel.refresh_newest_package"
                )
                refresh_newest_package(self.channel[id], "backend.importPatches")

    def upload_updates(self, notices):
        batch = []
        processed_updates = 0
        backend = SQLBackend()
        # pylint: disable-next=unused-variable
        for notice in notices:
            notice = self.fix_notice(notice)

            # Save advisory names from all repositories
            self.all_errata.add(notice["update_id"])

            # pylint: disable=W0703
            try:
                erratum = self._populate_erratum(notice)
                if not erratum:
                    continue
                batch.append(erratum)

                if self.deep_verify:
                    # import step by step
                    importer = ErrataImport(batch, backend)
                    importer.run()
                    batch = []
                processed_updates += 1
            except Exception:
                # pylint: disable-next=consider-using-f-string
                e = "Skipped %s - %s" % (notice["update_id"], sys.exc_info()[1])
                log2(1, 1, e, stream=sys.stderr)
                if self.fail:
                    raise
        if batch:
            # pylint: disable-next=consider-using-f-string
            log(0, "    Syncing %s new patch(es) to channel." % len(batch))
            importer = ErrataImport(batch, backend)
            importer.run()
            self.regen = True
        elif notices:
            log(0, "    No new patch to sync.")
        return processed_updates

    @staticmethod
    def fix_notice(notice):
        if "." in notice["version"]:
            new_version = 0
            for n in notice["version"].split("."):
                new_version = (new_version + int(n)) * 100
            notice["version"] = new_version / 100
            if LzRepoSync.is_old_suse_style(notice):
                # old suse style; we need to append the version to id
                # to get a seperate patch for every issue
                # pylint: disable-next=consider-using-f-string
                notice["update_id"] = "%s-%s" % (notice["update_id"], notice["version"])
        return notice

    @staticmethod
    def is_old_suse_style(notice):
        if (
            (
                notice["from"]
                and "suse" in notice["from"].lower()
                and int(notice["version"]) >= 1000
            )
            or (
                notice["update_id"][:4] in ("res5", "res6")
                and int(notice["version"]) > 6
            )
            or (notice["update_id"][:4] == "res4")
        ):
            # old style suse updateinfo starts with version >= 1000 or
            # have the res update_tag
            return True
        return False

    def _populate_erratum(self, notice):
        patch_name = self._patch_naming(notice)
        existing_errata = self.get_errata(patch_name)
        if existing_errata and not self._is_old_suse_style(notice):
            if int(existing_errata["advisory_rel"]) < int(notice["version"]):
                # A disaster happens
                #
                # re-releasing an errata with a higher release number
                # only happens in case of a disaster.
                # This should force mirrored repos to remove the old
                # errata and take care that the new one is the only
                # available.
                # This mean a hard overwrite
                self._delete_invalid_errata(existing_errata["id"])
            elif int(existing_errata["advisory_rel"]) > int(notice["version"]):
                # the existing errata has a higher release than the now
                # parsed one. We need to skip the current errata
                return None
            # else: release match, so we update the errata

        if notice["updated"]:
            updated_date = self._to_db_date(notice["updated"])
        else:
            updated_date = self._to_db_date(notice["issued"])
        if (
            existing_errata
            and existing_errata["advisory_status"] == notice["status"]
            and not self.errata_needs_update(
                existing_errata, notice["version"], updated_date
            )
        ):
            return None

        # pylint: disable-next=consider-using-f-string
        log(0, "Add Patch %s" % patch_name)

        e = importLib.Erratum()
        e["errata_from"] = notice["from"]
        e["advisory"] = e["advisory_name"] = patch_name
        e["advisory_rel"] = notice["version"]
        e["advisory_type"] = errata_typemap.get(
            notice["type"], "Product Enhancement Advisory"
        )
        e["advisory_status"] = notice["status"]
        e["product"] = notice["release"] or "Unknown"
        e["description"] = notice["description"] or "not set"
        e["synopsis"] = notice["title"] or notice["update_id"]
        if notice["severity"]:
            e["security_impact"] = notice["severity"]
            if notice["type"] == "security" and not e["synopsis"].startswith(
                notice["severity"] + ": "
            ):
                e["synopsis"] = notice["severity"] + ": " + e["synopsis"]
        else:
            # 'severity' not available in older yum versions
            # set default to Low to get a correct currency rating
            e["security_impact"] = "Low"
        e["topic"] = " "
        e["solution"] = " "
        e["issue_date"] = self._to_db_date(notice["issued"])
        e["update_date"] = updated_date
        e["org_id"] = self.org_id
        e["notes"] = ""
        e["rights"] = notice["rights"]
        e["refers_to"] = ""
        e["channels"] = [{"label": self.channel_label}]
        e["packages"] = []
        e["files"] = []
        if existing_errata:
            e["channels"].extend(existing_errata["channels"])
            e["packages"] = existing_errata["packages"]

        npkgs = [pkg for c in notice["pkglist"] for pkg in c["packages"]]

        e["packages"] = self._updates_process_packages(
            npkgs, e["advisory_name"], e["packages"]
        )
        # One or more package references could not be found in the Database.
        # To not provide incomplete patches we skip this update
        if not e["packages"] and not npkgs:
            # pylint: disable-next=consider-using-f-string
            log(2, "Advisory %s has empty package list." % e["advisory_name"])
        elif not e["packages"]:
            log(
                2,
                # pylint: disable-next=consider-using-f-string
                "Advisory %s skipped because of empty package list (filtered)."
                % e["advisory_name"],
            )
            return None

        e["keywords"] = self._update_keywords(notice)
        e["bugs"] = self._update_bugs(notice)
        e["cve"] = self._update_cve(notice)
        e["locally_modified"] = None
        return e

    def _patch_naming(self, notice):
        """Return the name of the patch according to our rules

        :notice: a notice/patch object (this could be a dictionary
        (new-style) or an ElementTree element (old code10 style))

        """
        try:
            version = int(notice.find(YUM + "version").get("ver"))
        except AttributeError:
            # normal yum updates (dicts)
            patch_name = notice["update_id"]
        else:
            # code10 patches
            if version >= 1000:
                # old suse style patch naming
                patch_name = notice.get("patchid")
            else:
                # new suse style patch naming
                patch_name = notice.find(YUM + "name").text

        # remove the channel-specific prefix
        # this way we can merge patches from different channels like
        # SDK, HAE and SLES
        update_tag = self.channel["update_tag"]
        if update_tag and patch_name.startswith(update_tag):
            patch_name = patch_name[len(update_tag) + 1 :]  # +1 for the hyphen
        elif update_tag and update_tag in patch_name:
            # SLE12 has SUSE-<update-tag>-...
            patch_name = patch_name.replace("SUSE-" + update_tag, "SUSE", 1)

        return patch_name

    def get_errata(self, update_id):
        """Return an Errata dict

        search in the database for the given advisory and
        return a dict with important values.
        If the advisory was not found it returns None

        :update_id - the advisory (name)
        """
        h = rhnSQL.prepare(
            """
            select e.id, e.advisory,
                   e.advisory_name, e.advisory_rel, e.advisory_status,
                   TO_CHAR(e.update_date, 'YYYY-MM-DD HH24:MI:SS') as update_date
              from rhnerrata e
             where e.advisory = :name
              and (e.org_id = :org_id or (e.org_id is null and :org_id is null))
        """
        )
        h.execute(name=update_id, org_id=self.org_id)
        ret = h.fetchone_dict()
        if not ret:
            return None

        h = rhnSQL.prepare(
            """
            select distinct c.label
              from rhnchannelerrata ce
              join rhnchannel c on c.id = ce.channel_id
             where ce.errata_id = :eid
        """
        )
        h.execute(eid=ret["id"])
        ret["channels"] = h.fetchall_dict() or []
        ret["packages"] = []

        h = rhnSQL.prepare(
            """
            select p.id as package_id,
                   pn.name,
                   pevr.epoch,
                   pevr.version,
                   pevr.release,
                   pa.label as arch,
                   p.org_id,
                   cv.checksum,
                   cv.checksum_type
              from rhnerratapackage ep
              join rhnpackage p on p.id = ep.package_id
              join rhnpackagename pn on pn.id = p.name_id
              join rhnpackageevr pevr on pevr.id = p.evr_id
              join rhnpackagearch pa on pa.id = p.package_arch_id
              join rhnchecksumview cv on cv.id = p.checksum_id
             where ep.errata_id = :eid
        """
        )
        h.execute(eid=ret["id"])
        packages = h.fetchall_dict() or []
        for pkg in packages:
            ipackage = importLib.IncompletePackage().populate(pkg)
            ipackage["epoch"] = pkg.get("epoch", "")

            ipackage["checksums"] = {ipackage["checksum_type"]: ipackage["checksum"]}
            ret["packages"].append(ipackage)

        return ret

    @staticmethod
    def _is_old_suse_style(notice):
        if (
            (
                notice["from"]
                and "suse" in notice["from"].lower()
                and int(notice["version"]) >= 1000
            )
            or (
                notice["update_id"][:4] in ("res5", "res6")
                and int(notice["version"]) > 6
            )
            or (notice["update_id"][:4] == "res4")
        ):
            # old style suse updateinfo starts with version >= 1000 or
            # have the res update_tag
            return True
        return False

    @staticmethod
    def _delete_invalid_errata(errata_id):
        """
        Remove the errata from all channels
        This should only be alled in case of a disaster
        """
        # first get a list of all channels where this errata exists
        h = rhnSQL.prepare(
            """
            SELECT channel_id
              FROM rhnChannelErrata
             WHERE errata_id = :errata_id
        """
        )
        h.execute(errata_id=errata_id)
        channels = [x["channel_id"] for x in h.fetchall_dict() or []]

        # delete channel from errata
        h = rhnSQL.prepare(
            """
            DELETE FROM rhnChannelErrata
             WHERE errata_id = :errata_id
        """
        )
        h.execute(errata_id=errata_id)

        # delete all packages from errata
        h = rhnSQL.prepare(
            """
            DELETE FROM rhnErrataPackage ep
             WHERE ep.errata_id = :errata_id
        """
        )
        h.execute(errata_id=errata_id)

        # delete files from errata
        h = rhnSQL.prepare(
            """
            DELETE FROM rhnErrataFile
             WHERE errata_id = :errata_id
        """
        )
        h.execute(errata_id=errata_id)

        # delete errata
        # removes also references from rhnErrataCloned
        # and rhnServerNeededCache
        h = rhnSQL.prepare(
            """
            DELETE FROM rhnErrata
             WHERE id = :errata_id
        """
        )
        h.execute(errata_id=errata_id)
        rhnSQL.commit()
        update_needed_cache = rhnSQL.Procedure("rhn_channel.update_needed_cache")

        for cid in channels:
            update_needed_cache(cid)
        rhnSQL.commit()

    @staticmethod
    def _to_db_date(date):
        if not date:
            ret = datetime.utcnow()
        elif date.isdigit():
            try:
                ret = datetime.fromtimestamp(float(date))
            except ValueError:
                # For the case when date is specified in milliseconds
                # fromtimestamp raises the ValueError as the year is out of range
                ret = datetime.fromtimestamp(float(date) / 1000)
        else:
            ret = parse_date(date)
            try:
                ret = ret.astimezone(tzutc())
            except ValueError as e:
                log(2, e)
        return ret.isoformat(" ")[
            :19
        ]  # return 1st 19 letters of date, therefore preventing ORA-01830 caused by fractions of seconds

    def errata_needs_update(
        self, existing_errata, new_errata_version, new_errata_changedate
    ):
        """check, if the errata in the DB needs an update

        new_errata_version: integer version number
        new_errata_changedate: date of the last change in DB format "%Y-%m-%d %H:%M:%S"
        """
        if self.force_all_errata:
            # with force_all_errata always re-import all errata
            return True

        if int(existing_errata["advisory_rel"]) < int(new_errata_version):
            log(2, "Patch need update: higher version")
            return True
        newdate = datetime.strptime(new_errata_changedate, "%Y-%m-%d %H:%M:%S")
        olddate = datetime.strptime(existing_errata["update_date"], "%Y-%m-%d %H:%M:%S")
        if newdate > olddate:
            log(
                2,
                # pylint: disable-next=consider-using-f-string
                "Patch need update: newer update date - %s > %s" % (newdate, olddate),
            )
            return True
        for c in existing_errata["channels"]:
            if self.channel_label == c["label"]:
                log(2, "No update needed")
                return False
        log(2, "Patch need update: channel not yet part of the patch")
        return True

    def _updates_process_packages(self, packages, advisory_name, existing_packages):
        """Check if the packages are in the database

        Go through the list of 'packages' and for each of them
        check to see if it is already present in the database. If it is,
        return a list of IncompletePackage objects, otherwise return an
        empty list.

        :packages: a list of dicts that represent packages (updateinfo style)
        :advisory_name: the name of the current erratum
        :existing_packages: list of already existing packages for this errata

        """
        erratum_packages = existing_packages
        for pkg in packages:
            if pkg["arch"] in ["src", "nosrc"]:
                continue
            param_dict = {
                "name": pkg["name"],
                "version": pkg["version"],
                "release": pkg["release"],
                "arch": pkg["arch"],
                "epoch": pkg["epoch"],
                "channel_id": int(self.channel["id"]),
            }
            if param_dict["arch"] not in self.arches:
                continue
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                if "epoch" not in param_dict:
                    param_dict["epoch"] = ""
                else:
                    # pylint: disable-next=consider-using-f-string
                    param_dict["epoch"] = "%s:" % param_dict["epoch"]
                if (
                    "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s" % param_dict
                    not in self.available_packages
                ):
                    continue
                # This package could not be found in the database
                # but should be available in this repo
                # so we skip the broken patch.
                errmsg = (
                    "The package "
                    "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s "
                    "which is referenced by patch %(patch)s was not found "
                    "in the database. This patch has been skipped."
                    % dict(patch=advisory_name, **param_dict)
                )
                log(0, errmsg)
                self.error_messages.append(errmsg)
                return []

            # add new packages to the errata
            found = False
            for oldpkg in erratum_packages:
                if oldpkg["package_id"] == ret["package_id"]:
                    found = True
            if not found:
                erratum_packages.append(ret)
        return erratum_packages

    # pylint: disable-next=unused-argument
    def _process_package(self, param_dict, advisory_name):
        """Search for a package in the database

        Search for the package specified by 'param_dict' to see if it is
        already present in the database. If it is, return a
        IncompletePackage objects, otherwise return None.

        :param_dict: dict that represent packages (nerva + channel_id)
        :advisory_name: the name of the current erratum

        """
        pkgepoch = param_dict["epoch"]
        del param_dict["epoch"]

        if not pkgepoch or pkgepoch == "0":
            # pylint: disable-next=invalid-name
            epochStatement = "(pevr.epoch is NULL or pevr.epoch = '0')"
        else:
            # pylint: disable-next=invalid-name
            epochStatement = "pevr.epoch = :epoch"
            param_dict["epoch"] = pkgepoch
        if self.org_id:
            param_dict["org_id"] = self.org_id
            # pylint: disable-next=invalid-name
            orgidStatement = " = :org_id"
        else:
            # pylint: disable-next=invalid-name
            orgidStatement = " is NULL"

        h = rhnSQL.prepare(
            # pylint: disable-next=consider-using-f-string
            """
            select p.id, c.checksum, c.checksum_type, pevr.epoch
              from rhnPackage p
              join rhnPackagename pn on p.name_id = pn.id
              join rhnpackageevr pevr on p.evr_id = pevr.id
              join rhnpackagearch pa on p.package_arch_id = pa.id
              join rhnArchType at on pa.arch_type_id = at.id
              join rhnChecksumView c on p.checksum_id = c.id
              join rhnChannelPackage cp on p.id = cp.package_id
             where pn.name = :name
               and p.org_id %s
               and pevr.version = :version
               and pevr.release = :release
               and pa.label = :arch
               and %s
               and at.label = 'rpm'
               and cp.channel_id = :channel_id
            """
            % (orgidStatement, epochStatement)
        )
        h.execute(**param_dict)
        cs = h.fetchone_dict()

        if not cs:
            return None

        package = importLib.IncompletePackage()
        for k in param_dict:
            if k not in ["epoch", "channel_label", "channel_id"]:
                package[k] = param_dict[k]
        package["epoch"] = cs["epoch"]
        package["org_id"] = self.org_id

        package["checksums"] = {cs["checksum_type"]: cs["checksum"]}
        package["checksum_type"] = cs["checksum_type"]
        package["checksum"] = cs["checksum"]

        package["package_id"] = cs["id"]
        return package

    @staticmethod
    def get_compatible_arches(channel_id):
        """Return a list of compatible package arch labels for this channel"""
        h = rhnSQL.prepare(
            """select pa.label
                              from rhnChannelPackageArchCompat cpac,
                              rhnChannel c,
                              rhnpackagearch pa
                              where c.id = :channel_id
                              and c.channel_arch_id = cpac.channel_arch_id
                              and cpac.package_arch_id = pa.id"""
        )
        h.execute(channel_id=channel_id)
        # pylint: disable-next=invalid-name
        with cfg_component("server.susemanager") as CFG:
            arches = [
                k["label"]
                for k in h.fetchall_dict()
                if CFG.SYNC_SOURCE_PACKAGES or k["label"] not in ["src", "nosrc"]
            ]
        return arches

    @staticmethod
    def _update_keywords(notice):
        """Return a list of Keyword objects for the notice"""
        keywords = []
        if notice["reboot_suggested"]:
            kw = importLib.Keyword()
            kw.populate({"keyword": "reboot_suggested"})
            keywords.append(kw)
        if notice["restart_suggested"]:
            kw = importLib.Keyword()
            kw.populate({"keyword": "restart_suggested"})
            keywords.append(kw)
        return keywords

    @staticmethod
    def _update_bugs(notice):
        """Return a list of Bug objects from the notice's references"""
        bugs = {}
        if notice["references"] is None:
            return []
        for bz in notice["references"]:
            if bz["type"] == "bugzilla":
                # Fix: in case of non-integer id try to parse it from href
                if not bz["id"].isdigit():
                    log(
                        2,
                        # pylint: disable-next=consider-using-f-string
                        "Bugzilla ID is wrong: {0}. Trying to parse ID from from URL".format(
                            bz["id"]
                        ),
                    )
                    # pylint: disable-next=anomalous-backslash-in-string
                    bz_id_match = re.search("/show_bug.cgi\?id=(\d+)", bz["href"])
                    if bz_id_match:
                        bz["id"] = bz_id_match.group(1)
                        # pylint: disable-next=consider-using-f-string
                        log(2, "Bugzilla ID found: {0}".format(bz["id"]))
                    else:
                        log2(
                            0,
                            0,
                            # pylint: disable-next=consider-using-f-string
                            "Unable to find Bugzilla ID for {0}. Omitting".format(
                                bz["id"]
                            ),
                            stream=sys.stderr,
                        )
                        continue
                if bz["id"] not in bugs:
                    bug = importLib.Bug()
                    bug.populate(
                        {
                            "bug_id": bz["id"],
                            # pylint: disable-next=consider-using-f-string
                            "summary": bz["title"] or ("Bug %s" % bz["id"]),
                            "href": bz["href"],
                        }
                    )
                    bugs[bz["id"]] = bug
        return list(bugs.values())

    @staticmethod
    def _update_cve(notice):
        """Return a list of unique ids from notice references of type 'cve'"""
        cves = []
        if notice["description"] is not None:
            # sometimes CVE numbers appear in the description, but not in
            # the reference list
            cves = LzRepoSync.find_cves(notice["description"])
        if notice["references"] is not None:
            cves.extend(
                [cve["id"][:20] for cve in notice["references"] if cve["type"] == "cve"]
            )
        # remove duplicates
        cves = list(set(cves))

        return cves

    @staticmethod
    def find_cves(text):
        """Find and return a list of CVE ids

        Matches:
         - CVE-YEAR-NUMBER

         Beginning 2014, the NUMBER has no maximal length anymore.
         We limit the length at 20 chars, because of the DB column size
        """
        cves = list()
        # pylint: disable-next=anomalous-backslash-in-string
        cves.extend([cve[:20] for cve in set(re.findall("CVE-\d{4}-\d+", text))])
        return cves
