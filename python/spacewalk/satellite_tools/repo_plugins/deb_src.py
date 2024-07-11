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
from salt.utils.versions import LooseVersion
from uyuni.common import fileutils
from uyuni.common.context_managers import cfg_component
from spacewalk.common.suseLib import get_proxy
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.syncLib import log2
from spacewalk.server import rhnSQL
from spacewalk.common import repo

try:
    #  python 2 
    from urllib import unquote
    import urlparse
except ImportError:
    #  python3
    import urllib.parse as urlparse # pylint: disable=F0401,E0611
    from urllib.parse import unquote

RETRIES = 10
RETRY_DELAY = 1
FORMAT_PRIORITY = ['.xz', '.gz', '']
log = logging.getLogger(__name__)

class DebPackage:
    def __init__(self):
        self.name = None
        self.epoch = None
        self.version = None
        self.release = None
        self.arch = None
        self.relativepath = None
        self.checksum_type = None
        self.checksum = None

    def __getitem__(self, key):
        return getattr(self, key)

    def __setitem__(self, key, value):
        return setattr(self, key, value)

    def evr(self):
        evr = ""
        if self.epoch:
           evr = evr + "{}:".format(self.epoch)
        if self.version:
           evr = evr + "{}".format(self.version)
        if self.release:
           evr = evr + "-{}".format(self.release)
        return evr

    def is_populated(self):
        return all([attribute is not None for attribute in (self.name, self.epoch,
                                                            self.version, self.release, self.arch,
                                                            self.relativepath, self.checksum_type,
                                                            self.checksum)])


