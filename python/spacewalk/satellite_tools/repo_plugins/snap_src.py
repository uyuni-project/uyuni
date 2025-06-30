#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2016--2017 Red Hat, Inc.
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

import sys
import os.path
from shutil import rmtree
from shutil import copyfile
import time
import re
import fnmatch
import requests
import logging
from functools import cmp_to_key
from uyuni.common import fileutils
from spacewalk.common.suseLib import get_proxy
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.syncLib import log2
from spacewalk.server import rhnSQL
from spacewalk.common import repo

import looseversion

try:
    #  python 2
    from urllib import unquote
    import urlparse
except ImportError:
    #  python3
    import urllib.parse as urlparse  # pylint: disable=F0401,E0611
    from urllib.parse import unquote

RETRIES = 10
RETRY_DELAY = 1
FORMAT_PRIORITY = [".xz", ".gz", ""]
log = logging.getLogger(__name__)

class UniqueIDWrapper:
    def __init__(self, unique_id_str, relativepath):
        self.value = unique_id_str  # optional: retain original string
        self.relativepath = relativepath

    def __str__(self):
        return self.value  # behave like a string if needed
# pylint: disable-next=missing-class-docstring
class SnapPackage(ContentPackage):
    def __init__(self):
        self.name = None
        self.version = None
        self.revision = None
        self.channel = None
        self.arch = None
        self.download_url = None
        self.checksum_type = None
        self.checksum = None

    def __getitem__(self, key):
        return getattr(self, key)

    def __setitem__(self, key, value):
        return setattr(self, key, value)

    def evr(self):
        # The format is: [epoch:]upstream_version[-debian_revision].
        # https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
        evr = ""
        if self.epoch:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}:".format(self.epoch)
        if self.version:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}".format(self.version)
        if self.release:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "-{}".format(self.release)
        return evr

    def nevra(self):
        return f"{self.name}_{self.evr()}_{self.arch}"

    def is_populated(self):
        return all(
            [
                attribute is not None
                for attribute in (
                    self.name,
                    self.epoch,
                    self.version,
                    self.release,
                    self.arch,
                    self.relativepath,
                    self.checksum_type,
                    self.checksum,
                )
            ]
        )


# pylint: disable-next=missing-class-docstring
class SnapRepo:

    def __init__(
        self,
        url,
        cache_dir,
        pkg_dir,
        proxy_addr="",
        proxy_user="",
        proxy_pass="",
        gpg_verify=True,
        channel_label=None,
        timeout=None,
    ):
        self.url = url
        parsed_url = urlparse.urlparse(url)
        self.base_url = [f"{parsed_url.scheme}://{parsed_url.netloc}/"]
        self.urls = self.base_url

        self.sslclientcert = self.sslclientkey = self.sslcacert = None
        self.proxy = proxy_addr
        self.proxy_username = proxy_user
        self.proxy_password = proxy_pass
        self.gpg_verify = gpg_verify

        self.basecachedir = cache_dir
        if not os.path.isdir(self.basecachedir):
            # pylint: disable-next=invalid-name
            with cfg_component(component=None) as CFG:
                fileutils.makedirs(
                    self.basecachedir, user=CFG.httpd_user, group=CFG.httpd_group
                )
        self.includepkgs = []
        self.exclude = []
        self.pkgdir = pkg_dir
        self.http_headers = {
            "User-Agent": "curl/7.68.0",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }
    def verify(self):
        """
        Verify package index checksum and signature.

        :return:
        """
        dpkg_repo = repo.DpkgRepo(
            self.url, self._get_proxies(), self.gpg_verify, self.timeout
        )
        log.debug("DebRepo.verify() dpkg_repo=%s", dpkg_repo)
        if not dpkg_repo.verify_packages_index():
            raise repo.GeneralRepoException("Package index checksum failure")

    def _get_proxies(self):
        """
        Returns proxies dict for requests with python-requests.
        """
        if self.proxy:
            (_, netloc, _, _, _) = urlparse.urlsplit(self.proxy)
            proxies = {"http": "http://" + netloc, "https": "http://" + netloc}
            if self.proxy_username and self.proxy_password:
                proxies = {
                    "http": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                    "https": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                }
            return proxies
        else:
            return None
    def get_package_list(self):
        to_return = []
        seen = set()
        base_url = "https://api.snapcraft.io/api/v1/snaps/search?q={}&limit=100&page={}"

        headers = {
            "User-Agent": "snapd/2.63",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }

        for ch in range(ord("y"), ord("y") + 1):  # Change range if needed
            keyword = chr(ch)
            page = 1
            same_count = 0
            prev_count = 0

            while True:
                url = base_url.format(keyword, page)
                try:
                    res = requests.get(url, headers=headers)
                    if res.status_code != 200:
                        print(f"[ERROR] HTTP {res.status_code} for keyword '{keyword}' page {page}")
                        break

                    data = res.json()
                    packages = data.get("_embedded", {}).get("clickindex:package", [])
                    if not packages:
                        break

                    for pkg in packages:
                        name = pkg.get("package_name")
                        if not name or name in seen:
                            continue

                        seen.add(name)
                        arch_list = pkg.get("architecture", [])
                        version = pkg.get("version")
                        checksum = pkg.get("download_sha512", "")
                        download_url = pkg.get("download_url")

                        if not download_url:
                            continue  # Skip if no download URL

                        for arch in arch_list or ["noarch"]:
                            sp = SnapPackage()
                            #sp = ContentPackage()
                            sp.name = name
                            sp.version = version
                            sp.unique_id = sp
                            sp.epoch = ""
                            sp.arch = arch
                            sp.download_url = download_url
                            sp.checksum_type = "sha512"
                            sp.checksum = checksum
                            sp.unique_id = pkg.get("snap_id")
                            snap_id = pkg.get("snap_id")
                            revision = str(pkg.get("revision", "1"))
                            sp.release = revision
                            relativepath = f"{snap_id}_{revision}.snap"
                            unique_id = f"{snap_id}_{revision}_{arch}"

                            sp.relativepath = relativepath
                            sp.unique_id = UniqueIDWrapper(unique_id, relativepath)

                            to_return.append(sp)

                    curr_count = len(to_return)
                    if curr_count == prev_count:
                        same_count += 1
                    else:
                        same_count = 0
                        prev_count = curr_count

                    if same_count >= 2:
                        break

                    page += 1
                    time.sleep(0.2)

                except Exception as e:
                    print(f"[ERROR] Exception for keyword '{keyword}' page {page}: {e}")
                    break

        print(f"[DEBUG] Total snap packages collected: {len(to_return)}")
        return to_return[:5]

