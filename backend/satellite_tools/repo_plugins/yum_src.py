#
# Copyright (c) 2008--2011 Red Hat, Inc.
# Copyright (c) 2010--2011 SUSE LINUX Products GmbH, Nuernberg, Germany.
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

import shutil
import subprocess
import sys
import os
import re
import gzip
import xml.etree.ElementTree as etree

import urlgrabber
from urlgrabber.grabber import URLGrabber, URLGrabError, default_grabber
from rpmUtils.transaction import initReadOnlyTransaction
import yum
from spacewalk.common import fileutils
from yum.config import ConfigParser
from yum.update_md import UpdateMetadata, UpdateNoticeException, UpdateNotice
from yum.yumRepo import YumRepository

try:
    from yum.misc import cElementTree_iterparse as iterparse
except ImportError:
    try:
        from xml.etree import cElementTree
    except ImportError:
        # pylint: disable=F0401
        import cElementTree
    iterparse = cElementTree.iterparse
from spacewalk.satellite_tools.reposync import ChannelException, ChannelTimeoutException, ContentPackage
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common import rhnLog

# namespace prefix to parse patches.xml file
PATCHES = '{http://novell.com/package/metadata/suse/patches}'

CACHE_DIR   = '/var/cache/rhn/reposync/'
GPG_DIR     = '/var/lib/spacewalk/gpgdir'
YUMSRC_CONF = '/etc/rhn/spacewalk-repo-sync/yum.conf'

class YumWarnings:
    def __init__(self):
        self.saved_stdout = None
    def write(self, s):
        pass
    def disable(self):
        self.saved_stdout = sys.stdout
        sys.stdout = self
    def restore(self):
        sys.stdout = self.saved_stdout

class YumUpdateMetadata(UpdateMetadata):
    """The root update metadata object supports getting all updates"""

    def add(self, obj, mdtype='updateinfo', all=False):
        """ Parse a metadata from a given YumRepository, file, or filename. """
        if not obj:
            raise UpdateNoticeException
        if type(obj) in (type(''), type(u'')):
            infile = obj.endswith('.gz') and gzip.open(obj) or open(obj, 'rt')
        elif isinstance(obj, YumRepository):
            if obj.id not in self._repos:
                self._repos.append(obj.id)
                md = obj.retrieveMD(mdtype)
                if not md:
                    raise UpdateNoticeException()
                infile = gzip.open(md)
        else:   # obj is a file object
            infile = obj

        for _event, elem in iterparse(infile):
            if elem.tag == 'update':
                un = UpdateNotice(elem)
                key = un['update_id']
                if all:
                    key = "%s-%s" % (un['update_id'], un['version'])
                if not self._notices.has_key(key):
                    self._notices[key] = un
                    for pkg in un['pkglist']:
                        for pkgfile in pkg['packages']:
                            self._cache['%s-%s-%s' % (pkgfile['name'],
                                                      pkgfile['version'],
                                                      pkgfile['release'])] = un
                            no = self._no_cache.setdefault(pkgfile['name'], set())
                            no.add(un)