class DebRepo:
    # url example - http://ftp.debian.org/debian/dists/jessie/main/binary-amd64/
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
        parts = url.rsplit('/dists/', 1)
        self.base_url = [parts[0]]
        self.timeout = timeout

        parsed_url = urlparse.urlparse(url)
        query = urlparse.parse_qsl(parsed_url.query)
        new_query = []
        suite = None
        component = None
        arch = None
        for qi in query:
            if qi[0] == "uyuni_suite":
                suite = qi[1]
            elif qi[0] == "uyuni_component":
                component = qi[1]
            elif qi[0] == "uyuni_arch":
                arch = qi[1]
            else:
                new_query.append(qi)
        if suite:
            parsed_url = parsed_url._replace(query=urlparse.urlencode(new_query))
            base_url = urlparse.urlunparse(parsed_url)
            path_list = parsed_url.path.split("/")
            log2(0, 0, "Base URL: {}".format(base_url))
            log2(0, 0, "Suite: {}".format(suite))
            log2(0, 0, "Component: {}".format(component))
            if "/" not in suite:
                path_list.append("dists")
            path_list.extend(suite.split("/"))
            if component:
                path_list.extend(component.split("/"))
            if "/" not in suite:
                if arch is None:
                    rhnSQL.initDB()
                    h = rhnSQL.prepare("""
                                       SELECT ca.label AS arch_label
                                       FROM rhnChannel AS c
                                       LEFT JOIN rhnChannelArch AS ca
                                           ON c.channel_arch_id = ca.id
                                       WHERE c.label = :channel_label
                                       """)
                    h.execute(channel_label=channel_label)
                    row = h.fetchone_dict()
                    if row and "arch_label" in row:
                        aspl = row["arch_label"].split("-")
                        if len(aspl) == 3 and aspl[0] == "channel" and aspl[2] == "deb":
                            arch_trans = {
                                "ia32": "i386",
                                "arm": "armhf",
                            }
                            if aspl[1] in arch_trans:
                                arch = arch_trans[aspl[1]]
                            else:
                                arch = aspl[1]
                if arch:
                    log2(0, 0, "Channel architecture: {}".format(arch))
                    path_list.append("binary-{}".format(arch))
            while "" in path_list:
                path_list.remove("")
            parsed_url = parsed_url._replace(path="/".join(path_list))
            self.url = url = urlparse.urlunparse(parsed_url)
            self.base_url = [base_url]

        # Make sure baseurl ends with / and urljoin will work correctly
        if self.base_url[0][-1] != '/':
            self.base_url[0] += '/'
        self.urls = self.base_url
        self.sslclientcert = self.sslclientkey = self.sslcacert = None
        self.proxy = proxy_addr
        self.proxy_username = proxy_user
        self.proxy_password = proxy_pass
        self.gpg_verify = gpg_verify

        self.basecachedir = cache_dir
        if not os.path.isdir(self.basecachedir):
            fileutils.makedirs(self.basecachedir, user='wwwrun', group='www')
        self.includepkgs = []
        self.exclude = []
        self.pkgdir = pkg_dir
        self.http_headers = {}

    def verify(self):
        """
        Verify package index checksum and signature.

        :return:
        """
        if not repo.DpkgRepo(
            self.url, self._get_proxies(), self.gpg_verify, self.timeout
        ).verify_packages_index():
            raise repo.GeneralRepoException("Package index checksum failure")

    def _get_proxies(self):
        """
        Returns proxies dict for requests with python-requests.
        """
        if self.proxy:
            (_, netloc, _, _, _) = urlparse.urlsplit(self.proxy)
            proxies = {
                'http': 'http://' + netloc,
                'https': 'http://' + netloc
            }
            if self.proxy_username and self.proxy_password:
                proxies = {
                    'http': 'http://' + self.proxy_username + ":" + self.proxy_password + "@" + netloc,
                    'https': 'http://' + self.proxy_username + ":" + self.proxy_password + "@" + netloc,
                }
            return proxies
        else:
            return None


    def _download(self, url):
        if url.startswith('file://'):
            srcpath = unquote(url[len('file://'):])
            if not os.path.exists(srcpath):
                return ''
            filename = self.basecachedir + '/' + os.path.basename(url)
            copyfile(srcpath, filename)
            return filename
        for _ in range(0, RETRIES):
            try:
                data = requests.get(
                    url,
                    proxies=self._get_proxies(),
                    cert=(self.sslclientcert, self.sslclientkey),
                    verify=self.sslcacert,
                    timeout=self.timeout,
                )
                if not data.ok:
                    return ''
                filename = os.path.join(self.basecachedir, os.path.basename(urlparse.urlparse(url).path))
                fd = open(filename, 'wb')
                try:
                    for chunk in data.iter_content(chunk_size=1024):
                        fd.write(chunk)
                finally:
                    if fd is not None:
                        fd.close()
                return filename
            except requests.exceptions.RequestException as exc:
                print("ERROR: requests.exceptions.RequestException occurred:", exc)
                time.sleep(RETRY_DELAY)

        return ''

    def get_package_list(self):
        decompressed = None
        packages_raw = []
        to_return = []

        for extension in FORMAT_PRIORITY:
            scheme, netloc, path, query, fragid = urlparse.urlsplit(self.url)
            url = urlparse.urlunsplit((scheme, netloc,
                                       path + ('/' if not path.endswith('/') else '') + 'Packages' + extension, query, fragid))
            filename = self._download(url)
            if filename:
                if query:
                    newfilename = filename.split('?')[0]
                    os.rename(filename, newfilename)
                    filename = newfilename
                decompressed = fileutils.decompress_open(filename)
                break

        if decompressed:
            for pkg in decompressed.read().split("\n\n"):
                packages_raw.append(pkg)
            decompressed.close()
        else:
            print("ERROR: Download of package list failed.")

        # Parse and format package metadata
        for chunk in packages_raw:
            package = DebPackage()
            package.epoch = ""
            lines = chunk.split("\n")
            checksums = {}
            for line in lines:
                pair = tuple(p.strip() for p in line.split(" ", 1))
                if pair[0] == "Package:":
                    package.name = pair[1]
                elif pair[0] == "Architecture:":
                    package.arch = pair[1] + '-deb'
                elif pair[0] == "Version:":
                    package['epoch'] = ''
                    version = pair[1]
                    if version.find(':') != -1:
                        package['epoch'], version = version.split(':')
                    if version.find('-') != -1:
                        tmp = version.split('-')
                        package['version'] = '-'.join(tmp[:-1])
                        package['release'] = tmp[-1]
                    else:
                        package['version'] = version
                        package['release'] = 'X'
                elif pair[0] == "Filename:":
                    package.relativepath = pair[1]
                elif pair[0] == "SHA256:":
                    checksums['sha256'] = pair[1]
                elif pair[0] == "SHA1:":
                    checksums['sha1'] = pair[1]
                elif pair[0] == "MD5sum:":
                    checksums['md5'] = pair[1]

            # Pick best available checksum
            if 'sha256' in checksums:
                package.checksum_type = 'sha256'
                package.checksum = checksums['sha256']
            elif 'sha1' in checksums:
                package.checksum_type = 'sha1'
                package.checksum = checksums['sha1']
            elif 'md5' in checksums:
                package.checksum_type = 'md5'
                package.checksum = checksums['md5']

            if package.is_populated():
                to_return.append(package)
        return to_return


