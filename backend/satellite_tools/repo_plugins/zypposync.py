# coding: utf-8
"""
Reposync via Salt library.
"""
from __future__ import absolute_import, unicode_literals

import glob
import gzip
import os
import sys
import types
import xml.etree.ElementTree as etree

import salt.client
import salt.config

from spacewalk.common import rhnLog

# namespace prefix to parse patches.xml file
PATCHES_XML = '{http://novell.com/package/metadata/suse/patches}'
REPO_XML = '{http://linux.duke.edu/metadata/repo}'

ZYPP_CACHE_ROOT_PATH = '/var/cache/rhn/reposync'
ZYPP_RAW_CACHE_PATH = 'var/cache/zypp/raw'


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
        self.root = root
        if self.root is not None:
            self._init_root(self.root)
        self._caller = salt.client.Caller(mopts=self._conf)

    def _init_root(self, root:str) -> None:
        """
        Creates a root environment for Zypper, but only if none is around.

        :return: None
        """
        if not os.path.exists(root):
            try:
                for pth in [root, os.path.join(root, "etc/zypp/repos.d")]:
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


class UpdateNoticeException(Exception):
    """ An exception thrown for bad UpdateNotice data. """
    pass


class UpdateNotice(object):
    def __init__(self, elem=None, repoid=None, vlogger=None):
        self._md = {
            'from'             : '',
            'type'             : '',
            'title'            : '',
            'release'          : '',
            'status'           : '',
            'version'          : '',
            'pushcount'        : '',
            'update_id'        : '',
            'issued'           : '',
            'updated'          : '',
            'description'      : '',
            'rights'           : '',
            'severity'         : '',
            'summary'          : '',
            'solution'         : '',
            'references'       : [],
            'pkglist'          : [],
            'reboot_suggested' : False
        }

        if elem:
            self._parse(elem)

    def __getitem__(self, item):
        """ Allows scriptable metadata access (ie: un['update_id']). """
        if type(item) is int:
            return sorted(self._md)[item]
        ret = self._md.get(item)
        if ret == '':
            ret = None
        return ret

    def __setitem__(self, item, val):
        self._md[item] = val

    def _parse(self, elem):
        """
        Parse an update element::
            <!ELEMENT update (id, synopsis?, issued, updated,
                              references, description, rights?,
                              severity?, summary?, solution?, pkglist)>
                <!ATTLIST update type (errata|security) "errata">
                <!ATTLIST update status (final|testing) "final">
                <!ATTLIST update version CDATA #REQUIRED>
                <!ATTLIST update from CDATA #REQUIRED>
        """
        if elem.tag == 'update':
            for attrib in ('from', 'type', 'status', 'version'):
                self._md[attrib] = elem.attrib.get(attrib)
            for child in elem:
                if child.tag == 'id':
                    if not child.text:
                        raise UpdateNoticeException("No id element found")
                    self._md['update_id'] = child.text
                elif child.tag == 'pushcount':
                    self._md['pushcount'] = child.text
                elif child.tag == 'issued':
                    self._md['issued'] = child.attrib.get('date')
                elif child.tag == 'updated':
                    self._md['updated'] = child.attrib.get('date')
                elif child.tag == 'references':
                    self._parse_references(child)
                elif child.tag == 'description':
                    self._md['description'] = child.text
                elif child.tag == 'rights':
                    self._md['rights'] = child.text
                elif child.tag == 'severity':
                    self._md[child.tag] = child.text
                elif child.tag == 'summary':
                    self._md['summary'] = child.text
                elif child.tag == 'solution':
                    self._md['solution'] = child.text
                elif child.tag == 'pkglist':
                    self._parse_pkglist(child)
                elif child.tag == 'title':
                    self._md['title'] = child.text
                elif child.tag == 'release':
                    self._md['release'] = child.text
        else:
            raise UpdateNoticeException('No update element found')

    def _parse_references(self, elem):
        """
        Parse the update references::
            <!ELEMENT references (reference*)>
            <!ELEMENT reference>
                <!ATTLIST reference href CDATA #REQUIRED>
                <!ATTLIST reference type (self|other|cve|bugzilla) "self">
                <!ATTLIST reference id CDATA #IMPLIED>
                <!ATTLIST reference title CDATA #IMPLIED>
        """
        for reference in elem:
            if reference.tag == 'reference':
                data = {}
                for refattrib in ('id', 'href', 'type', 'title'):
                    data[refattrib] = reference.attrib.get(refattrib)
                self._md['references'].append(data)
            else:
                raise UpdateNoticeException('No reference element found')

    def _parse_pkglist(self, elem):
        """
        Parse the package list::
            <!ELEMENT pkglist (collection+)>
            <!ELEMENT collection (name?, package+)>
                <!ATTLIST collection short CDATA #IMPLIED>
                <!ATTLIST collection name CDATA #IMPLIED>
            <!ELEMENT name (#PCDATA)>
        """
        for collection in elem:
            data = { 'packages' : [] }
            if 'short' in collection.attrib:
                data['short'] = collection.attrib.get('short')
            for item in collection:
                if item.tag == 'name':
                    data['name'] = item.text
                elif item.tag == 'package':
                    data['packages'].append(self._parse_package(item))
            self._md['pkglist'].append(data)

    def _parse_package(self, elem):
        """
        Parse an individual package::
            <!ELEMENT package (filename, sum, reboot_suggested)>
                <!ATTLIST package name CDATA #REQUIRED>
                <!ATTLIST package version CDATA #REQUIRED>
                <!ATTLIST package release CDATA #REQUIRED>
                <!ATTLIST package arch CDATA #REQUIRED>
                <!ATTLIST package epoch CDATA #REQUIRED>
                <!ATTLIST package src CDATA #REQUIRED>
            <!ELEMENT reboot_suggested (#PCDATA)>
            <!ELEMENT filename (#PCDATA)>
            <!ELEMENT sum (#PCDATA)>
                <!ATTLIST sum type (md5|sha1) "sha1">
        """
        package = {}
        for pkgfield in ('arch', 'epoch', 'name', 'version', 'release', 'src'):
            package[pkgfield] = elem.attrib.get(pkgfield)

        #  Bad epoch and arch data is the most common (missed) screwups.
        # Deal with bad epoch data.
        if not package['epoch'] or package['epoch'][0] not in '0123456789':
            package['epoch'] = None

        for child in elem:
            if child.tag == 'filename':
                package['filename'] = child.text
            elif child.tag == 'sum':
                package['sum'] = (child.attrib.get('type'), child.text)
            elif child.tag == 'reboot_suggested':
                self._md['reboot_suggested'] = True
        return package


