# coding: utf-8
"""
Reposync via Salt library.
"""
from __future__ import absolute_import, unicode_literals

import os
import sys
import types
import salt.client
import salt.config


class ZyppoSync:
    """
    Wrapper for underlying package manager for the reposync via Salt.

    Example usage:

    >>> zyppo = ZyppoSync()
    >>> for idx, repo_meta in enumerate(zyppo.list_repos().values()):
    >>>    print(idx + 1, repo_meta["name"])
    >>>    print("  ", repo_meta["baseurl"])

    """
    def __init__(self, cfg_path:str = "/etc/salt/minion", root:str = None):
        self._conf = salt.config.minion_config(cfg_path)
        self._conf["file_client"] = "local"
        self._conf["server_id_use_crc"] = "Adler32"
        if root is not None:
            self._init_root(root)
        self._caller = salt.client.Caller(mopts=self._conf)

    def _init_root(self, root:str) -> None:
        """
        Creates a root environment for Zypper, but only if none is around.

        :return: None
        """
        if not os.path.exists(root):
            try:
                for pth in [root, os.path.join(root, "zypp/repos.d")]:
                    os.makedirs(pth)
            except PermissionError as exc:
                # TODO: a proper logging somehow?
                sys.stderr("Unable to initialise Zypper root for {}: {}".format(root, exc))
                raise
            self._conf["zypper_root"] = root

    def _get_call(self, key:str) -> types.FunctionType:
        """
        Prepare a call to the pkg module.
        """
        def make_call(*args, **kwargs):
            """
            Makes a call to the underlying package.
            """
            kwargs["root"] = self._conf.get("zypper_root")
            return self._caller.cmd("pkg.{}".format(key), *args, **kwargs)
        return make_call

    def __getattr__(self, attr:str) -> types.FunctionType:
        """
        Prepare a callable on the requested
        attribute name.
        """
        return self._get_call(attr)
    

class ContentSource(object):
    def __init__(self, url, name, insecure=False, interactive=True,
                 yumsrc_conf=None, org="1", channel_label="", 
                 no_mirrors=False, ca_cert_file=None, client_cert_file=None,
                 client_key_file=None):
        """
        Plugin constructor.
        """

        # pylint: disable=W0613
        self.url = url
        self.name = name
        self.org = org if org else "NULL"

        # read the proxy configuration in /etc/rhn/rhn.conf
        initCFG('server.satellite')
        self.proxy_addr = CFG.http_proxy
        self.proxy_user = CFG.http_proxy_username
        self.proxy_pass = CFG.http_proxy_password

        # TODO: Further configure
        self.repo = ZyppoSync()

        self.num_packages = 0
        self.num_excluded = 0

        # keep authtokens for mirroring
        _scheme, _netloc, _path, query, _fragid = urlparse.urlsplit(url)
        self.authtoken = query if query else None

    def get_md_checksum_type(self) -> (str, int):
        """
        Get checksum type.

        :returns: tuple of: checksum-type, checksum
        """
        return "sha1", 0

    def get_products(self) -> dict:
        """
        Return products of SLE.

        Example data:

            {'arch': 'x86_64',
             'description': 'openSUSE Leap 15.0',
             'eol': '2019-11-30T01:00:00+01',
             'eol_t': 1575072000,
             'epoch': '0',
             'flavor': 'ftp',
             'installed': True,
             'isbase': True,
             'name': 'openSUSE',
             'productline': 'Leap',
             'release': '1',
             'repo': '@System',
             'shortname': 'openSUSE Leap',
             'summary': 'openSUSE Leap 15.0',
             'vendor': 'openSUSE',
             'version': '15.0'}

        :returns: list of mappings
        """

        data = []
        for product in self.repo.list_products():
            for sp_key in ["eol", "eol_t", "flavor", "installed", "isbase",
                           "productline", "repo", "shortname"]:
                if sp_key in product:
                    del product[sp_key]
            data.append(product)

        return data

    def get_susedata(self):
        """
        Return ??

        :returns: list
        """
        return []

    def get_updates(self):
        """
        Return available updates.

        :returns: list
        """
        self.repo.list_updates()
        return ('', [])

    def list_packages(self, filters, latest):
        """
        List available packages.

        :returns: list
        """

        pkglist = self.repo.list_pkgs()
        self.num_packages = len(pkglist)

        pkglist.sort(self._sort_packages)

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
            new_pack.setNVREA(pack.name, pack.version, pack.release,
                              pack.epoch, pack.arch)
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
                    if (reobj.match(excluded_pkg['name'])):
                        allmatched_include.insert(0,excluded_pkg)
                        selected.insert(0,excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == '-':
                # exclude
                for selected_pkg in selected:
                    if (reobj.match(selected_pkg['name'])):
                        allmatched_exclude.insert(0,selected_pkg)
                        excluded.insert(0,selected_pkg)

                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = (excluded + allmatched_exclude)
            else:
                raise IOError("Filters are malformed")
        return selected

    def clear_cache(self, directory=None):
        if directory is None:
            directory = os.path.join(CACHE_DIR, self.org, self.name)
        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

    @staticmethod
    def get_groups():
        # There aren't any
        return None

    # Get download parameters for threaded downloader
    def set_download_parameters(self, params, relative_path, target_file, checksum_type=None, checksum_value=None,
                                bytes_range=None):
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
        params['proxy'] = self.proxy_addr
        params['proxy_username'] = self.proxy_user
        params['proxy_password'] = self.proxy_pass
        params['http_headers'] = self.repo.http_headers
        # Older urlgrabber compatibility
        params['proxies'] = get_proxies(self.repo.proxy, self.repo.proxy_username, self.repo.proxy_password)

    @staticmethod
    def get_file(path, local_base=None):
        # pylint: disable=W0613
        # Called from import_kickstarts, not working for deb repo
        log2(0, 0, "Unable to download path %s from deb repo." % path, stream=sys.stderr)
        return None