# pylint: disable-next=missing-class-docstring
#
class ContentSource:
    def __init__(
        self,
        url,
        name,
        insecure=False,
        interactive=True,
        yumsrc_conf=None,
        org="1",
        channel_label="",
        no_mirrors=False,
        ca_cert_file=None,
        client_cert_file=None,
        client_key_file=None,
        channel_arch="",
        http_headers=None,
    ):
        self.url = url
        self.name = name
        if org:
            self.org = org
        else:
            self.org = "NULL"

        # read the proxy configuration in /etc/rhn/rhn.conf
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            self.proxy_addr, self.proxy_user, self.proxy_pass = get_proxy(self.url)
            self.authtoken = None

            # Replace non-valid characters from reponame (only alphanumeric chars allowed)
            self.reponame = "".join([x if x.isalnum() else "_" for x in self.name])
            self.channel_label = channel_label

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

            try:
                # extended reposync nevra filter enable
                # this will filter packages based on full nevra
                # instead of package name only.
                self.nevra_filter = bool(CFG.REPOSYNC_NEVRA_FILTER)
            except (AttributeError, ValueError):
                self.nevra_filter = False

            # SUSE vendor repositories belongs to org = NULL
            # The repository cache root will be "/var/cache/rhn/reposync/REPOSITORY_LABEL/"
            root = os.path.join(CACHE_DIR, str(org or "NULL"), self.reponame)
            self.repo = SnapRepo(
                url,
                root,
                os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, self.org, "stage"),
                self.proxy_addr,
                self.proxy_user,
                self.proxy_pass,
                gpg_verify=not (insecure),
                channel_label=channel_label,
                timeout=self.timeout,
            )
            self.repo.http_headers = http_headers


            self.num_packages = 0
            self.num_excluded = 0

            # keep authtokens for mirroring
            # pylint: disable-next=invalid-name,unused-variable
            (_scheme, _netloc, _path, query, _fragid) = urlparse.urlsplit(url)
            if query:
                self.authtoken = query



    def list_packages(self, filters, latest):
        print("[DEBUG] list_packages called")
        pkgs = self.repo.get_package_list()
        self.num_packages = len(pkgs)
        return pkgs
    def get_md_checksum_type(self):
        pass

    def get_products(self):
        # No products
        return []

    def get_susedata(self):
        # No susedata
        return []

    def get_mediaproducts(self):
        # No mediaproducts data
        return None


    @staticmethod
    def _filter_packages(packages, filters, nevra_filter=False):
        """implement include / exclude logic
        filters are: [ ('+', includelist1), ('-', excludelist1),
                       ('+', includelist2), ... ]
        """
        if filters is None:
            return

        selected = []
        excluded = []
        allmatched_include = []
        allmatched_exclude = []
        if filters[0][0] == "-":
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        for filter_item in filters:
            sense, pkg_list = filter_item
            regex = fnmatch.translate(pkg_list[0])
            reobj = re.compile(regex)
            if sense == "+":
                # include
                for excluded_pkg in excluded:
                    if nevra_filter:
                        pkg_name = excluded_pkg.nevra()
                    else:
                        pkg_name = excluded_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_include.insert(0, excluded_pkg)
                        selected.insert(0, excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == "-":
                # exclude
                for selected_pkg in selected:
                    if nevra_filter:
                        pkg_name = selected_pkg.nevra()
                    else:
                        pkg_name = selected_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_exclude.insert(0, selected_pkg)
                        excluded.insert(0, selected_pkg)
                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = excluded + allmatched_exclude
            else:
                raise IOError("Filters are malformed")
        return selected

    def clear_cache(self, directory=None):
        if directory is None:
            directory = self.repo.basecachedir
        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

    @staticmethod
    def get_updates():
        # There isn't any update info in the repository
        return "", []

    @staticmethod
    def get_groups():
        pass

    # Get download parameters for threaded downloader
    def set_download_parameters(
        self,
        params,
        relative_path,
        target_file,
        checksum_type=None,
        checksum_value=None,
        bytes_range=None,
    ):
        # Create directories if needed
        target_dir = os.path.dirname(target_file)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir, int("0755", 8))

        params["authtoken"] = self.authtoken
        params["relative_path"] = relative_path
        params["urls"] =  ["https://api.snapcraft.io/api/v1/snaps/download/"]
        params["authtoken"] = self.authtoken
        params["target_file"] = target_file
        params["ssl_ca_cert"] = self.repo.sslcacert
        params["ssl_client_cert"] = self.repo.sslclientcert
        params["ssl_client_key"] = self.repo.sslclientkey
        params["checksum_type"] = checksum_type
        params["checksum"] = checksum_value
        params["bytes_range"] = bytes_range
        params["http_headers"] = tuple(self.repo.http_headers.items())
        params["timeout"] = self.timeout
        params["minrate"] = self.minrate
        params["proxies"] = get_proxies(
            self.repo.proxy, self.repo.proxy_username, self.repo.proxy_password
        )
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            params["urlgrabber_logspec"] = CFG.get("urlgrabber_logspec")

    @staticmethod
    def get_file(path, local_base=None):
        # pylint: disable=W0613
        # Called from import_kickstarts, not working for deb repo
        log2(
            0,
            0,
            # pylint: disable-next=consider-using-f-string
            "Unable to download path %s from deb repo." % path,
            stream=sys.stderr,
        )
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # cat snap_src.py
#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2016--2017 Red Hat, Inc.
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

