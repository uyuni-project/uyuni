# coding: utf-8
"""
Reposync via Salt library.
"""
from __future__ import absolute_import, unicode_literals

from shutil import rmtree

import configparser
import glob
import gzip
import os
import solv
import sys
import types
import urlgrabber

try:
    from urllib import urlencode
    from urlparse import urlsplit
except:
    from urllib.parse import urlsplit, urlencode

import xml.etree.ElementTree as etree

import salt.client
import salt.config

from spacewalk.common import checksum, rhnLog, fileutils
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.suseLib import get_proxy


# namespace prefix to parse patches.xml file
PATCHES_XML = '{http://novell.com/package/metadata/suse/patches}'
REPO_XML = '{http://linux.duke.edu/metadata/repo}'

CACHE_DIR = '/var/cache/rhn/reposync'
ZYPP_CACHE_PATH = 'var/cache/zypp'
ZYPP_RAW_CACHE_PATH = os.path.join(ZYPP_CACHE_PATH, 'raw')
ZYPP_SOLV_CACHE_PATH = os.path.join(ZYPP_CACHE_PATH, 'solv')
REPOSYNC_ZYPPER_CONF = '/etc/rhn/spacewalk-repo-sync/zypper.conf'


class ZyppoSync:
    """
    Wrapper for underlying package manager for the reposync via Salt.

    Example usage:

    >>> zyppo = ZyppoSync()
    >>> for idx, repo_meta in enumerate(zyppo.list_repos().values()):
    >>>    print(idx + 1, repo_meta["name"])
    >>>    print("  ", repo_meta["baseurl"])

    """
    def __init__(self, cfg_path="/etc/salt/minion", root=None):
        self._conf = salt.config.minion_config(cfg_path)
        self._conf["file_client"] = "local"
        self._conf["server_id_use_crc"] = "Adler32"
        self._root = root
        if self._root is not None:
            self._init_root(self._root)
        self._caller = salt.client.Caller(mopts=self._conf)

    def _init_root(self, root):
        """
        Creates a root environment for Zypper, but only if none is around.

        :return: None
        """
        try:
            for pth in [root, os.path.join(root, "etc/zypp/repos.d")]:
                if not os.path.exists(pth):
                    os.makedirs(pth)
        except PermissionError as exc:
            # TODO: a proper logging somehow?
            sys.stderr("Unable to initialise Zypper root for {}: {}".format(root, exc))
            raise
        self._conf["zypper_root"] = root

    def _get_call(self, key):
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

    def __getattr__(self, attr):
        """
        Prepare a callable on the requested
        attribute name.
        """
        return self._get_call(attr)


class ZypperRepo:
    def __init__(self, root, url, org):
       self.root = root
       self.baseurl = [url]
       self.basecachedir = os.path.join(CACHE_DIR, org)
       pkgdir = os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, org, 'stage')
       if not os.path.isdir(pkgdir):
           fileutils.makedirs(pkgdir, user='wwwrun', group='www')
       self.pkgdir = pkgdir
       self.urls = self.baseurl
       # Make sure baseurl ends with / and urljoin will work correctly
       if self.urls[0][-1] != '/':
           self.urls[0] += '/'


class RawSolvablePackage:
    def __init__(self, solvable):
        self.name = solvable.name
        self.raw_name = str(solvable)
        self.epoch, self.version, self.release = self._parse_solvable_evr(solvable.evr)
        self.arch = solvable.arch
        cksum = solvable.lookup_checksum(solv.SOLVABLE_CHECKSUM)
        self.checksum_type = cksum.typestr()
        self.checksum = cksum.hex()
        self.packagesize = solvable.lookup_num(solv.SOLVABLE_DOWNLOADSIZE)
        self.relativepath = solvable.lookup_location()[0]

    def __repr__(self):
        return "RawSolvablePackage({})".format(self.raw_name)

    @staticmethod
    def _parse_solvable_evr(evr):
        """
        Return the (epoch, version, release) tuple based on evr string.
        The "evr" string from libsolv is represented as: "epoch:version-release"

        https://github.com/openSUSE/libsolv/blob/master/src/solvable.h

        :returns: tuple
        """
        if evr in [None, '']:
           return ('', '', '')
        idx_epoch = evr.find(':')
        epoch = evr[:idx_epoch] if idx_epoch != -1 else ''
        idx_release = evr.find('-')
        if idx_release != -1:
            version = evr[idx_epoch + 1:idx_release]
            release = evr[idx_release + 1:]
        else:
            version = evr[idx_epoch + 1:]
            release = ''
        return epoch, version, release


