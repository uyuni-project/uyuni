#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2020 Red Hat, Inc.
# Copyright (c) 2010--2011 SUSE LINUX Products GmbH, Nuernberg, Germany.
# Copyright (c) 2020--2021  Stefan Bluhm, Germany.
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

import dnf
import hashlib
import hawkey
import logging
import re
import os.path
import xml.etree.ElementTree as etree
from os import makedirs
from shutil import rmtree
from libdnf.conf import ConfigParser
from dnf.exceptions import Error, RepoError
from uyuni.common import checksum, fileutils

# pylint: disable-next=unused-import
from spacewalk.common.suseLib import get_proxy
from spacewalk.common.rhnConfig import cfg_component

# pylint: disable-next=unused-import
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.satellite_tools.repo_plugins import CACHE_DIR, ContentPackage
from spacewalk.satellite_tools.repo_plugins.yum_src import (
    ContentSource as zypper_ContentSource,
)

# pylint: disable-next=unused-import
from spacewalk.satellite_tools.repo_plugins.yum_src import (
    RepoMDError,
    UpdateNotice,
    UpdateNoticeException,
)

# pylint: disable-next=unused-import
from urllib.parse import urlparse, urlsplit, urlunparse


YUMSRC_CONF = "/etc/rhn/spacewalk-repo-sync/yum.conf"


logging.basicConfig()
log = logging.getLogger(__name__)


class RawSolvablePackage:
    """Represents a hawkey package in RawSolvablePackage format required by reposync.. #:api"""

    # pylint: disable-next=redefined-outer-name
    def __init__(self, hawkey):
        self.name = hawkey.name
        self.epoch = hawkey.epoch
        self.version = hawkey.version
        self.release = hawkey.release
        self.arch = hawkey.arch
        # Unclear if checksum info is used and if it is the correct format. Probably no to both.
        self.checksum_type, self.checksum = hawkey.chksum
        self.packagesize = hawkey.downloadsize
        self.relativepath = hawkey.location
        # Unclear what the rawname really is.
        self.raw_name = (
            self.name + "-" + self.version + "-" + self.release + "." + self.arch
        )

    def __repr__(self):
        # pylint: disable-next=consider-using-f-string
        return "RawSolvablePackage({})".format(self.raw_name)

    def __str__(self):
        return f"RawSolvablePackage: name = {self.name}, raw_name = {self.raw_name}, epoch = {self.epoch}, version = {self.version}, release = {self.release}, arch = {self.arch}, checksum_type = {self.checksum_type}, checksum = {self.checksum}, packagesize = {self.packagesize}, relativepath = {self.relativepath}"