import sys
import os.path
from shutil import rmtree
from shutil import copyfile
import time
import re
import fnmatch
import requests
import logging
from functools import cmp_to_key
from uyuni.common import fileutils
from spacewalk.common.suseLib import get_proxy
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.syncLib import log2
from spacewalk.server import rhnSQL
from spacewalk.common import repo

import looseversion

try:
    #  python 2
    from urllib import unquote
    import urlparse
except ImportError:
    #  python3
    import urllib.parse as urlparse  # pylint: disable=F0401,E0611
    from urllib.parse import unquote

log = logging.getLogger(__name__)

class UniqueIDWrapper:
    def __init__(self, unique_id_str, relativepath):
        self.value = unique_id_str  # optional: retain original string
        self.relativepath = relativepath

    def __str__(self):
        return self.value  # behave like a string if needed
# pylint: disable-next=missing-class-docstring
class SnapPackage(ContentPackage):
    def __init__(self):
        self.name = None
        self.version = None
        self.revision = None
        self.channel = None
        self.arch = None
        self.download_url = None
        self.checksum_type = None
        self.checksum = None

    def __getitem__(self, key):
        return getattr(self, key)

    def __setitem__(self, key, value):
        return setattr(self, key, value)

    def evr(self):
        # The format is: [epoch:]upstream_version[-debian_revision].
        # https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
        evr = ""
        if self.epoch:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}:".format(self.epoch)
        if self.version:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}".format(self.version)
        if self.release:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "-{}".format(self.release)
        return evr

    def nevra(self):
        return f"{self.name}_{self.evr()}_{self.arch}"

    def is_populated(self):
        return all(
            [
                attribute is not None
                for attribute in (
                    self.name,
                    self.epoch,
                    self.version,
                    self.release,
                    self.arch,
                    self.relativepath,
                    self.checksum_type,
                    self.checksum,
                )
            ]
        )


# pylint: disable-next=missing-class-docstring
class SnapRepo:

    def __init__(
        self,
        url,
        cache_dir,
        pkg_dir,
        proxy_addr="",
        proxy_user="",
        proxy_pass="",
        gpg_verify=True,
        channel_label=None,
        timeout=None,
    ):
        self.url = url
        parsed_url = urlparse.urlparse(url)
        self.base_url = [f"{parsed_url.scheme}://{parsed_url.netloc}/"]
        self.urls = self.base_url

        self.sslclientcert = self.sslclientkey = self.sslcacert = None
        self.proxy = proxy_addr
        self.proxy_username = proxy_user
        self.proxy_password = proxy_pass
        self.gpg_verify = gpg_verify

        self.basecachedir = cache_dir
        if not os.path.isdir(self.basecachedir):
            # pylint: disable-next=invalid-name
            with cfg_component(component=None) as CFG:
                fileutils.makedirs(
                    self.basecachedir, user=CFG.httpd_user, group=CFG.httpd_group
                )
        self.includepkgs = []
        self.exclude = []
        self.pkgdir = pkg_dir
        self.http_headers = {
            "User-Agent": "curl/7.68.0",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }
    def verify(self):
        """
        Verify package index checksum and signature.

        :return:
        """
        dpkg_repo = repo.DpkgRepo(
            self.url, self._get_proxies(), self.gpg_verify, self.timeout
        )
        log.debug("DebRepo.verify() dpkg_repo=%s", dpkg_repo)
        if not dpkg_repo.verify_packages_index():
            raise repo.GeneralRepoException("Package index checksum failure")

    def _get_proxies(self):
        """
        Returns proxies dict for requests with python-requests.
        """
        if self.proxy:
            (_, netloc, _, _, _) = urlparse.urlsplit(self.proxy)
            proxies = {"http": "http://" + netloc, "https": "http://" + netloc}
            if self.proxy_username and self.proxy_password:
                proxies = {
                    "http": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                    "https": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                }
            return proxies
        else:
            return None
    def get_package_list(self):
        to_return = []
        seen = set()
        base_url = "https://api.snapcraft.io/api/v1/snaps/search?q={}&limit=100&page={}"

        headers = {
            "User-Agent": "snapd/2.63",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }

        for ch in range(ord("y"), ord("y") + 1):  # Change range if needed
            keyword = chr(ch)
            page = 1
            same_count = 0
            prev_count = 0

            while True:
                url = base_url.format(keyword, page)
                try:
                    res = requests.get(url, headers=headers)
                    if res.status_code != 200:
                        print(f"[ERROR] HTTP {res.status_code} for keyword '{keyword}' page {page}")
                        break

                    data = res.json()
                    packages = data.get("_embedded", {}).get("clickindex:package", [])
                    if not packages:
                        break

                    for pkg in packages:
                        name = pkg.get("package_name")
                        if not name or name in seen:
                            continue

                        seen.add(name)
                        arch_list = pkg.get("architecture", [])
                        version = pkg.get("version")
                        checksum = pkg.get("download_sha512", "")
                        download_url = pkg.get("download_url")

                        if not download_url:
                            continue  # Skip if no download URL

                        for arch in arch_list or ["noarch"]:
                            sp = SnapPackage()
                            #sp = ContentPackage()
                            sp.name = name
                            sp.version = version
                            sp.unique_id = sp
                            sp.epoch = ""
                            sp.arch = arch
                            sp.download_url = download_url
                            sp.checksum_type = "sha512"
                            sp.checksum = checksum
                            sp.unique_id = pkg.get("snap_id")
                            snap_id = pkg.get("snap_id")
                            revision = str(pkg.get("revision", "1"))
                            sp.release = revision
                            relativepath = f"{snap_id}_{revision}.snap"
                            
                            sp.relativepath = relativepath
                            sp.unique_id = UniqueIDWrapper(unique_id, relativepath)

                            to_return.append(sp)

                    curr_count = len(to_return)
                    if curr_count == prev_count:
                        same_count += 1
                    else:
                        same_count = 0
                        prev_count = curr_count

                    if same_count >= 2:
                        break

                    page += 1
                    time.sleep(0.2)

                except Exception as e:
                    print(f"[ERROR] Exception for keyword '{keyword}' page {page}: {e}")
                    break

        print(f"[DEBUG] Total snap packages collected: {len(to_return)}")
        return to_return[:5]

