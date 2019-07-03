# coding: utf-8
#
# Copyright (c) 2008--2018 Red Hat, Inc.
# Copyright (c) 2010--2019 SUSE LINUX GmbH, Nuernberg, Germany.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

from __future__ import absolute_import, unicode_literals

from shutil import rmtree, copytree

import configparser
import fnmatch
import glob
import gzip
import os
import re
import solv
import subprocess
import sys
import tempfile
import types
import urlgrabber

try:
    from urllib import urlencode, unquote
    from urlparse import urlsplit, urlparse, urlunparse
except:
    from urllib.parse import urlsplit, urlencode, urlparse, urlunparse, unquote

import xml.etree.ElementTree as etree

from functools import cmp_to_key
from salt.utils.versions import LooseVersion
from spacewalk.common import checksum, rhnLog, fileutils
from spacewalk.satellite_tools.repo_plugins import ContentPackage, CACHE_DIR
from spacewalk.satellite_tools.download import get_proxies
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.suseLib import get_proxy


# namespace prefix to parse patches.xml file
PATCHES_XML = '{http://novell.com/package/metadata/suse/patches}'
REPO_XML = '{http://linux.duke.edu/metadata/repo}'
METALINK_XML = '{http://www.metalinker.org/}'

CACHE_DIR = '/var/cache/rhn/reposync'
SPACEWALK_LIB = '/var/lib/spacewalk'
SPACEWALK_GPG_KEYRING = os.path.join(SPACEWALK_LIB, 'gpgdir/pubring.gpg')
ZYPP_CACHE_PATH = 'var/cache/zypp'
ZYPP_RAW_CACHE_PATH = os.path.join(ZYPP_CACHE_PATH, 'raw')
ZYPP_SOLV_CACHE_PATH = os.path.join(ZYPP_CACHE_PATH, 'solv')
REPOSYNC_ZYPPER_ROOT = os.path.join(SPACEWALK_LIB, "reposync/root")
REPOSYNC_ZYPPER_RPMDB_PATH = os.path.join(REPOSYNC_ZYPPER_ROOT, 'var/lib/rpm')
REPOSYNC_ZYPPER_CONF = '/etc/rhn/spacewalk-repo-sync/zypper.conf'

RPM_PUBKEY_VERSION_RELEASE_RE = re.compile(r'^gpg-pubkey-([0-9a-fA-F]+)-([0-9a-fA-F]+)')