class ContentSource:
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

        # Add this repo to the Zypper env
        reponame = self.name.replace(" ", "-")
        # SUSE vendor repositories belongs to org = NULL
        root = os.path.join(ZYPP_CACHE_ROOT_PATH, str(org or "NULL"), reponame)
        self.repo = ZyppoSync(root=root)
        self.repo.mod_repo(name, url=url, gpgautoimport=True, gpgcheck=False,
                           alias=channel_label or reponame)
        self.repo.refresh_db()

        self.num_packages = 0
        self.num_excluded = 0

        # keep authtokens for mirroring
        _scheme, _netloc, _path, query, _fragid = urlparse.urlsplit(url)
        self.authtoken = query if query else None

    def error_msg(self, message):
        rhnLog.log_clean(0, message)
        sys.stderr.write(str(message) + "\n")

    def _md_exists(self, tag:str) -> bool:
        return bool(self._retrieve_md_path(tag))

    def _retrieve_md_path(self, tag:str) -> str:
        _md_files = glob.glob(self._get_repodata_path() + "/*{}.xml.gz".format(tag)) or glob.glob(self._get_repodata_path() + "/*{}.xml".format(tag))
        if _md_files:
            return _md_files[0]
        return None

    def _get_repodata_path(self) -> str:
        return os.path.join(self.repo.root, ZYPP_RAW_CACHE_PATH, self.name, "repodata")

    def get_md_checksum_type(self) -> (str, int):
        """
        Return the checksum type of the primary.xml if exists, otherwise
        default output is "sha1".

        :returns: str
        """
        if self._md_exists('repomd'):
            repomd_path = self._retrieve_md_path('repomd')
            infile = repomd_path.endswith('.gz') and gzip.open(repomd_path) or open(repomd_path, 'rt')
            for repodata in etree.parse(infile).getroot():
                if repodata.get('type') == 'primary':
                    checksum_elem = repodata.find(REPO_XML+'checksum')
                    return checksum_elem.get('type')
        return "sha1"

    def _fix_encoding(text):
        if text is None:
            return None
        if isinstance(text, str):
            return str.encode(text, 'utf-8')
        else:
            return str(text)

    def get_susedata(self):
        """
        Return ??

        :returns: list
        """
        susedata = []
        if self._md_exists('susedata'):
            data_path = self._retrieve_md_path('susedata')
            infile = data_path.endswith('.gz') and gzip.open(data_path) or open(data_path, 'rt')
            for package in etree.parse(infile).getroot():
                d = {}
                d['pkgid'] = package.get('pkgid')
                d['name'] = package.get('name')
                d['arch'] = package.get('arch')
                d['keywords'] = []
                for child in package:
                    # we use "endswith" because sometimes it has a namespace
                    # and sometimes not :-(
                    if child.tag.endswith('version'):
                        d['version'] = child.get('ver')
                        d['release'] = child.get('rel')
                        d['epoch'] = child.get('epoch')
                        if d['epoch'] == '0' or d['epoch'] == '':
                            d['epoch'] = None
                        if child.get('arch'):
                            d['arch'] = child.get('arch')

                    elif child.tag.endswith('keyword'):
                        d['keywords'].append(child.text)
                    elif child.tag == 'eula':
                        d['eula'] = child.text
                susedata.append(d)
        return susedata

    def get_products(self):
        """
        Return list of products
        :returns: ?
        """
        products = []
        if self._md_exists('products'):
            data_path = self._retrieve_md_path('products')
            infile = data_path.endswith('.gz') and gzip.open(data_path) or open(data_path, 'rt')
            for product in etree.parse(infile).getroot():
                p = {}
                p['name'] = product.find('name').text
                p['arch'] = product.find('arch').text
                version = product.find('version')
                p['version'] = version.get('ver')
                p['release'] = version.get('rel')
                p['epoch'] = version.get('epoch')
                p['vendor'] = _fix_encoding(product.find('vendor').text)
                p['summary'] = _fix_encoding(product.find('summary').text)
                p['description'] = _fix_encoding(product.find('description').text)
                if p['epoch'] == '0':
                    p['epoch'] = None
                products.append(p)
        return products

    def get_updates(self):
        """
        Return available updates.

        :returns: list
        """
        if self._md_exists('updateinfo'):
            notices = {}
            updates_path = self._retrieve_md_path('updateinfo')
            infile = updates_path.endswith('.gz') and gzip.open(updates_path) or open(updates_path, 'rt')
            for _event, elem in etree.iterparse(infile):
                if elem.tag == 'update':
                    un = UpdateNotice(elem)
                    key = un['update_id']
                    key = "%s-%s" % (un['update_id'], un['version'])
                    if key not in notices:
                        notices[key] = un
            return ('updateinfo', notices)
        elif self._md_exists('patches'):
            patches_path = self._retrieve_md_path('patches')
            infile = patches_path.endswith('.gz') and gzip.open(patches_path) or open(patches_path, 'rt')
            notices = []
            for patch in etree.parse(infile).getroot():
                checksum_elem = patch.find(PATCHES_XML+'checksum')
                location_elem = patch.find(PATCHES_XML+'location')
                relative = location_elem.get('href')
                checksum_type = checksum_elem.get('type')
                checksum = checksum_elem.text
                filename = os.path.join(self._get_repodata_path(), os.path.basename(relative))
                try:
                    notices.append(etree.parse(filename).getroot())
                except SyntaxError as e:
                    self.error_msg("Could not parse %s. "
                                   "The file is not a valid XML document. %s" %
                                   (filename, e.msg))
                    continue
            return ('patches', notices)
        else:
            return ('', [])

    def get_groups(self):
        # groups -> /var/cache/rhn/reposync/1/CentOS_7_os_x86_64/bc140c8149fc43a5248fccff0daeef38182e49f6fe75d9b46db1206dc25a6c1c-c7-x86_64-comps.xml.gz
        groups = None
        if self._md_exists('comps'):
            groups = self._retrieve_md_path('comps')
        return groups

    def get_modules(self):
        modules = None
        if self._md_exists('modules'):
            modules = self._retrieve_md_path('modules')
        return modules

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