# pylint: disable-next=missing-class-docstring
#
class ContentSource:
    def __init__(
        self,
        url,
        name,
        insecure=False,
        interactive=True,
        yumsrc_conf=None,
        org="1",
        channel_label="",
        no_mirrors=False,
        ca_cert_file=None,
        client_cert_file=None,
        client_key_file=None,
        channel_arch="",
        http_headers=None,
    ):
        self.url = url
        self.name = name
        if org:
            self.org = org
        else:
            self.org = "NULL"

        # read the proxy configuration in /etc/rhn/rhn.conf
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            self.proxy_addr, self.proxy_user, self.proxy_pass = get_proxy(self.url)
            self.authtoken = None

            # Replace non-valid characters from reponame (only alphanumeric chars allowed)
            self.reponame = "".join([x if x.isalnum() else "_" for x in self.name])
            self.channel_label = channel_label

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

            try:
                # extended reposync nevra filter enable
                # this will filter packages based on full nevra
                # instead of package name only.
                self.nevra_filter = bool(CFG.REPOSYNC_NEVRA_FILTER)
            except (AttributeError, ValueError):
                self.nevra_filter = False

            # SUSE vendor repositories belongs to org = NULL
            # The repository cache root will be "/var/cache/rhn/reposync/REPOSITORY_LABEL/"
            root = os.path.join(CACHE_DIR, str(org or "NULL"), self.reponame)
            self.repo = SnapRepo(
                url,
                root,
                os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, self.org, "stage"),
                self.proxy_addr,
                self.proxy_user,
                self.proxy_pass,
                gpg_verify=not (insecure),
                channel_label=channel_label,
                timeout=self.timeout,
            )
            self.repo.http_headers = http_headers


            self.num_packages = 0
            self.num_excluded = 0

            # keep authtokens for mirroring
            # pylint: disable-next=invalid-name,unused-variable
            (_scheme, _netloc, _path, query, _fragid) = urlparse.urlsplit(url)
            if query:
                self.authtoken = query



    def list_packages(self, filters, latest):
        print("[DEBUG] list_packages called")
        pkgs = self.repo.get_package_list()
        self.num_packages = len(pkgs)
        return pkgs
    def get_md_checksum_type(self):
        pass

    def get_products(self):
        # No products
        return []

    def get_susedata(self):
        # No susedata
        return []

    def get_mediaproducts(self):
        # No mediaproducts data
        return None


    @staticmethod
    def _filter_packages(packages, filters, nevra_filter=False):
        """implement include / exclude logic
        filters are: [ ('+', includelist1), ('-', excludelist1),
                       ('+', includelist2), ... ]
        """
        if filters is None:
            return

        selected = []
        excluded = []
        allmatched_include = []
        allmatched_exclude = []
        if filters[0][0] == "-":
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        for filter_item in filters:
            sense, pkg_list = filter_item
            regex = fnmatch.translate(pkg_list[0])
            reobj = re.compile(regex)
            if sense == "+":
                # include
                for excluded_pkg in excluded:
                    if nevra_filter:
                        pkg_name = excluded_pkg.nevra()
                    else:
                        pkg_name = excluded_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_include.insert(0, excluded_pkg)
                        selected.insert(0, excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == "-":
                # exclude
                for selected_pkg in selected:
                    if nevra_filter:
                        pkg_name = selected_pkg.nevra()
                    else:
                        pkg_name = selected_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_exclude.insert(0, selected_pkg)
                        excluded.insert(0, selected_pkg)
                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = excluded + allmatched_exclude
            else:
                raise IOError("Filters are malformed")
        return selected

    def clear_cache(self, directory=None):
        if directory is None:
            directory = self.repo.basecachedir
        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

    @staticmethod
    def get_updates():
        # There isn't any update info in the repository
        return "", []

    @staticmethod
    def get_groups():
        pass

    # Get download parameters for threaded downloader
    def set_download_parameters(
        self,
        params,
        relative_path,
        target_file,
        checksum_type=None,
        checksum_value=None,
        bytes_range=None,
    ):
        # Create directories if needed
        target_dir = os.path.dirname(target_file)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir, int("0755", 8))

        params["authtoken"] = self.authtoken
        params["relative_path"] = relative_path
        params["urls"] =  ["https://api.snapcraft.io/api/v1/snaps/download/"]
        params["authtoken"] = self.authtoken
        params["target_file"] = target_file
        params["ssl_ca_cert"] = self.repo.sslcacert
        params["ssl_client_cert"] = self.repo.sslclientcert
        params["ssl_client_key"] = self.repo.sslclientkey
        params["checksum_type"] = checksum_type
        params["checksum"] = checksum_value
        params["bytes_range"] = bytes_range
        params["http_headers"] = tuple(self.repo.http_headers.items())
        params["timeout"] = self.timeout
        params["minrate"] = self.minrate
        params["proxies"] = get_proxies(
            self.repo.proxy, self.repo.proxy_username, self.repo.proxy_password
        )
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            params["urlgrabber_logspec"] = CFG.get("urlgrabber_logspec")

    @staticmethod
    def get_file(path, local_base=None):
        # pylint: disable=W0613
        # Called from import_kickstarts, not working for deb repo
        log2(
            0,
            0,
            # pylint: disable-next=consider-using-f-string
            "Unable to download path %s from deb repo." % path,
            stream=sys.stderr,
        )
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # vim snap_src.py
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # cd
uyuni-server:~ # spacewalk-repo-sync --channel=ubuntu_22.04_snap_stable --type=snap
ERROR: attempting to run more than one instance of spacewalk-repo-sync Exiting.
uyuni-server:~ # spacewalk-repo-sync --channel=ubuntu_22.04_snap_stable --type=snap
ERROR: attempting to run more than one instance of spacewalk-repo-sync Exiting.
uyuni-server:~ # ps aux | grep spacewalk-repo-sync
root     26538 19.4  1.8 716820 73616 ?        Sl   01:33   1:18 /usr/bin/python3 -u /usr/bin/spacewalk-repo-sync --channel ubuntu-20.04-pool-amd64-uyuni --type deb --non-interactive
root     27060  0.0  0.0   5432  1336 pts/3    S+   01:39   0:00 grep spacewalk-repo-sync
uyuni-server:~ # sudo kill -9 26538
uyuni-server:~ # spacewalk-repo-sync --channel=ubuntu_22.04_snap_stable --type=snap
ERROR: attempting to run more than one instance of spacewalk-repo-sync Exiting.
uyuni-server:~ # ps aux | grep spacewalk-repo-sync
root      3873 17.2  2.3 519028 94080 ?        Sl   01:40   0:03 /usr/bin/python3 -u /usr/bin/spacewalk-repo-sync --channel ubuntu_22_04_main --type deb --non-interactive
root      3893  0.0  0.0   5432  1336 pts/3    S+   01:40   0:00 grep spacewalk-repo-sync
uyuni-server:~ # tail -f /var/log/rhn/reposync.log
2025/06/28 01:33:01 +02:00 | Channel: ubuntu-20.04-pool-amd64-uyuni
2025/06/28 01:33:01 +02:00 ======================================
2025/06/28 01:33:01 +02:00 Sync of channel started.
                           Please check 'reposync/ubuntu-20.04-pool-amd64-uyuni.log' for sync log of this channel.