class RepoMDNotFound(Exception):
    """ An exception thrown when not RepoMD is found. """
    pass


class SolvFileNotFound(Exception):
    """ An exception thrown when not Solv file is found. """
    pass


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

        if elem is not None:
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
        if urlsplit(url).scheme:
          self.url = url
        else:
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

        # read the proxy configuration in /etc/rhn/rhn.conf
        initCFG('server.satellite')

        # keep authtokens for mirroring
        (_scheme, _netloc, _path, query, _fragid) = urlsplit(url)
        if query:
            self.authtoken = query

        if CFG.http_proxy:
            self.proxy_url, self.proxy_user, self.proxy_pass = get_proxy(self.url)
        elif os.path.isfile(REPOSYNC_ZYPPER_CONF):
            zypper_cfg = configparser.ConfigParser()
            zypper_cfg.read_file(open(REPOSYNC_ZYPPER_CONF))
            section_name = None

            if zypper_cfg.has_section(self.name):
                section_name = self.name
            elif zypper_cfg.has_section(channel_label):
                section_name = channel_label
            elif zypper_cfg.has_section('main'):
                section_name = 'main'

            if section_name:
                if zypper_cfg.has_option(section_name, option='proxy'):
                    self.proxy_hostname = zypper_cfg.get(section_name, option='proxy')
                    self.proxy_url = "http://%s" % self.proxy_hostname

                if zypper_cfg.has_option(section_name, 'proxy_username'):
                    self.proxy_user = zypper_cfg.get(section_name, 'proxy_username')

                if zypper_cfg.has_option(section_name, 'proxy_password'):
                    self.proxy_pass = zypper_cfg.get(section_name, 'proxy_password')

        # Make sure baseurl ends with / and urljoin will work correctly
        self.urls = [url]
        if self.urls[0][-1] != '/':
            self.urls[0] += '/'

        # Add this repo to the Zypper env
        self.reponame = self.name
        for chr in ["$", " ", ".", ";"]:
            self.reponame = self.reponame.replace(chr, "_")
        self.channel_label = channel_label
        # SUSE vendor repositories belongs to org = NULL
        root = os.path.join(CACHE_DIR, str(org or "NULL"), self.channel_label or self.reponame)
        self.salt = ZyppoSync(root=root)
        self.repo = ZypperRepo(root=root, url=self.url, org=self.org)
        zypp_repo_url = self._prep_zypp_repo_url(url)

        #REMOVE: Manually call Zypper
        repo_cfg = '''[{reponame}]
enabled=1
autorefresh=0
baseurl={baseurl}
type=rpm-md
'''
        with open(os.path.join(root, "etc/zypp/repos.d", str(self.channel_label or self.reponame) + ".repo"), "w") as repo_conf_file:
            repo_conf_file.write(repo_cfg.format(reponame=self.channel_label or self.reponame, baseurl=zypp_repo_url))
        os.system("zypper --root {} --gpg-auto-import-keys --no-gpg-checks ref".format(root))