# pylint: disable-next=missing-class-docstring
class ContentSource(zypper_ContentSource):
    # pylint: disable-next=super-init-not-called
    def __init__(
        self,
        url,
        name,
        insecure=False,
        interactive=False,
        yumsrc_conf=YUMSRC_CONF,
        org="1",
        channel_label="",
        no_mirrors=False,
        ca_cert_file=None,
        client_cert_file=None,
        client_key_file=None,
        channel_arch="",
        http_headers=None,
    ):
        # insecure and interactive are not implemented for this module.
        """
        Plugin constructor.
        """

        name = re.sub("[^a-zA-Z0-9_.:-]+", "_", name)
        if urlsplit(url).scheme:
            self.url = url
        else:
            # pylint: disable-next=consider-using-f-string
            self.url = "file://%s" % url
        self.name = name
        self.insecure = insecure
        self.interactive = interactive
        self.org = org if org else "NULL"
        self.proxy_hostname = None
        self.proxy_url = None
        self.proxy_user = None
        self.proxy_pass = None
        self.authtoken = None
        self.sslcacert = ca_cert_file
        self.sslclientcert = client_cert_file
        self.sslclientkey = client_key_file
        self.http_headers = {}

        self.dnfbase = dnf.Base()
        self.dnfbase.conf.read(yumsrc_conf)
        if not os.path.exists(yumsrc_conf):
            self.dnfbase.conf.read("/dev/null")
        self.configparser = (
            ConfigParser()
        )  # Reading config file directly as dnf only ready MAIN section.
        self.configparser.setSubstitutions(dnf.Base().conf.substitutions)
        self.configparser.read(yumsrc_conf)
        self.dnfbase.conf.cachedir = os.path.join(CACHE_DIR, self.org)

        # read the proxy configuration
        # /etc/rhn/rhn.conf has more priority than yum.conf
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            # keep authtokens for mirroring
            # pylint: disable-next=invalid-name,unused-variable
            (_scheme, _netloc, _path, query, _fragid) = urlsplit(url)
            if query:
                self.authtoken = query

            # load proxy configuration based on the url
            self._load_proxy_settings(self.url)

            # perform authentication if implemented
            self._authenticate(url)

            # Check for settings in yum configuration files (for custom repos/channels only)
            if org:
                repos = self.dnfbase.repos
            else:
                repos = None
            if repos and name in repos:
                repo = repos[name]
            elif repos and channel_label in repos:
                repo = repos[channel_label]
                # In case we are using Repo object based on channel config, override it's id to name of the repo
                # To not create channel directories in cache directory
                repo.id = name
            else:
                # Not using values from config files
                repo = dnf.repo.Repo(name, self.dnfbase.conf)
                repo.repofile = yumsrc_conf
                # pylint: disable=W0212
                repo._populate(self.configparser, name, yumsrc_conf)

            self.repo = repo

            self.yumbase = self.dnfbase  # for compatibility

            self.setup_repo(
                repo, no_mirrors, ca_cert_file, client_cert_file, client_key_file
            )
            self.num_packages = 0
            self.num_excluded = 0
            self.groupsfile = None

            # configure network connection
            try:
                # bytes per second
                self.minrate = int(CFG.REPOSYNC_MINRATE)
            except ValueError:
                self.minrate = 1000
            try:
                # seconds
                self.timeout = int(CFG.REPOSYNC_TIMEOUT)
            except ValueError:
                self.timeout = 300

            self.repo = self.dnfbase.repos[self.repoid]
            self.get_metadata_paths()

    def __del__(self):
        # close log files for yum plugin
        for handler in logging.getLogger("dnf").handlers:
            handler.close()
        self.dnfbase.close()

    def setup_repo(
        self, repo, no_mirrors, ca_cert_file, client_cert_file, client_key_file
    ):
        """
        Setup repository and fetch metadata
        """
        repo.metadata_expire = 0
        repo.mirrorlist = self.url
        repo.baseurl = [self.url]
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            pkgdir = os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, self.org, "stage")
        if not os.path.isdir(pkgdir):
            # pylint: disable-next=invalid-name
            with cfg_component("server.satellite") as CFG:
                fileutils.makedirs(pkgdir, user=CFG.httpd_user, group=CFG.httpd_group)
        repo.pkgdir = pkgdir
        repo.sslcacert = ca_cert_file
        repo.sslclientcert = client_cert_file
        repo.sslclientkey = client_key_file
        repo.proxy = None
        repo.proxy_username = None
        repo.proxy_password = None

        if self.proxy_url:
            repo.proxy = (
                self.proxy_url
                if "://" in self.proxy_url
                else "http://" + self.proxy_url
            )
            repo.proxy_username = self.proxy_user
            repo.proxy_password = self.proxy_pass

        if no_mirrors:
            repo.mirrorlist = ""
        self.digest = hashlib.sha256(self.url.encode("utf8")).hexdigest()[:16]
        self.dnfbase.repos.add(repo)
        self.repoid = repo.id
        # Try loading the repo configuration
        try:
            self.dnfbase.repos[self.repoid].load()
            # Repo config loaded successfully.
            # Verify whether the supplied mirror list is working as intended. Otherwise don't use mirror lists.
            try:
                if not self.clean_urls(
                    # pylint: disable-next=protected-access
                    self.dnfbase.repos[self.repoid]._repo.getMirrors()
                ):
                    no_mirrors = True
                    # Reload repo just in case.
                    repo.mirrorlist = ""
                    self.dnfbase.repos[self.repoid].load()
            # pylint: disable-next=bare-except
            except:
                no_mirrors = True
                # Reload repo just in case.
                repo.mirrorlist = ""
                self.dnfbase.repos[self.repoid].load()
        except RepoError as exc:
            # pylint: disable-next=raise-missing-from
            raise RepoMDError(exc)

        # Do not try to expand baseurl to other mirrors
        if no_mirrors:
            self.dnfbase.repos[self.repoid].urls = repo.baseurl
            # Make sure baseurl ends with / and urljoin will work correctly
            if self.dnfbase.repos[self.repoid].urls[0][-1] != "/":
                self.dnfbase.repos[self.repoid].urls[0] += "/"
        else:
            self.dnfbase.repos[self.repoid].urls = self.clean_urls(
                # pylint: disable-next=protected-access
                self.dnfbase.repos[self.repoid]._repo.getMirrors()
            )  # pylint: disable=W0212
            self.dnfbase.repos[self.repoid].urls = [
                url for url in self.dnfbase.repos[self.repoid].urls if "?" not in url
            ]
        self.dnfbase.repos[self.repoid].basecachedir = os.path.join(CACHE_DIR, self.org)
        # pylint: disable-next=invalid-name
        repoXML = type("", (), {})()
        repoXML.repoData = {}
        self.dnfbase.repos[self.repoid].repoXML = repoXML

    @staticmethod
    def clean_urls(urls):
        """
        Filters a url schema for http, https, ftp, file only.
        :return: urllist (string)
        """
        cleaned = []
        for url in urls:
            s = dnf.pycomp.urlparse.urlparse(url)[0]
            if s in ("http", "ftp", "file", "https"):
                cleaned.append(url)
        return cleaned

    def number_of_packages(self):
        for dummy_index in range(3):
            try:
                self.dnfbase.fill_sack(load_system_repo=False)
                break
            except RepoError:
                pass
        return len(self.dnfbase.sack)

    def raw_list_packages(self, filters=None):
        for dummy_index in range(3):
            try:
                self.dnfbase.fill_sack(
                    load_system_repo=False, load_available_repos=True
                )
                break
            except RepoError:
                pass

        rawpkglist = self.dnfbase.sack.query(flags=hawkey.IGNORE_MODULAR_EXCLUDES).run()
        self.num_packages = len(rawpkglist)

        if not filters:
            filters = []
            # if there's no include/exclude filter on command line or in database
            for p in self.dnfbase.repos[self.repoid].includepkgs:
                filters.append(("+", [p]))
            for p in self.dnfbase.repos[self.repoid].exclude:
                filters.append(("-", [p]))

        if filters:
            rawpkglist = self._filter_packages(rawpkglist, filters)
            rawpkglist = self._get_package_dependencies(self.dnfbase.sack, rawpkglist)
            self.num_excluded = self.num_packages - len(rawpkglist)

        for pack in rawpkglist:
            pack.packagesize = pack.downloadsize
            pack.checksum_type = pack.returnIdSum()[0]
            pack.checksum = pack.returnIdSum()[1]

        return rawpkglist

    def list_packages(self, filters, latest):
        """list packages"""
        self.dnfbase.fill_sack(load_system_repo=False, load_available_repos=True)
        pkglist = self.dnfbase.sack.query(flags=hawkey.IGNORE_MODULAR_EXCLUDES)
        self.num_packages = len(pkglist)
        if latest:
            pkglist = pkglist.latest()
        pkglist = list(dict.fromkeys(pkglist))  # Filter out duplicates

        if not filters:
            # if there's no include/exclude filter on command line or in database
            # check repository config file
            for p in self.dnfbase.repos[self.repoid].includepkgs:
                filters.append(("+", [p]))
            for p in self.dnfbase.repos[self.repoid].exclude:
                filters.append(("-", [p]))

        filters = self._expand_package_groups(filters)

        if filters:
            pkglist = self._filter_packages(pkglist, filters)
            pkglist = self._get_package_dependencies(self.dnfbase.sack, pkglist)

            self.num_excluded = self.num_packages - len(pkglist)
        to_return = []
        for pack in pkglist:
            if pack.arch == "src":
                continue
            new_pack = ContentPackage()
            try:
                new_pack.setNVREA(
                    pack.name, pack.version, pack.release, pack.epoch, pack.arch
                )
            except ValueError as e:
                log(0, "WARNING: package contains incorrect metadata. SKIPPING!")
                log(0, e)
                continue
            new_pack.unique_id = RawSolvablePackage(pack)
            new_pack.checksum_type = pack.returnIdSum()[0]
            if new_pack.checksum_type == "sha":
                new_pack.checksum_type = "sha1"
            new_pack.checksum = pack.returnIdSum()[1]
            to_return.append(new_pack)
        return to_return

    @staticmethod
    def _find_comps_type(comps_type, environments, groups, name):
        # Finds environment or regular group by name or label
        found = None
        if comps_type == "environment":
            for e in environments:
                if e.id == name or e.name == name:
                    found = e
                    break
        elif comps_type == "group":
            for g in groups:
                if g.id == name or g.name == name:
                    found = g
                    break
        return found

    def _expand_comps_type(self, comps_type, environments, groups, filters):
        new_filters = []
        # Rebuild filter list
        for sense, pkg_list in filters:
            new_pkg_list = []
            for pkg in pkg_list:
                # Package group id
                if pkg and pkg[0] == "@":
                    group_name = pkg[1:].strip()
                    found = self._find_comps_type(
                        comps_type, environments, groups, group_name
                    )
                    if found and comps_type == "environment":
                        # Save expanded groups to the package list
                        new_pkg_list.extend(
                            ["@" + grp.name for grp in found.groups_iter()]
                        )
                    elif found and comps_type == "group":
                        # Replace with package list, simplified to not evaluate if packages are default, optional etc.
                        for package in found.packages:
                            new_pkg_list.append(str(package.name))
                    else:
                        # Invalid group, save group id back
                        new_pkg_list.append(pkg)
                else:
                    # Regular package
                    new_pkg_list.append(pkg)
            if new_pkg_list:
                new_filters.append((sense, new_pkg_list))
        return new_filters

    def _expand_package_groups(self, filters):
        if not self.groupsfile:
            return filters
        comps = dnf.comps.Comps()
        # pylint: disable=W0212
        comps._add_from_xml_filename(self.groupsfile)
        groups = comps.groups

        if hasattr(comps, "environments"):
            # First expand environment groups, then regular groups
            environments = comps.environments
            filters = self._expand_comps_type(
                "environment", environments, groups, filters
            )
        else:
            environments = []
        filters = self._expand_comps_type("group", environments, groups, filters)
        return filters

    @staticmethod
    # pylint: disable-next=invalid-name
    def __parsePackages(pkgSack, pkgs):
        """
        Substitute for yum's parsePackages.
        The function parses a list of package names and returns their Hawkey
        list if it exists in the package sack. Inputs are a package sack and
        a list of packages. Returns a list of latest existing packages in
        Hawkey format.
        """

        matches = set()
        for pkg in pkgs:
            hkpkgs = set()
            subject = dnf.subject.Subject(pkg)
            hkpkgs |= set(subject.get_best_selector(pkgSack, obsoletes=True).matches())
            if len(matches) == 0:
                matches = hkpkgs
            else:
                matches |= hkpkgs
        result = list(matches)
        a = pkgSack.query(
            flags=hawkey.IGNORE_MODULAR_EXCLUDES
        ).available()  # Load all available packages from the repository
        result = a.filter(pkg=result).latest().run()
        return result

    # pylint: disable-next=arguments-renamed,arguments-renamed,arguments-renamed
    def _filter_packages(self, packages, filters):
        """implement include / exclude logic
        filters are: [ ('+', includelist1), ('-', excludelist1),
                       ('+', includelist2), ... ]
        """
        if filters is None:
            return []

        selected = []
        excluded = []
        if filters[0][0] == "-":
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        sack = self.dnfbase.sack
        for filter_item in filters:
            sense, pkg_list = filter_item
            # pylint: disable-next=invalid-name
            convertFilterToPackagelist = self.__parsePackages(sack, pkg_list)
            if sense == "+":
                # include
                matched = list()
                for (
                    v1
                ) in (
                    convertFilterToPackagelist
                ):  # Use only packages that are in pkg_list
                    for v2 in excluded:
                        if v1 == v2 and v1 not in matched:
                            matched.append(v1)
                allmatched = list(dict.fromkeys(matched))  # remove duplicates
                selected = list(
                    dict.fromkeys(selected + allmatched)
                )  # remove duplicates
                for pkg in allmatched:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == "-":
                # exclude
                matched = list()
                for (
                    v1
                ) in (
                    convertFilterToPackagelist
                ):  # Use only packages that are in pkg_list
                    for v2 in selected:
                        if v1 == v2 and v1 not in matched:
                            matched.append(v1)
                allmatched = list(dict.fromkeys(matched))  # remove duplicates
                for pkg in allmatched:
                    if pkg in selected:
                        selected.remove(pkg)
                allmatched = list(allmatched)
                excluded = excluded + allmatched
                excluded = list(dict.fromkeys(excluded))  # Filter out duplicates
            else:
                # pylint: disable-next=consider-using-f-string
                raise Error("Invalid filter sense: '%s'" % sense)
        return selected

    @staticmethod
    # pylint: disable-next=invalid-name
    def __findDeps(pkgSack, pkgs):
        #
        #        Input: Sack, list of packages
        #        Output: List of packages
        #
        results = {}
        a = pkgSack.query(flags=hawkey.IGNORE_MODULAR_EXCLUDES).available()
        for pkg in pkgs:
            results[pkg] = {}
            reqs = pkg.requires
            pkgresults = results[pkg]
            for req in reqs:
                if str(req).startswith("rpmlib("):
                    continue
                satisfiers = []
                for po in a.filter(provides=req).latest():
                    satisfiers.append(po)
                pkgresults[req] = satisfiers
        return results

    def _get_package_dependencies(self, sack, packages):
        self.dnfbase.pkgSack = sack
        known_deps = set()
        resolved_deps = self.__findDeps(self.dnfbase.pkgSack, packages)
        while resolved_deps:
            next_level_deps = []
            for deps in resolved_deps.values():
                # pylint: disable-next=invalid-name
                for _dep, dep_packages in deps.items():
                    if _dep not in known_deps:
                        next_level_deps.extend(dep_packages)
                        packages.extend(dep_packages)
                        known_deps.add(_dep)

            resolved_deps = self.__findDeps(self.dnfbase.pkgSack, next_level_deps)

        return list(dict.fromkeys(packages))

    def get_package(self, package, metadata_only=False):
        """get package"""
        pack = package.hawkey_id
        check = (self.verify_pkg, (pack, 1), {})
        if metadata_only:
            # Include also data before header section
            pack.hdrstart = 0
            data = self.repo.getHeader(pack, checkfunc=check)
        else:
            data = self.repo.getPackage(pack, checkfunc=check)
        return data

    @staticmethod
    # pylint: disable-next=invalid-name
    def verify_pkg(_fo, pkg, _fail):
        return pkg.verifyLocalPkg()

    def get_md_checksum_type(self):
        """
        Return the checksum type of the primary.xml if exists, otherwise
        default output is "sha1".
        :returns: str
        """
        if self.dnfbase.repos[self.repoid].repoXML.repoData["primary"].checksum:
            return self.dnfbase.repos[self.repoid].repoXML.repoData["primary"].checksum
        return "sha1"

    def clear_cache(self, directory=None, keep_repomd=False):
        if directory is None:
            directory = os.path.join(CACHE_DIR, self.org, self.name + "-" + self.digest)

        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path) and not (keep_repomd and item == "repomd.xml"):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

        # restore empty directories
        makedirs(directory + "/packages", int("0755", 8))
        makedirs(directory + "/gen", int("0755", 8))
        makedirs(directory + "/repodata", int("0755", 8))
        self.dnfbase.repos[self.repoid].load()

    def _md_exists(self, tag):
        """
        Check if the requested metadata exists on the repository
        :returns: bool
        """
        return bool(self.dnfbase.repos[self.repoid].get_metadata_content(tag))

    def _retrieve_md_path(self, tag):
        """
        Return the path to the requested metadata if exists
        :returns: str
        """

        if self.dnfbase.repos[self.repoid].get_metadata_path(tag):
            return self.dnfbase.repos[self.repoid].get_metadata_path(tag)

        return None

    def _get_repodata_path(self):
        """
        Return the path to the repository repodata directory
        :returns: str
        """
        return self.dnfbase.repos[
            self.repoid
        ].pkgdir  # Not sure of the really desired path

    def get_groups(self):
        groups = self.repo.get_metadata_path("group_gz")
        if groups == "":
            groups = self.repo.get_metadata_path("group")
        if groups == "":
            groups = None
        return groups

    def repomd_up_to_date(self):
        repomd_old_path = os.path.join(self.repo.basecachedir, self.name, "repomd.xml")
        # No cached repomd?
        if not os.path.isfile(repomd_old_path):
            return False
        repomd_new_path = os.path.join(
            self.repo.basecachedir, self.name, "repomd.xml.new"
        )
        # Newer file not available? Don't do anything. It should be downloaded before this.
        if not os.path.isfile(repomd_new_path):
            return True
        return checksum.getFileChecksum(
            "sha256", filename=repomd_old_path
        ) == checksum.getFileChecksum("sha256", filename=repomd_new_path)

    # Simply load primary and updateinfo path from repomd
    def get_metadata_paths(self):
        def get_location(data_item):
            for sub_item in data_item:
                if sub_item.tag.endswith("location"):
                    return sub_item.attrib.get("href")
            return None

        def get_checksum(data_item):
            for sub_item in data_item:
                if sub_item.tag.endswith("checksum"):
                    return sub_item.attrib.get("type"), sub_item.text
            return None

        def get_timestamp(data_item):
            for sub_item in data_item:
                if sub_item.tag.endswith("timestamp"):
                    return sub_item.text
            return None

        repomd_path = os.path.join(
            self.dnfbase.repos[self.repoid].basecachedir,
            self.name + "-" + self.digest,
            "repodata",
            "repomd.xml",
        )
        if not os.path.isfile(repomd_path):
            raise RepoMDError(repomd_path)
        repomd = open(repomd_path, "rb")
        files = {}
        # pylint: disable-next=invalid-name,unused-variable
        for _event, elem in etree.iterparse(repomd):
            if elem.tag.endswith("data"):
                # pylint: disable-next=invalid-name
                repoData = type("", (), {})()
                if elem.attrib.get("type") == "primary_db":
                    files["primary"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                elif elem.attrib.get("type") == "primary" and "primary" not in files:
                    files["primary"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                    repoData.checksum = get_checksum(elem)
                elif elem.attrib.get("type") == "updateinfo":
                    files["updateinfo"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                elif elem.attrib.get("type") == "group_gz":
                    files["group"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                elif elem.attrib.get("type") == "group" and "group" not in files:
                    files["group"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                elif elem.attrib.get("type") == "modules":
                    files["modules"] = (get_location(elem), get_checksum(elem))
                    repoData.timestamp = get_timestamp(elem)
                self.dnfbase.repos[self.repoid].repoXML.repoData[
                    elem.attrib.get("type")
                ] = repoData
        repomd.close()
        return files.values()