2025/06/28 01:40:13 +02:00 Command: ['/usr/bin/spacewalk-repo-sync', '--channel', 'ubuntu_22_04_main', '--type', 'deb', '--non-interactive']
2025/06/28 01:40:13 +02:00 ======================================
2025/06/28 01:40:13 +02:00 | Channel: ubuntu_22_04_main
2025/06/28 01:40:13 +02:00 ======================================
2025/06/28 01:40:13 +02:00 Sync of channel started.
                           Please check 'reposync/ubuntu_22_04_main.log' for sync log of this channel.
2025/06/28 01:41:44 +02:00 Sync of channel completed.
2025/06/28 01:41:44 +02:00 Total time: 0:01:31
2025/06/28 01:41:44 +02:00 Command: ['/usr/bin/spacewalk-repo-sync', '--channel', 'ubuntu_22.04_snap_stable', '--type', 'snap', '--non-interactive']
2025/06/28 01:41:44 +02:00 ======================================
2025/06/28 01:41:44 +02:00 | Channel: ubuntu_22.04_snap_stable
2025/06/28 01:41:44 +02:00 ======================================
2025/06/28 01:41:44 +02:00 Sync of channel started.
                           Please check 'reposync/ubuntu_22.04_snap_stable.log' for sync log of this channel.