#        self.salt.mod_repo(name, url=zypp_repo_url, gpgautoimport=True, gpgcheck=True,
#                           alias=self.channel_label or self.reponame)
#        self.salt.refresh_db()

        self.num_packages = 0
        self.num_excluded = 0
        self.gpgkey_autotrust = None
        self.groupsfile = None

    def error_msg(self, message):
        rhnLog.log_clean(0, message)
        sys.stderr.write(str(message) + "\n")

    def _prep_zypp_repo_url(self, url):
        """
        Prepare the repository baseurl to use in the Zypper repo file.
        This will add the HTTP Proxy and Client certificate settings as part of
        the url parameters to be interpreted by CURL during the Zypper execution.

        :returns: str
        """
        ret_url = None
        query_params = {}
        if self.proxy_hostname:
            query_params['proxy'] = self.proxy_hostname
        if self.proxy_user:
            query_params['proxyuser'] = self.proxy_user
        if self.proxy_pass:
            query_params['proxypass'] = self.proxy_pass
        if self.sslcacert:
            query_params['ssl_capath'] = self.sslcacert
        if self.sslclientcert:
            query_params['ssl_clientcert'] = self.sslclientcert
        if self.sslclientkey:
            query_params['ssl_clientkey'] = self.sslclientkey
        new_query = urlencode(query_params, doseq=True)
        if self.authtoken:
            ret_url = "{0}&{1}".format(url, new_query)
        else:
            ret_url = "{0}?{1}".format(url, new_query) if new_query else url
        return ret_url

    def _md_exists(self, tag):
        """
        Check if the requested metadata exists on the repository

        :returns: bool
        """
        return bool(self._retrieve_md_path(tag))

    def _retrieve_md_path(self, tag):
        """
        Return the path to the requested metadata if exists

        :returns: str
        """
        _md_files = glob.glob(self._get_repodata_path() + "/*{}.xml.gz".format(tag)) or glob.glob(self._get_repodata_path() + "/*{}.xml".format(tag))
        if _md_files:
            return _md_files[0]
        return None

    def _get_repodata_path(self):
        """
        Return the path to the repository repodata directory

        :returns: str
        """
        return os.path.join(self.repo.root, ZYPP_RAW_CACHE_PATH, self.name, "repodata")

    def get_md_checksum_type(self):
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
        Return susedata metadata from the repository if available

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
        Return products metadata from the repository if available

        :returns: list
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
        Return update metadata from the repository if available

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
            return ('updateinfo', notices.values())
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
        """
        Return path to the repository groups metadata file if available

        :returns: str
        """
        # groups -> /var/cache/rhn/reposync/1/CentOS_7_os_x86_64/bc140c8149fc43a5248fccff0daeef38182e49f6fe75d9b46db1206dc25a6c1c-c7-x86_64-comps.xml.gz
        groups = None
        if self._md_exists('comps'):
            groups = self._retrieve_md_path('comps')
        return groups

    def get_modules(self):
        """
        Return path to the repository modules metadata file if available

        :returns: str
        """
        modules = None
        if self._md_exists('modules'):
            modules = self._retrieve_md_path('modules')
        return modules

    def raw_list_packages(self, filters=None):
        pool = solv.Pool()
        repo = pool.add_repo(str(self.channel_label or self.reponame))
        solv_path = os.path.join(self.repo.root, ZYPP_SOLV_CACHE_PATH, self.channel_label or self.reponame, 'solv')
        if not repo.add_solv(solv.xfopen(str(solv_path)), 0):
            raise SolvFileNotFound(solv_path)
        rawpkglist = []
        for solvable in repo.solvables_iter():
            # Solvables with ":" in name are not packages
            if ':' in solvable.name:
                continue
            rawpkglist.append(RawSolvablePackage(solvable))
        self.num_packages = len(rawpkglist)
        return rawpkglist

    def list_packages(self, filters, latest):
        """
        List available packages.

        :returns: list
        """
        pool = solv.Pool()
        repo = pool.add_repo(str(self.channel_label or self.reponame))
        solv_path = os.path.join(self.repo.root, ZYPP_SOLV_CACHE_PATH, self.channel_label or self.reponame, 'solv')
        if not repo.add_solv(solv.xfopen(str(solv_path)), 0):
            raise SolvFileNotFound(solv_path)

        #TODO: Implement latest
        #if latest:
        #     pkglist = pkglist.returnNewestByNameArch()

        #TODO: Implement sort
        #pkglist.sort(self._sort_packages)

        to_return = []
        for pack in repo.solvables:
            # Solvables with ":" in name are not packages
            if ':' in pack.name:
                continue
            new_pack = ContentPackage()
            epoch, version, release = RawSolvablePackage._parse_solvable_evr(pack.evr)
            new_pack.setNVREA(pack.name, version, release, epoch, pack.arch)
            new_pack.unique_id = RawSolvablePackage(pack)
            checksum = pack.lookup_checksum(solv.SOLVABLE_CHECKSUM)
            new_pack.checksum_type = checksum.typestr()
            new_pack.checksum = checksum.hex()
            to_return.append(new_pack)

        self.num_packages = len(to_return)
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

    def clear_cache(self, directory=None, keep_repomd=False):
        """
        Clear all cache files from the environment.
        """
        # TODO: This clean_cache method is called by reposync just
        # after ContentSource is instanciated. We need to prevent that
        # metadata files and update info is wiped here.
        # Maybe: os.system("zypper --root {} clean".format(self.repo.root))
        return

    def get_metadata_paths(self):
        """
        Simply return the 'primary' and 'updateinfo' path from repomd

        Example output:
        [
            (
                'repodata/bc140c8149fc43a5248fccff0daeef38182e49f6fe75d9b46db1206dc25a6c1c-c7-x86_64-comps.xml.gz',
                ('sha256', 'bc140c8149fc43a5248fccff0daeef38182e49f6fe75d9b46db1206dc25a6c1c')
            ),
            (
                'repodata/6614b3605d961a4aaec45d74ac4e5e713e517debb3ee454a1c91097955780697-primary.sqlite.bz2',
                ('sha256', '6614b3605d961a4aaec45d74ac4e5e713e517debb3ee454a1c91097955780697')
            )
        ]

        :returns: list
        """
        def get_location(data_item):
            for sub_item in data_item:
                if sub_item.tag.endswith("location"):
                    return sub_item.attrib.get("href")

        def get_checksum(data_item):
            for sub_item in data_item:
                if sub_item.tag.endswith("checksum"):
                    return sub_item.attrib.get("type"), sub_item.text

        if self._md_exists('repomd'):
            repomd_path = self._retrieve_md_path('repomd')
        else:
            raise RepoMDNotFound(self._get_repodata_path())
        repomd = open(repomd_path, 'rb')
        files = {}
        for _event, elem in etree.iterparse(repomd):
            if elem.tag.endswith("data"):
                if elem.attrib.get("type") == "primary_db":
                    files['primary'] = (get_location(elem), get_checksum(elem))
                elif elem.attrib.get("type") == "primary" and 'primary' not in files:
                    files['primary'] = (get_location(elem), get_checksum(elem))
                elif elem.attrib.get("type") == "updateinfo":
                    files['updateinfo'] = (get_location(elem), get_checksum(elem))
                elif elem.attrib.get("type") == "group_gz":
                    files['group'] = (get_location(elem), get_checksum(elem))
                elif elem.attrib.get("type") == "group" and 'group' not in files:
                    files['group'] = (get_location(elem), get_checksum(elem))
                elif elem.attrib.get("type") == "modules":
                    files['modules'] = (get_location(elem), get_checksum(elem))
        repomd.close()
        return list(files.values())

    def repomd_up_to_date(self):
        """
        Check if repomd.xml has been updated by spacewalk.

        :returns: bool
        """
        if self._md_exists('repomd'):
            repomd_old_path = self._retrieve_md_path('repomd')
            repomd_new_path = os.path.join(self._get_repodata_path(), "repomd.xml.new")
            # Newer file not available? Don't do anything. It should be downloaded before this.
            if not os.path.isfile(repomd_new_path):
                return True
            return checksum.getFileChecksum('sha256', filename=repomd_old_path) == checksum.getFileChecksum('sha256', filename=repomd_new_path)
        else:
            return False

    # Get download parameters for threaded downloader
    def set_download_parameters(self, params, relative_path, target_file, checksum_type=None, checksum_value=None, bytes_range=None):
        # Create directories if needed
        target_dir = os.path.dirname(target_file)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir, int('0755', 8))

        params['authtoken'] = self.authtoken
        params['urls'] = self.repo.urls
        params['relative_path'] = relative_path
        params['authtoken'] = self.authtoken
        params['target_file'] = target_file
        params['ssl_ca_cert'] = self.sslcacert
        params['ssl_client_cert'] = self.sslclientcert
        params['ssl_client_key'] = self.sslclientkey
        params['checksum_type'] = checksum_type
        params['checksum'] = checksum_value
        params['bytes_range'] = bytes_range
        params['proxy'] = self.proxy_url
        params['proxy_username'] = self.proxy_user
        params['proxy_password'] = self.proxy_pass
        params['http_headers'] = self.http_headers
        # Older urlgrabber compatibility
        params['proxies'] = get_proxies(self.proxy_url, self.proxy_user, self.proxy_pass)

    def get_file(self, path, local_base=None):
        try:
            try:
                temp_file = ""
                if local_base is not None:
                    target_file = os.path.join(local_base, path)
                    target_dir = os.path.dirname(target_file)
                    if not os.path.exists(target_dir):
                        os.makedirs(target_dir, int('0755', 8))
                    temp_file = target_file + '..download'
                    if os.path.exists(temp_file):
                        os.unlink(temp_file)
                    downloaded = urlgrabber.urlgrab(path, temp_file)
                    os.rename(downloaded, target_file)
                    return target_file
                else:
                    return urlgrabber.urlread(path)
            except URLGrabError:
                return
        finally:
            if os.path.exists(temp_file):
                os.unlink(temp_file)

    def set_ssl_options(self, ca_cert, client_cert, client_key):
        self.sslcacert = ca_cert_file
        self.sslclientcert = client_cert_file
        self.sslclientkey = client_key_file