class ContentSource:

    def __init__(self, url, name, insecure=False, interactive=True, yumsrc_conf=None,
                 org="1", channel_label="", no_mirrors=False, ca_cert_file=None,
                 client_cert_file=None, client_key_file=None, channel_arch="",
                 http_headers=None):
        # pylint: disable=W0613
        self.url = url
        self.name = name
        if org:
            self.org = org
        else:
            self.org = "NULL"

        # read the proxy configuration in /etc/rhn/rhn.conf
        with cfg_component('server.satellite') as CFG:
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

            # SUSE vendor repositories belongs to org = NULL
            # The repository cache root will be "/var/cache/rhn/reposync/REPOSITORY_LABEL/"
            root = os.path.join(CACHE_DIR, str(org or "NULL"), self.reponame)
            self.repo = DebRepo(
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
            self.repo.verify()

            self.num_packages = 0
            self.num_excluded = 0

            # keep authtokens for mirroring
            (_scheme, _netloc, _path, query, _fragid) = urlparse.urlsplit(url)
            if query:
                self.authtoken = query

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

    def list_packages(self, filters, latest):
        """ list packages"""

        pkglist = self.repo.get_package_list()
        self.num_packages = len(pkglist)
        if latest:
            latest_pkgs = {}
            for pkg in pkglist:
                ident = '{}.{}'.format(pkg.name, pkg.arch)
                if ident not in latest_pkgs.keys() or LooseVersion(pkg.evr()) > LooseVersion(latest_pkgs[ident].evr()):
                    latest_pkgs[ident] = pkg
            pkglist = list(latest_pkgs.values())
        pkglist.sort(key = cmp_to_key(self._sort_packages))

        if not filters:
            # if there's no include/exclude filter on command line or in database
            for p in self.repo.includepkgs:
                filters.append(('+', [p]))
            for p in self.repo.exclude:
                filters.append(('-', [p]))

        if filters:
            pkglist = self._filter_packages(pkglist, filters)
            self.num_excluded = self.num_packages - len(pkglist)

        to_return = []
        for pack in pkglist:
            new_pack = ContentPackage()
            try:
                new_pack.setNVREA(pack.name, pack.version, pack.release,
                                pack.epoch, pack.arch)
            except ValueError as e:
                log(0, "WARNING: package contains incorrect metadata. SKIPPING!")
                log(0, e)
                continue
            new_pack.unique_id = pack
            new_pack.checksum_type = pack.checksum_type
            new_pack.checksum = pack.checksum
            to_return.append(new_pack)
        return to_return

    @staticmethod
    def _sort_packages(pkg1, pkg2):
        """sorts a list of deb package dicts by name"""
        if pkg1.name > pkg2.name:
            return 1
        elif pkg1.name == pkg2.name:
            return 0
        else:
            return -1

    @staticmethod
    def _filter_packages(packages, filters):
        """ implement include / exclude logic
            filters are: [ ('+', includelist1), ('-', excludelist1),
                           ('+', includelist2), ... ]
        """
        if filters is None:
            return

        selected = []
        excluded = []
        allmatched_include = []
        allmatched_exclude = []
        if filters[0][0] == '-':
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        for filter_item in filters:
            sense, pkg_list = filter_item
            regex = fnmatch.translate(pkg_list[0])
            reobj = re.compile(regex)
            if sense == '+':
                # include
                for excluded_pkg in excluded:
                    if reobj.match(excluded_pkg['name']):
                        allmatched_include.insert(0, excluded_pkg)
                        selected.insert(0, excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == '-':
                # exclude
                for selected_pkg in selected:
                    if reobj.match(selected_pkg['name']):
                        allmatched_exclude.insert(0, selected_pkg)
                        excluded.insert(0, selected_pkg)

                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = (excluded + allmatched_exclude)
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
        return '', []

    @staticmethod
    def get_groups():
        pass

    # Get download parameters for threaded downloader
    def set_download_parameters(self, params, relative_path, target_file, checksum_type=None,
                                checksum_value=None, bytes_range=None):
        # Create directories if needed
        target_dir = os.path.dirname(target_file)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir, int('0755', 8))

        params['authtoken'] = self.authtoken
        params['urls'] = self.repo.urls
        params['relative_path'] = relative_path
        params['authtoken'] = self.authtoken
        params['target_file'] = target_file
        params['ssl_ca_cert'] = self.repo.sslcacert
        params['ssl_client_cert'] = self.repo.sslclientcert
        params['ssl_client_key'] = self.repo.sslclientkey
        params['checksum_type'] = checksum_type
        params['checksum'] = checksum_value
        params['bytes_range'] = bytes_range
        params['http_headers'] = tuple(self.repo.http_headers.items())
        params["timeout"] = self.timeout
        params["minrate"] = self.minrate
        params['proxies'] = get_proxies(self.repo.proxy, self.repo.proxy_username,
                                        self.repo.proxy_password)

    @staticmethod
    def get_file(path, local_base=None):
        # pylint: disable=W0613
        # Called from import_kickstarts, not working for deb repo
        log2(0, 0, "Unable to download path %s from deb repo." % path, stream=sys.stderr)