^C
uyuni-server:~ # sudo kill -9 3873
kill: sending signal to 3873 failed: No such process
uyuni-server:~ # ps aux | grep spacewalk-repo-sync
root     27167  9.3  1.8 644288 72432 ?        Sl   01:41   0:05 /usr/bin/python3 -u /usr/bin/spacewalk-repo-sync --channel ubuntu_22.04_snap_stable --type snap --non-interactive
root     27264  0.0  0.0   5432  1320 pts/3    S+   01:42   0:00 grep spacewalk-repo-sync
uyuni-server:~ # spacewalk-repo-sync --channel=ubuntu_22.04_snap_stable --type=snap
01:44:40 ======================================
01:44:40 | Channel: ubuntu_22.04_snap_stable
01:44:40 ======================================
01:44:40 Sync of channel started.
[DEBUG] list_packages called
[DEBUG] Total snap packages collected: 1959
01:45:12 Repo URL: https://api.snapcraft.io
01:45:12     Packages in repo:                 5
01:45:12     Packages already synced:          0
01:45:12     Packages to sync:                 5
01:45:12     New packages to download:         5
01:45:12   Downloading packages:
01:45:12 Downloading total 5 files from 1 queues.
01:45:36     1/5 : Nv4binu8zywXYgg0fpmhRkBtjaAbHZvL_8.snap
01:45:38     2/5 : nnIL7gLSqKrZdp3uLnt8KubIwfNTQGv2_2.snap
01:45:38     3/5 : z6aFwrsrUDv43zlW3AdiKtVpevBMEdsx_79.snap
01:45:45     4/5 : 4Rb31sn6ve0Q18dC3KT8SuNuzQp2Gapl_12.snap
01:45:47     5/5 : Tut7kMl7WlcoEStIgSSsVB1E6CIZnibQ_4806.snap
01:45:47 Filtering packages that failed to download
01:45:47
01:45:47   Importing packages to DB:
01:45:48   Package batch #1 of 1 completed...
01:45:48
01:45:48   Patches in repo: 0.
01:45:48 Total time: 0:01:07
uyuni-server:~ # exit
exit
server:~ # git clone https://github.com/uyuni-project/uyuni.git
-bash: git: command not found
server:~ # podman exec -it uyuni-server bash
===============================================================================
!
! This shell operates within a container environment, meaning that not all
! modifications will be permanently saved in volumes.
!
! Please exercise caution when making changes, as some alterations may not
! persist beyond the current session.
!
===============================================================================
uyuni-server:/ # cd /usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # vim snap_src.py
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # vim snap_src.py
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # cd
uyuni-server:~ # spacewalk-repo-sync --channel=ubuntu_22.04_snap_stable --type=snap
05:55:28 ======================================
05:55:28 | Channel: ubuntu_22.04_snap_stable
05:55:28 ======================================
05:55:28 Sync of channel started.
[DEBUG] list_packages called
[DEBUG] Total snap packages collected: 4775
05:57:07 Repo URL: https://api.snapcraft.io
05:57:07     Packages in repo:                 5
05:57:07     Packages already synced:          0
05:57:07     Packages to sync:                 5
05:57:07     New packages to download:         5
05:57:07   Downloading packages:
05:57:07 Downloading total 5 files from 1 queues.
05:57:16     1/5 : mq0sTMV7a8744UTRBwQHimStAqsGzbjj_465.snap
05:57:22     2/5 : BKaGy9RLIWYV93fTL4Oeu8xcqsIVQxsT_1.snap
05:57:23     3/5 : ZZjSue7jjdQXHsNz5RI9BH3Pf3UnRo0V_58.snap
05:57:25     4/5 : V5RMY2e1OzXcJZvTp2P42Aafb1PiRPPP_2.snap
05:57:26     5/5 : QulQD1qbrCvkV9QD4YLF1ZZczAXSi8Fy_171.snap
05:57:27 Filtering packages that failed to download
05:57:27
05:57:27   Importing packages to DB:
05:57:27   Package batch #1 of 1 completed...
05:57:27
05:57:27   Patches in repo: 0.
05:57:27 Total time: 0:01:58
uyuni-server:~ # cd /usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # vim snap_src.py
uyuni-server:/usr/lib/python3.6/site-packages/spacewalk/satellite_tools/repo_plugins # cat snap_src.py
#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2016--2017 Red Hat, Inc.
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

import sys
import os.path
from shutil import rmtree
from shutil import copyfile
import time
import re
import fnmatch
import requests
import logging
from functools import cmp_to_key
from uyuni.common import fileutils
from spacewalk.common.suseLib import get_proxy
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.syncLib import log2
from spacewalk.server import rhnSQL
from spacewalk.common import repo

import looseversion

try:
    #  python 2
    from urllib import unquote
    import urlparse
except ImportError:
    #  python3
    import urllib.parse as urlparse  # pylint: disable=F0401,E0611
    from urllib.parse import unquote


class UniqueIDWrapper:
    def __init__(self, unique_id_str, relativepath):
        self.value = unique_id_str  # optional: retain original string
        self.relativepath = relativepath

    def __str__(self):
        return self.value  # behave like a string if needed