class ZyppoSync:
    """
    This class prepares a environment for running Zypper inside a dedicated reposync root

    """
    def __init__(self, root=None):
        self._root = root
        if self._root is not None:
            self._init_root(self._root)

    def _init_root(self, root):
        """
        Creates a root environment for Zypper, but only if none is around.

        :return: None
        """
        try:
            for pth in [root, os.path.join(root, "etc/zypp/repos.d"), REPOSYNC_ZYPPER_ROOT]:
                if not os.path.exists(pth):
                    os.makedirs(pth)
        except Exception as exc:
            msg = "Unable to initialise Zypper root for {}: {}".format(root, exc)
            rhnLog.log_clean(0, msg)
            sys.stderr.write(str(msg) + "\n")
            raise
        try:
            # Synchronize new GPG keys that come from the Spacewalk GPG keyring
            self.__synchronize_gpg_keys()
        except Exception as exc:
            msg = "Unable to synchronize Spacewalk GPG keyring: {}".format(exc)
            rhnLog.log_clean(0, msg)
            sys.stderr.write(str(msg) + "\n")

    def __synchronize_gpg_keys(self):
        """
        This method does update the Zypper RPM database with new keys coming from the Spacewalk GPG keyring

        """
        spacewalk_gpg_keys = {}
        zypper_gpg_keys = {}
        with tempfile.NamedTemporaryFile() as f:
            # Collect GPG keys from the Spacewalk GPG keyring
            os.system("gpg -q --batch --no-options --no-default-keyring --no-permission-warning --keyring {} --export -a > {}".format(SPACEWALK_GPG_KEYRING, f.name))
            process = subprocess.Popen(['gpg', '--verbose', '--with-colons', f.name], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
            for line in process.stdout.readlines():
               line_l = line.decode().split(":")
               if line_l[0] == "sig" and "selfsig" in line_l[10]:
                   spacewalk_gpg_keys.setdefault(line_l[4][8:].lower(), []).append(format(int(line_l[5]), 'x'))

            # Collect GPG keys from reposync Zypper RPM database
            process = subprocess.Popen(['rpm', '-q', 'gpg-pubkey', '--dbpath', REPOSYNC_ZYPPER_RPMDB_PATH], stdout=subprocess.PIPE)
            for line in process.stdout.readlines():
                match = RPM_PUBKEY_VERSION_RELEASE_RE.match(line.decode())
                if match:
                    zypper_gpg_keys[match.groups()[0]] = match.groups()[1]

            # Compare GPG keys and remove keys from reposync that are going to be imported with a newer release.
            for key in zypper_gpg_keys:
                # If the GPG key id already exists, is that new key actually newer? We need to check the release
                release_i = int(zypper_gpg_keys[key], 16)
                if key in spacewalk_gpg_keys and any(int(i, 16) > release_i for i in spacewalk_gpg_keys[key]):
                    # This GPG key has a newer release on the Spacewalk GPG keyring that on the reposync Zypper RPM database.
                    # We delete this key from the RPM database to allow importing the newer version.
                    os.system("rpm --dbpath {} -e gpg-pubkey-{}-{}".format(REPOSYNC_ZYPPER_RPMDB_PATH, key, zypper_gpg_keys[key]))

            # Finally, once we deleted the existing old key releases from the Zypper RPM database
            # we proceed to import all keys from the Spacewalk GPG keyring. This will allow new GPG
            # keys release are upgraded in the Zypper keyring since rpmkeys does not handle the upgrade
            # properly
            os.system("rpmkeys --dbpath {} --import {}".format(REPOSYNC_ZYPPER_RPMDB_PATH, f.name))


class ZypperRepo:
    def __init__(self, root, url, org):
       self.root = root
       self.baseurl = [url]
       self.basecachedir = os.path.join(CACHE_DIR, org)
       self.pkgdir = os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, org, 'stage')
       self.urls = self.baseurl
       # Make sure baseurl ends with / and urljoin will work correctly
       if self.urls[0][-1] != '/':
           self.urls[0] += '/'
       # Make sure root paths are created
       if not os.path.isdir(self.root):
           fileutils.makedirs(self.root, user='wwwrun', group='www')
       if not os.path.isdir(self.pkgdir):
           fileutils.makedirs(self.pkgdir, user='wwwrun', group='www')
       self.is_configured = False
       self.includepkgs = []
       self.exclude = []


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


class RepoMDError(Exception):
    """ An exception thrown when not RepoMD is found. """
    pass


class SolvFileNotFound(Exception):
    """ An exception thrown when not Solv file is found. """
    pass


class UpdateNoticeException(Exception):
    """ An exception thrown for bad UpdateNotice data. """
    pass