class ContentSource:
    def __init__(self, url, name, insecure=False, quiet=False, interactive=True):
        self.url = url
        self.name = name
        self.insecure = insecure
        self.quiet = quiet
        self.interactive = interactive
        self.yumbase = yum.YumBase()
        global YUMSRC_CONF
        if not os.path.exists(YUMSRC_CONF):
            YUMSRC_CONF = '/dev/null'
        try:
            self.yumbase.preconf.fn=YUMSRC_CONF
        except AttributeError: # older yum versions don't have the preconf attr
            self.yumbase.doConfigSetup(fn=YUMSRC_CONF)
        self.configparser = ConfigParser()
        self._clean_cache(CACHE_DIR + name)

        # read the proxy configuration in /etc/rhn/rhn.conf
        initCFG('server.satellite')
        self.proxy_addr = CFG.http_proxy
        self.proxy_user = CFG.http_proxy_username
        self.proxy_pass = CFG.http_proxy_password

        if (self.proxy_user is not None and
            self.proxy_pass is not None and
            self.proxy_addr is not None):
            self.proxy_url = "http://%s:%s@%s" % (
                self.proxy_user, self.proxy_pass, self.proxy_addr)
        elif self.proxy_addr is not None:
            self.proxy_url = "http://" + self.proxy_addr
        else:
            self.proxy_url = None

        repo = yum.yumRepo.YumRepository(name)
        repo.populate(self.configparser, name, self.yumbase.conf)
        self.repo = repo
        self.sack = None

        self.setup_repo(repo)
        self.num_packages = 0
        self.num_excluded = 0

    def setup_repo(self, repo):
        """Fetch repository metadata"""
        repo.cache = 0
        repo.metadata_expire = 0
        repo.mirrorlist = self.url
        repo.baseurl = [self.url]
        repo.basecachedir = CACHE_DIR
        if self.insecure:
            repo.repo_gpgcheck = False
        else:
            repo.repo_gpgcheck = True

        if hasattr(repo, 'base_persistdir'):
            repo.base_persistdir = CACHE_DIR
        pkgdir = os.path.join(CFG.MOUNT_POINT, CFG.PREPENDED_DIR, '1', 'stage')
        if not os.path.isdir(pkgdir):
            fileutils.makedirs(pkgdir, user='wwwrun', group='www')
        repo.pkgdir = pkgdir

        if self.proxy_url is not None:
            repo.proxy = self.proxy_url

        warnings = YumWarnings()
        warnings.disable()
        repo.baseurlSetup()
        warnings.restore()
        for burl in repo.baseurl:
            repo.gpgkey = [burl + '/repodata/repomd.xml.key']
        repo.setup(False, None, gpg_import_func=self.getKeyForRepo,
                   confirm_func=self.askImportKey)
        # use a fix dir for repo metadata sig checks
        repo.gpgdir = GPG_DIR
        self.initgpgdir( repo.gpgdir )
        self.sack = self.repo.getPackageSack()

    def list_packages(self, filters):
        """ list packages"""
        try:
            self.sack.populate(self.repo, 'metadata', None, 0)
        except yum.Errors.RepoError,e :
            if "No more mirrors" in str(e):
                reqFile = re.search('failure:\s+(.+)\s+from',
                                    str(e)).groups()[0]
                raise ChannelTimeoutException("Retrieving '%s' failed: File not found in repository '%s'" % (reqFile, self.repo))
            else:
                raise

        pkglist = self.sack.returnPackages()
        self.num_packages = len(pkglist)
        if filters:
            pkglist = self._filter_packages(pkglist, filters)
            pkglist = self._get_package_dependencies(self.sack, pkglist)
            self.num_excluded = self.num_packages - len(pkglist)
        to_return = []
        for pack in pkglist:
            if pack.arch == 'src':
                continue
            new_pack = ContentPackage()
            new_pack.setNVREA(pack.name, pack.version, pack.release,
                              pack.epoch, pack.arch)
            new_pack.unique_id = pack
            new_pack.predef_checksum_type = pack.checksums[0][0]
            if new_pack.predef_checksum_type == 'sha':
                new_pack.predef_checksum_type = 'sha1'
            new_pack.predef_checksum      = pack.checksums[0][1]
            for cs in pack.checksums:
                new_pack.checksums[cs[0]] = cs[1]
            to_return.append(new_pack)
        return to_return

    def _filter_packages(self, packages, filters):
        """ implement include / exclude logic
            filters are: [ ('+', includelist1), ('-', excludelist1),
                           ('+', includelist2), ... ]
        """
        if filters is None:
            return

        selected = []
        excluded = []
        if filters[0][0] == '-':
            # first filter is exclude, start with full package list
            # and then exclude from it
            selected = packages
        else:
            excluded = packages

        for filter_item in filters:
            sense, pkg_list = filter_item
            if sense == '+':
                # include
                exactmatch, matched, _unmatched = yum.packages.parsePackages(
                                                        excluded, pkg_list)
                allmatched = yum.misc.unique(exactmatch + matched)
                selected = yum.misc.unique(selected + allmatched)
                for pkg in allmatched:
                    if pkg in excluded:
                        excluded.remove(pkg)
            elif sense == '-':
                # exclude
                exactmatch, matched, _unmatched = yum.packages.parsePackages(
                                                        selected, pkg_list)
                allmatched = yum.misc.unique(exactmatch + matched)
                for pkg in allmatched:
                    if pkg in selected:
                        selected.remove(pkg)
                excluded = yum.misc.unique(excluded + allmatched)
            else:
                raise UpdateNoticeException
        return selected

    def _get_package_dependencies(self, sack, packages):
        self.yumbase.pkgSack = sack
        resolved_deps = self.yumbase.findDeps(packages)
        for (_pkg, deps) in resolved_deps.items():
            for (_dep, dep_packages) in deps.items():
                packages.extend(dep_packages)
        return yum.misc.unique(packages)

    def get_package(self, package):
        """ get package """
        check = (self.verify_pkg, (package.unique_id, 1), {})
        return self.repo.getPackage(package.unique_id, checkfunc=check)

    def verify_pkg(self, fo, pkg, fail):
        return pkg.verifyLocalPkg()

    def _clean_cache(self, directory):
        shutil.rmtree(directory, True)

    def get_products(self):
        products = []
        if 'products' in self.repo.repoXML.repoData:
            prod_path = self.repo.retrieveMD('products')
            for product in etree.parse(prod_path).getroot():
                p = {}
                p['name'] = product.find('name').text
                p['arch'] = product.find('arch').text
                version = product.find('version')
                p['version'] = version.get('ver')
                p['release'] = version.get('rel')
                p['epoch'] = version.get('epoch')
                p['vendor'] = product.find('vendor').text
                p['summary'] = product.find('summary').text
                p['description'] = product.find('description').text
		if p['epoch'] == '0':
                    p['epoch'] = None
                products.append(p)
        return products

    def get_updates(self):
        if 'updateinfo' in self.repo.repoXML.repoData:
            um = YumUpdateMetadata()
            try:
                um.add(self.repo, all=True)
            except yum.Errors.NoMoreMirrorsRepoError:
                raise ChannelTimeoutException("Retrieving updateinfo failed: File not found")
            return ('updateinfo', um.notices)

        elif 'patches' in self.repo.repoXML.repoData:
            patches_path = self.repo.retrieveMD('patches')
            os.mkdir(os.path.join(self.repo.cachedir, 'patches'))

            # parse the patches.xml file and download every patch-xxx.xml file
            notices = []
            for patch in etree.parse(patches_path).getroot():
                checksum_elem = patch.find(PATCHES+'checksum')
                location_elem = patch.find(PATCHES+'location')
                relative = location_elem.get('href')
                checksum_type = checksum_elem.get('type')
                checksum = checksum_elem.text
                filename = os.path.join(self.repo.cachedir, 'patches',
                                        os.path.basename(relative))
                try:
                    self.repo.grab.urlgrab(yum.misc.to_utf8(relative),
                                           filename,
                                           checkfunc=(self.patches_checksum_func,
                                                      (checksum_type, checksum),
                                                      {}))
                except URLGrabError, e:
                    self.error_msg("Failed to download %s. [Errno %i] %s" %
                                   (relative, e.errno, e.strerror))
                    continue

                try:
                    notices.append(etree.parse(filename).getroot())
                except SyntaxError, e:
                    self.error_msg("Could not parse %s. "
                                   "The file is not a valid XML document. %s" %
                                   (filename, e.msg))
                    continue

            return ('patches', notices)
        else:
            return ('', [])

    def patches_checksum_func(self, callback_obj, checksum_type, checksum):
        """Simple function to checksum patches for urlgrabber

        """
        import types
        # this is ugly code copy&pasted from yumRepo.YumRepository._checkMD
        if type(callback_obj) == types.InstanceType: # urlgrabber check
            filename = callback_obj.filename
        else:
            filename = callback_obj

        local_checksum = self.repo._checksum(checksum_type, filename)

        if local_checksum == checksum:
            return 1
        else:
            raise URLGrabError(-1, 'Metadata file does not match checksum')

    def getKeyForRepo(self, repo, callback=None):
        """
        Retrieve a key for a repository If needed, prompt for if the key should
        be imported using callback

        @param repo: Repository object to retrieve the key of.
        @param callback: Callback function to use for asking for verification
                          of a key. Takes a dictionary of key info.
        """
        keyurls = []
        if self.interactive:
            keyurls = repo.gpgkey
        key_installed = False
        for keyurl in keyurls:
            keys = self._retrievePublicKey(keyurl, repo)
            for info in keys:
                # Check if key is already installed
                if info['keyid'] in yum.misc.return_keyids_from_pubring(repo.gpgdir):
                    continue

                # Try installing/updating GPG key
                rc = False
                if callback:
                    rc = callback({'repo':repo, 'userid':info['userid'],
                                   'hexkeyid':info['hexkeyid'], 'keyurl':keyurl,
                                   'fingerprint':info['fingerprint'],
                                   'timestamp':info['timestamp']})


                if not rc:
                    raise ChannelException, "GPG key(0x%s '%s') for repo %s rejected" % (info['hexkeyid'],info['userid'],repo)

                # Import the key
                result = yum.misc.import_key_to_pubring(info['raw_key'], info['hexkeyid'], gpgdir=repo.gpgdir)
                if not result:
                    raise ChannelException, 'Key import failed'

                key_installed = True

        if not key_installed:
            raise ChannelException, 'The GPG keys listed for the "%s" repository are ' \
                                    'already installed but they are not correct.\n' \
                                    'Check that the correct key URLs are configured for ' \
                                    'this repository.' % (repo)


    def _retrievePublicKey(self, keyurl, repo=None):
        """
        Retrieve a key file
        @param keyurl: url to the key to retrieve
        Returns a list of dicts with all the keyinfo
        """
        key_installed = False

        # Go get the GPG key from the given URL
        try:
            url = yum.misc.to_utf8(keyurl)
            if repo is None:
                rawkey = urlgrabber.urlread(url, limit=9999)
            else:
                #  If we have a repo. use the proxy etc. configuration for it.
                # In theory we have a global proxy config. too, but meh...
                # external callers should just update.
                ug = URLGrabber(bandwidth = repo.bandwidth,
                                retry = repo.retries,
                                throttle = repo.throttle,
                                progress_obj = repo.callback,
                                proxies=repo.proxy_dict)
                ug.opts.user_agent = default_grabber.opts.user_agent
                rawkey = ug.urlread(url, text=repo.id + "/gpgkey")

        except urlgrabber.grabber.URLGrabError, e:
            raise ChannelException('GPG key retrieval failed: ' +
                                    yum.i18n.to_unicode(str(e)))
        # Parse the key
        keys_info = yum.misc.getgpgkeyinfo(rawkey, multiple=True)
        keys = []
        for keyinfo in keys_info:
            thiskey = {}
            for info in ('keyid', 'timestamp', 'userid',
                         'fingerprint', 'raw_key'):
                if not keyinfo.has_key(info):
                    raise ChannelException, \
                      'GPG key parsing failed: key does not have value %s' % info
                thiskey[info] = keyinfo[info]
            thiskey['keyid'] = str("%16x" % (thiskey['keyid'] & 0xffffffffffffffffL)).upper()
            thiskey['hexkeyid'] = yum.misc.keyIdToRPMVer(keyinfo['keyid']).upper()
            keys.append(thiskey)

        return keys

    def askImportKey(self, d ):
        if self.interactive:
          print 'Do you want to import the GPG key 0x%s "%s" from %s? [y/n]:' % (d['hexkeyid'],
              yum.i18n.to_unicode(d['userid']), d['keyurl'],)
          yn = sys.stdin.readline()
          yn = yn.strip()

          if yn in ['y', 'Y', 'j', 'J']:
            return True

        return False

    def initgpgdir(self, gpgdir):
        if not os.path.exists(gpgdir):
            # initially we trust all keys which are in the RPM DB.
            # If gpgdir does not exist, we create the keyring
            # with all keys from the RPM DB
            os.makedirs(gpgdir)
            ts = initReadOnlyTransaction("/")
            for hdr in ts.dbMatch('name', 'gpg-pubkey'):
                if hdr['description'] != "":
                    yum.misc.import_key_to_pubring(hdr['description'], hdr['version'], gpgdir=gpgdir)

    def error_msg(self, message):
        rhnLog.log_clean(0, message)
        if not self.quiet:
            sys.stderr.write(str(message) + "\n")