# pylint: disable-next=missing-class-docstring
class SnapPackage(ContentPackage):
    def __init__(self):
        self.name = None
        self.version = None
        self.revision = None
        self.channel = None
        self.arch = None
        self.download_url = None
        self.checksum_type = None
        self.checksum = None

    def __getitem__(self, key):
        return getattr(self, key)

    def __setitem__(self, key, value):
        return setattr(self, key, value)

    def evr(self):
        # The format is: [epoch:]upstream_version[-debian_revision].
        # https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
        evr = ""
        if self.epoch:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}:".format(self.epoch)
        if self.version:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "{}".format(self.version)
        if self.release:
            # pylint: disable-next=consider-using-f-string
            evr = evr + "-{}".format(self.release)
        return evr

    def nevra(self):
        return f"{self.name}_{self.evr()}_{self.arch}"

    def is_populated(self):
        return all(
            [
                attribute is not None
                for attribute in (
                    self.name,
                    self.epoch,
                    self.version,
                    self.release,
                    self.arch,
                    self.relativepath,
                    self.checksum_type,
                    self.checksum,
                )
            ]
        )


# pylint: disable-next=missing-class-docstring
class SnapRepo:

    def __init__(
        self,
        url,
        cache_dir,
        pkg_dir,
        proxy_addr="",
        proxy_user="",
        proxy_pass="",
        gpg_verify=True,
        channel_label=None,
        timeout=None,
    ):
        self.url = url
        parsed_url = urlparse.urlparse(url)
        self.base_url = [f"{parsed_url.scheme}://{parsed_url.netloc}/"]
        self.urls = self.base_url

        self.sslclientcert = self.sslclientkey = self.sslcacert = None
        self.proxy = proxy_addr
        self.proxy_username = proxy_user
        self.proxy_password = proxy_pass
        self.gpg_verify = gpg_verify

        self.basecachedir = cache_dir
        if not os.path.isdir(self.basecachedir):
            # pylint: disable-next=invalid-name
            with cfg_component(component=None) as CFG:
                fileutils.makedirs(
                    self.basecachedir, user=CFG.httpd_user, group=CFG.httpd_group
                )
        self.includepkgs = []
        self.exclude = []
        self.pkgdir = pkg_dir
        self.http_headers = {
            "User-Agent": "curl/7.68.0",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }
    def verify(self):
        """
        Verify package index checksum and signature.

        :return:
        """
        pass

    def _get_proxies(self):
        """
        Returns proxies dict for requests with python-requests.
        """
        if self.proxy:
            (_, netloc, _, _, _) = urlparse.urlsplit(self.proxy)
            proxies = {"http": "http://" + netloc, "https": "http://" + netloc}
            if self.proxy_username and self.proxy_password:
                proxies = {
                    "http": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                    "https": "http://"
                    + self.proxy_username
                    + ":"
                    + self.proxy_password
                    + "@"
                    + netloc,
                }
            return proxies
        else:
            return None
    def get_package_list(self):
        to_return = []
        seen = set()
        base_url = "https://api.snapcraft.io/api/v1/snaps/search?q={}&limit=100&page={}"

        headers = {
            "User-Agent": "snapd/2.63",
            "Snap-Device-Series": "16",
            "Accept": "application/json",
        }

        for ch in range(ord("u"), ord("u") + 1):  # Change range if needed
            keyword = chr(ch)
            page = 1
            same_count = 0
            prev_count = 0

            while True:
                url = base_url.format(keyword, page)
                try:
                    res = requests.get(url, headers=headers)
                    if res.status_code != 200:
                        print(f"[ERROR] HTTP {res.status_code} for keyword '{keyword}' page {page}")
                        break

                    data = res.json()
                    packages = data.get("_embedded", {}).get("clickindex:package", [])
                    if not packages:
                        break

                    for pkg in packages:
                        name = pkg.get("package_name")
                        if not name or name in seen:
                            continue

                        seen.add(name)
                        arch_list = pkg.get("architecture", [])
                        version = pkg.get("version")
                        checksum = pkg.get("download_sha512", "")
                        download_url = pkg.get("download_url")

                        if not download_url:
                            continue  # Skip if no download URL

                        for arch in arch_list or ["noarch"]:
                            sp = SnapPackage()
                            #sp = ContentPackage()
                            sp.name = name
                            sp.version = version
                            sp.unique_id = sp
                            sp.epoch = ""
                            sp.arch = arch
                            sp.download_url = download_url
                            sp.checksum_type = "sha512"
                            sp.checksum = checksum
                            sp.unique_id = pkg.get("snap_id")
                            snap_id = pkg.get("snap_id")
                            revision = str(pkg.get("revision", "1"))
                            sp.release = revision
                            relativepath = f"{snap_id}_{revision}.snap"
                            unique_id = f"{snap_id}_{revision}_{arch}"

                            sp.relativepath = relativepath
                            sp.unique_id = UniqueIDWrapper(unique_id, relativepath)

                            to_return.append(sp)

                    curr_count = len(to_return)
                    if curr_count == prev_count:
                        same_count += 1
                    else:
                        same_count = 0
                        prev_count = curr_count

                    if same_count >= 2:
                        break

                    page += 1
                    time.sleep(0.2)

                except Exception as e:
                    print(f"[ERROR] Exception for keyword '{keyword}' page {page}: {e}")
                    break

        print(f"[DEBUG] Total snap packages collected: {len(to_return)}")
        return to_return[:5]