class UpdateNotice(object):
    """
    Simplified UpdateNotice class implementation
    https://github.com/rpm-software-management/yum/blob/master/yum/update_md.py

    A single update notice (for instance, a security fix).
    """
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

        # Get the global HTTP Proxy settings from DB or per-repo
        # settings on /etc/rhn/spacewalk-repo-sync/zypper.conf
        if CFG.http_proxy:
            self.proxy_url, self.proxy_user, self.proxy_pass = get_proxy(self.url)
            self.proxy_hostname = self.proxy_url
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

        self._authenticate(url)

        # Make sure baseurl ends with / and urljoin will work correctly
        self.urls = [url]
        if self.urls[0][-1] != '/':
            self.urls[0] += '/'

        # Exclude non-valid characters from reponame
        self.reponame = self.name
        for chr in ["$", " ", ".", ";"]:
            self.reponame = self.reponame.replace(chr, "_")
        self.channel_label = channel_label
        # SUSE vendor repositories belongs to org = NULL
        root = os.path.join(CACHE_DIR, str(org or "NULL"), self.channel_label or self.reponame)

        self.repo = ZypperRepo(root=root, url=self.url, org=self.org)
        self.num_packages = 0
        self.num_excluded = 0
        self.gpgkey_autotrust = None
        self.groupsfile = None

    def _get_mirror_list(self, repo, url):
        mirrorlist_path = os.path.join(repo.root, 'mirrorlist.txt')
        returnlist = []
        content = []
        try:
            urlgrabber.urlgrab(url, mirrorlist_path)
        except Exception as exc:
            # no mirror list found continue without
            return returnlist

        def _replace_and_check_url(url_list):
            goodurls = []
            skipped = None
            for url in url_list:
                # obvious bogons get ignored b/c, we could get more interesting checks but <shrug>
                if url in ['', None]:
                    continue
                try:
                    # This started throwing ValueErrors, BZ 666826
                    (s,b,p,q,f,o) = urlparse(url)
                    if p[-1] != '/':
                        p = p + '/'
                except (ValueError, IndexError, KeyError) as e:
                    s = 'blah'

                if s not in ['http', 'ftp', 'file', 'https']:
                    skipped = url
                    continue
                else:
                    goodurls.append(urlunparse((s,b,p,q,f,o)))
            return goodurls

        try:
           with open(mirrorlist_path, 'r') as mirrorlist_file:
               content = mirrorlist_file.readlines()
        except Exception as exc:
            self.error_msg("Could not read mirrorlist: {}".format(exc))

        try:
            # Try to read a metalink XML
            for files in etree.parse(mirrorlist_path).getroot():
                file_elem = files.find(METALINK_XML+'file')
                if file_elem.get('name') == 'repomd.xml':
                    _urls = file_elem.find(METALINK_XML+'resources').findall(METALINK_XML+'url')
                    for _url in _urls:
                        # The mirror urls in the metalink file are for repomd.xml so it
                        # gives a list of mirrors for that one file, but we want the list
                        # of mirror baseurls. Joy of reusing other people's stds. :)
                        if not _url.text.endswith("/repodata/repomd.xml"):
                            continue
                        returnlist.append(_url.text[:-len("/repodata/repomd.xml")])
        except Exception as exc:
            # If no metalink XML, we try to read a mirrorlist
            for line in content:
                if re.match('^\s*\#.*', line) or re.match('^\s*$', line):
                    continue
                mirror = re.sub('\n$', '', line) # no more trailing \n's
                (mirror, count) = re.subn('\$ARCH', '$BASEARCH', mirror)
                returnlist.append(mirror)

        returnlist = _replace_and_check_url(returnlist)

        try:
           # Write the final mirrorlist that is going to be pass to Zypper
           with open(mirrorlist_path, 'w') as mirrorlist_file:
               mirrorlist_file.write(os.linesep.join(returnlist))
        except Exception as exc:
            self.error_msg("Could not write the calculated mirrorlist: {}".format(exc))
        return returnlist

    def setup_repo(self, repo, uln_repo=False):
        """
        Setup repository and fetch metadata
        """
        self.zypposync = ZyppoSync(root=repo.root)
        zypp_repo_url = self._prep_zypp_repo_url(self.url)

        mirrorlist = self._get_mirror_list(repo, zypp_repo_url)
        repo.baseurl = repo.baseurl + mirrorlist
        repo.urls = repo.baseurl

        # Manually call Zypper
        repo_cfg = '''[{reponame}]
enabled=1
autorefresh=0
{repo_url}={url}
gpgcheck={gpgcheck}
repo_gpgcheck={gpgcheck}
type=rpm-md
'''
        if uln_repo:
           _url = 'plugin:spacewalk-uln-resolver?url={}'.format(self._url_orig)
        else:
           _url = zypp_repo_url if not mirrorlist else os.path.join(repo.root, 'mirrorlist.txt')

        with open(os.path.join(repo.root, "etc/zypp/repos.d", str(self.channel_label or self.reponame) + ".repo"), "w") as repo_conf_file:
            repo_conf_file.write(repo_cfg.format(
                reponame=self.channel_label or self.reponame,
                repo_url='baseurl' if not mirrorlist else 'mirrorlist',
                url=_url,
                gpgcheck="0" if self.insecure else "1"
            ))
        zypper_cmd = "zypper"
        if not self.interactive:
            zypper_cmd = "{} -n".format(zypper_cmd)
        ret_error = os.system("{} --root {} --reposd-dir {} --cache-dir {} --raw-cache-dir {} --solv-cache-dir {} ref".format(
            zypper_cmd,
            REPOSYNC_ZYPPER_ROOT,
            os.path.join(repo.root, "etc/zypp/repos.d/"),
            'var/lib/rpm',
            os.path.join(repo.root, "var/cache/zypp/raw/"),
            os.path.join(repo.root, "var/cache/zypp/solv/")
        ))
        if ret_error:
            raise RepoMDError("Cannot access repository. Maybe repository GPG keys are not imported")

        repo.is_configured = True

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
            # Since Zypper only accepts CAPATH, we need to split the certificates bundle
            # and run "c_rehash" on our custom CAPATH
            _ssl_capath = os.path.dirname(self.sslcacert)
            msg = "Preparing custom SSL CAPATH at {}".format(_ssl_capath)
            rhnLog.log_clean(0, msg)
            sys.stdout.write(str(msg) + "\n")
            os.system("awk 'BEGIN {{c=0;}} /BEGIN CERT/{{c++}} {{ print > \"{0}/cert.\" c \".pem\"}}' < {1}".format(_ssl_capath, self.sslcacert))
            os.system("c_rehash {}".format(_ssl_capath))
            query_params['ssl_capath'] = _ssl_capath
        if self.sslclientcert:
            query_params['ssl_clientcert'] = self.sslclientcert
        if self.sslclientkey:
            query_params['ssl_clientkey'] = self.sslclientkey
        new_query = unquote(urlencode(query_params, doseq=True))
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
        if not self.repo.is_configured:
            self.setup_repo(self.repo)
        return bool(self._retrieve_md_path(tag))

    def _retrieve_md_path(self, tag):
        """
        Return the path to the requested metadata if exists

        :returns: str
        """
        if not self.repo.is_configured:
            self.setup_repo(self.repo)
        _md_files = glob.glob(self._get_repodata_path() + "/*{}.xml.gz".format(tag)) or glob.glob(self._get_repodata_path() + "/*{}.xml".format(tag))
        if _md_files:
            return _md_files[0]
        return None

    def _get_repodata_path(self):
        """
        Return the path to the repository repodata directory

        :returns: str
        """
        if not self.repo.is_configured:
            self.setup_repo(self.repo)
        return os.path.join(self.repo.root, ZYPP_RAW_CACHE_PATH, self.channel_label or self.reponame, "repodata")

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

    def _get_solvable_packages(self):
        """
        Return the full list of solvable packages available at the configured repo.
        This information is read from the solv file created by Zypper.

        :returns: list
        """
        if not self.repo.is_configured:
            self.setup_repo(self.repo)
        self.solv_pool = solv.Pool()
        self.solv_repo = self.solv_pool.add_repo(str(self.channel_label or self.reponame))
        solv_path = os.path.join(self.repo.root, ZYPP_SOLV_CACHE_PATH, self.channel_label or self.reponame, 'solv')
        if not os.path.isfile(solv_path) or not self.solv_repo.add_solv(solv.xfopen(str(solv_path)), 0):
            raise SolvFileNotFound(solv_path)
        self.solv_pool.addfileprovides()
        self.solv_pool.createwhatprovides()
        # Solvables with ":" in name are not packages
        return [pack for pack in self.solv_repo.solvables if ':' not in pack.name]

    def _get_solvable_dependencies(self, solvables):
        """
        Return a list containing all passed solvables and all its calculated dependencies.

        For each solvable we explore the "SOLVABLE_REQUIRES" to add any new solvable where "SOLVABLE_PROVIDES"
        is matching the requirement. All the new solvables that are added will be again processed in order to get
        a new level of dependencies.

        The exploration of dependencies is done when all the solvables are been processed and no new solvables are added

        :returns: list
        """
        if not self.repo.is_configured:
            self.setup_repo(self.repo)
        known_solvables = set()

        new_deps = True
        next_solvables = solvables

        # Collect solvables dependencies in depth
        while new_deps:
            new_deps = False
            for sol in next_solvables:
                # Do not explore dependencies from solvables that are already proceesed
                if sol not in known_solvables:
                    # This solvable has not been proceesed yet. We need to calculate its dependencies
                    known_solvables.add(sol)
                    new_deps = True
                    # Adding solvables that provide the dependencies
                    for _req in sol.lookup_deparray(keyname=solv.SOLVABLE_REQUIRES):
                        next_solvables.extend(self.solv_pool.whatprovides(_req))
        return list(known_solvables)

    def _apply_filters(self, pkglist, filters):
        """
        Return a list of packages where defined filters were applied.

        :returns: list
        """
        if not filters:
            # if there's no include/exclude filter on command line or in database
            for p in self.repo.includepkgs:
                filters.append(('+', [p]))
            for p in self.repo.exclude:
                filters.append(('-', [p]))

        if filters:
            pkglist = self._filter_packages(pkglist, filters)
            pkglist = self._get_solvable_dependencies(pkglist)

            # Do not pull in dependencies if there're explicitly excluded
            pkglist = self._filter_packages(pkglist, filters, True)
            self.num_excluded = self.num_packages - len(pkglist)

        return pkglist

    @staticmethod
    def _fix_encoding(text):
        if text is None:
            return None
        else:
            return str(text)

    @staticmethod
    def _filter_packages(packages, filters, exclude_only=False):
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
        if exclude_only or filters[0][0] == '-':
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
                if exclude_only:
                    continue
                # include
                for excluded_pkg in excluded:
                    if reobj.match(excluded_pkg.name):
                        allmatched_include.insert(0, excluded_pkg)
                        selected.insert(0, excluded_pkg)
                for pkg in allmatched_include:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == '-':
                # exclude
                for selected_pkg in selected:
                    if reobj.match(selected_pkg.name):
                        allmatched_exclude.insert(0, selected_pkg)
                        excluded.insert(0, selected_pkg)

                for pkg in allmatched_exclude:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = (excluded + allmatched_exclude)
            else:
                raise IOError("Filters are malformed")
        return selected

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
                p['vendor'] = self._fix_encoding(product.find('vendor').text)
                p['summary'] = self._fix_encoding(product.find('summary').text)
                p['description'] = self._fix_encoding(product.find('description').text)
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
        """
        Return a raw list of available packages.

        :returns: list
        """
        rawpkglist = [RawSolvablePackage(solvable) for solvable in self._get_solvable_packages()]
        return self._apply_filters(rawpkglist, filters)

    def list_packages(self, filters, latest):
        """
        List available packages.

        :returns: list
        """
        pkglist = self._get_solvable_packages()
        pkglist.sort(key = cmp_to_key(self._sort_packages))
        self.num_packages = len(pkglist)
        pkglist = self._apply_filters(pkglist, filters)

        if latest:
            latest_pkgs = {}
            new_pkgs = []
            for pkg in pkglist:
               ident = '{}.{}'.format(pkg.name, pkg.arch)
               if ident not in latest_pkgs.keys() or LooseVersion(str(pkg.evr)) > LooseVersion(str(latest_pkgs[ident].evr)):
                  latest_pkgs[ident] = pkg
            pkglist = list(latest_pkgs.values())

        to_return = []
        for pack in pkglist:
            new_pack = ContentPackage()
            epoch, version, release = RawSolvablePackage._parse_solvable_evr(pack.evr)
            new_pack.setNVREA(pack.name, version, release, epoch, pack.arch)
            new_pack.unique_id = RawSolvablePackage(pack)
            checksum = pack.lookup_checksum(solv.SOLVABLE_CHECKSUM)
            new_pack.checksum_type = checksum.typestr()
            new_pack.checksum = checksum.hex()
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

    def clear_cache(self, directory=None, keep_repomd=False):
        """
        Clear all cache files from the environment.

        """
        if directory is None:
            directory = self.repo.root

        # remove content in directory
        for item in os.listdir(directory):
            path = os.path.join(directory, item)
            if os.path.isfile(path) and not (keep_repomd and item == "repomd.xml"):
                os.unlink(path)
            elif os.path.isdir(path):
                rmtree(path)

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
            raise RepoMDError(self._get_repodata_path())
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
            except urlgrabber.URLGrabError:
                return
        finally:
            if os.path.exists(temp_file):
                os.unlink(temp_file)

    def set_ssl_options(self, ca_cert, client_cert, client_key):
        self.sslcacert = ca_cert
        self.sslclientcert = client_cert
        self.sslclientkey = client_key

    def _authenticate(self, url):
        pass