# pylint: disable-next=missing-class-docstring
#
class ContentSource:
    def __init__(
        self,
        url,
        name,
        insecure=False,
        interactive=True,
        yumsrc_conf=None,
        org="1",
        channel_label="",
        no_mirrors=False,
        ca_cert_file=None,
        client_cert_file=None,
        client_key_file=None,
        channel_arch="",
        http_headers=None,
    ):
        self.url = url
        self.name = name
        if org:
            self.org = org
        else:
            self.org = "NULL"

        # read the proxy configuration in /etc/rhn/rhn.conf
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            self.proxy_addr, self.proxy_user, self.proxy_pass = get_proxy(self.url)
            self.authtoken = None

            # Replace non-valid characters from reponame (only alphanumeric chars allowed)
            self.reponame = "".join([x if x.isalnum() else "_" for x in self.name])
            self.channel_label = channel_label

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

            try:
                # extended reposync nevra filter enable
                # this will filter packages based on full nevra
                # instead of package name only.
                self.nevra_filter = bool(CFG.REPOSYNC_NEVRA_FILTER)
            except (AttributeError, ValueError):
                self.nevra_filter = False

            # SUSE vendor repositories belongs to org = NULL
            # The repository cache root will be "/var/cache/rhn/reposync/REPOSITORY_LABEL/"
            root = os.path.join(CACHE_DIR, str(org or "NULL"), self.reponame)
            self.repo = SnapRepo(
                url,
                root,
                os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, self.org, "stage"),
                self.proxy_addr,
                self.proxy_user,
                self.proxy_pass,
                gpg_verify=not (insecure),
                channel_label=channel_label,
                timeout=self.timeout,
            )
            self.repo.http_headers = http_headers


            self.num_packages = 0
            self.num_excluded = 0

            # keep authtokens for mirroring
            # pylint: disable-next=invalid-name,unused-variable
            (_scheme, _netloc, _path, query, _fragid) = urlparse.urlsplit(url)
            if query:
                self.authtoken = query



    def list_packages(self, filters, latest):
        print("[DEBUG] list_packages called")
        pkgs = self.repo.get_package_list()
        self.num_packages = len(pkgs)
        return pkgs
    def get_md_checksum_type(self):
        pass

    def get_products(self):
        # No products
        return []

    def get_susedata(self):
        # No susedata
        return []

    def get_mediaproducts(self):
        # No mediaproducts data
        return None


    @staticmethod
    def _filter_packages(packages, filters, nevra_filter=False):
        """implement include / exclude logic
        filters are: [ ('+', includelist1), ('-', excludelist1),
                       ('+', includelist2), ... ]
        """
        if filters is None:
            return

        selected = []
        excluded = []
        allmatched_include = []
        allmatched_exclude = []
        if filters[0][0] == "-":
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        for filter_item in filters:
            sense, pkg_list = filter_item
            regex = fnmatch.translate(pkg_list[0])
            reobj = re.compile(regex)
            if sense == "+":
                # include
                for excluded_pkg in excluded:
                    if nevra_filter:
                        pkg_name = excluded_pkg.nevra()
                    else:
                        pkg_name = excluded_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_include.insert(0, excluded_pkg)
                        selected.insert(0, excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == "-":
                # exclude
                for selected_pkg in selected:
                    if nevra_filter:
                        pkg_name = selected_pkg.nevra()
                    else:
                        pkg_name = selected_pkg["name"]
                    if reobj.match(pkg_name):
                        allmatched_exclude.insert(0, selected_pkg)
                        excluded.insert(0, selected_pkg)
                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = excluded + allmatched_exclude
            else:
                raise IOError("Filters are malformed")
        return selected

    def clear_cache(self, directory=None):
        if directory is None:
            directory = self.repo.basecachedir
        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

    @staticmethod
    def get_updates():
        # There isn't any update info in the repository
        return "", []

    @staticmethod
    def get_groups():
        pass

    # Get download parameters for threaded downloader
    def set_download_parameters(
        self,
        params,
        relative_path,
        target_file,
        checksum_type=None,
        checksum_value=None,
        bytes_range=None,
    ):
        # Create directories if needed
        target_dir = os.path.dirname(target_file)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir, int("0755", 8))

        params["authtoken"] = self.authtoken
        params["relative_path"] = relative_path
        params["urls"] =  ["https://api.snapcraft.io/api/v1/snaps/download/"]
        params["authtoken"] = self.authtoken
        params["target_file"] = target_file
        params["ssl_ca_cert"] = self.repo.sslcacert
        params["ssl_client_cert"] = self.repo.sslclientcert
        params["ssl_client_key"] = self.repo.sslclientkey
        params["checksum_type"] = checksum_type
        params["checksum"] = checksum_value
        params["bytes_range"] = bytes_range
        params["http_headers"] = tuple(self.repo.http_headers.items())
        params["timeout"] = self.timeout
        params["minrate"] = self.minrate
        params["proxies"] = get_proxies(
            self.repo.proxy, self.repo.proxy_username, self.repo.proxy_password
        )
        # pylint: disable-next=invalid-name
        with cfg_component("server.satellite") as CFG:
            params["urlgrabber_logspec"] = CFG.get("urlgrabber_logspec")

    @staticmethod
    def get_file(path, local_base=None):
        # pylint: disable=W0613
        # Called from import_kickstarts, not working for deb repo
        log2(
            0,
            0,
            # pylint: disable-next=consider-using-f-string
            "Unable to download path %s from deb repo." % path,
            stream=sys.stderr,
        )