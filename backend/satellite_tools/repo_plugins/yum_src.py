# -*- coding: utf-8 -*-
#
# Copyright (c) 2008--2011 Red Hat, Inc.
# Copyright (c) 2010--2011 SUSE Linux Products GmbH
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
import yum
import shutil
import subprocess
import sys
import os
import gzip
import xml.etree.ElementTree as etree

import urlgrabber
from urlgrabber.grabber import URLGrabber, URLGrabError, default_grabber
from rpmUtils.transaction import initReadOnlyTransaction

from yum import config, misc, Errors
from yum.i18n import to_unicode
from yum.update_md import UpdateMetadata, UpdateNoticeException, UpdateNotice
from yum.yumRepo import YumRepository
try:
    from yum.misc import cElementTree_iterparse as iterparse
except ImportError:
    try:
        from xml.etree import cElementTree
    except ImportError:
        import cElementTree
    iterparse = cElementTree.iterparse

from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.satellite_tools.reposync import ChannelException, ChannelTimeoutException, ContentPackage

class YumWarnings:
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

        for event, elem in iterparse(infile):
            if elem.tag == 'update':
                un = UpdateNotice(elem)
                key = un['update_id']
                if all:
                    key = "%s-%s" % (un['update_id'], un['version'])
                if not self._notices.has_key(key):
                    self._notices[key] = un
                    for pkg in un['pkglist']:
                        for file in pkg['packages']:
                            self._cache['%s-%s-%s' % (file['name'],
                                                      file['version'],
                                                      file['release'])] = un
                            no = self._no_cache.setdefault(file['name'], set())
                            no.add(un)

class ContentSource:
    url = None
    name = None
    repo = None
    cache_dir = '/var/cache/rhn/reposync/'
    def __init__(self, url, name, insecure=False, interactive=True):
        self.url = url
        self.name = name
        self.insecure = insecure
        self.interactive = interactive
        self._clean_cache(self.cache_dir + name)

        # read the proxy configuration in /etc/rhn/rhn.conf
        initCFG('server.satellite')
        self.proxy_addr = CFG.http_proxy
        self.proxy_user = CFG.http_proxy_username
        self.proxy_pass = CFG.http_proxy_password

        if (self.proxy_user is not None and self.proxy_pass is not None and self.proxy_addr is not None):
            self.proxy_url = "http://%s:%s@%s" %(self.proxy_user, self.proxy_pass, self.proxy_addr)
        elif (self.proxy_addr is not None):
            self.proxy_url = "http://%s" %(self.proxy_addr)
        else:
            self.proxy_url = None

    def list_packages(self):
        """ list packages"""
        repo = yum.yumRepo.YumRepository(self.name)
        self.repo = repo
        repo.cache = 0
        repo.metadata_expire = 0
        repo.mirrorlist = self.url
        repo.baseurl = [self.url]
        repo.basecachedir = self.cache_dir
        if self.insecure:
            repo.repo_gpgcheck = False
        else:
            repo.repo_gpgcheck = True
        if self.proxy_url is not None:
            repo.proxy = self.proxy_url

        warnings = YumWarnings()
        warnings.disable()
        repo.baseurlSetup()
        warnings.restore()
        for burl in repo.baseurl:
          repo.gpgkey = [burl + '/repodata/repomd.xml.key']
 
        repo.setup(False, None, gpg_import_func=self.getKeyForRepo, confirm_func=self.askImportKey)
        self.initgpgdir( repo.gpgdir )
        sack = repo.getPackageSack()

        try:
            sack.populate(repo, 'metadata', None, 0)
        except Errors.RepoError,e :
            if "No more mirrors" in str(e):
                raise ChannelTimeoutException('No more mirrors to try.')
            else:
                raise

        list = sack.returnPackages()
        to_return = []
        for pack in list:
            if pack.arch == 'src':
                continue
            new_pack = ContentPackage()
            new_pack.setNVREA(pack.name, pack.version, pack.release, 
                              pack.epoch, pack.arch)
            new_pack.unique_id = pack
            new_pack.checksum_type = pack.checksums[0][0]
            if new_pack.checksum_type == 'sha':
                new_pack.checksum_type = 'sha1'
            new_pack.checksum      = pack.checksums[0][1]
            to_return.append(new_pack)
        return to_return

    def get_package(self, package):
        """ get package """
        check = (self.verify_pkg, (package.unique_id ,1), {})
        return self.repo.getPackage(package.unique_id, checkfunc=check)

    def verify_pkg(self, fo, pkg, fail):
        return pkg.verifyLocalPkg()

    def _clean_cache(self, directory):
        shutil.rmtree(directory, True)

    def get_updates(self):
        if 'updateinfo' in self.repo.repoXML.repoData:
            um = YumUpdateMetadata()
            try:
                um.add(self.repo, all=True)
            except Errors.NoMoreMirrorsRepoError:
                raise ChannelTimeoutException('No more mirrors to try.')

            return ('updateinfo', um.notices)

        elif 'patches' in self.repo.repoXML.repoData:
            patches_path = self.repo.retrieveMD('patches')
            os.mkdir(os.path.join(self.repo.cachedir, 'patches'))

            # parse the patches.xml file and download every patch-xxx.xml file
            notices = []
            for patch in etree.parse(patches_path).getroot():
                (checksum_elem, location_elem) = patch
                relative = location_elem.get('href')
                checksum_type = checksum_elem.get('type')
                checksum = checksum_elem.text
                filename = os.path.join(self.repo.cachedir, 'patches',
                                        os.path.basename(relative))
                self.repo.grab.urlgrab(misc.to_utf8(relative),
                                       filename,
                                       checkfunc=(self.patches_checksum_func,
                                                  (checksum_type, checksum), {}))
                notices.append(etree.parse(filename).getroot())
            return ('patches', notices)

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
                if info['keyid'] in misc.return_keyids_from_pubring(repo.gpgdir):
                    #print('GPG key at %s (0x%s) is already imported' % (
                    #    keyurl, info['hexkeyid']))
                    continue

                # Try installing/updating GPG key
                #print('Importing GPG key 0x%s "%s" from %s' %
                #                     (info['hexkeyid'],
                #                     to_unicode(info['userid']),
                #                     keyurl.replace("file://","")))
                rc = False
                if callback:
                    rc = callback({'repo':repo, 'userid':info['userid'],
                                   'hexkeyid':info['hexkeyid'], 'keyurl':keyurl,
                                   'fingerprint':info['fingerprint'],
                                   'timestamp':info['timestamp']})


                if not rc:
                    #raise ChannelException, "Not installing key for repo %s" % repo
                    continue

                # Import the key
                result = misc.import_key_to_pubring(info['raw_key'], info['hexkeyid'], gpgdir=repo.gpgdir)
                if not result:
                    raise ChannelException, 'Key import failed'
                result = self.import_key_to_rpmdb(info['raw_key'], info['hexkeyid'], gpgdir=repo.gpgdir)
                if not result:
                    raise ChannelException, 'Key import failed'

                #print('Key imported successfully')
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

        #print( 'Retrieving GPG key from %s') % keyurl

        # Go get the GPG key from the given URL
        try:
            url = misc.to_utf8(keyurl)
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
                                    to_unicode(str(e)))
        # Parse the key
        keys_info = misc.getgpgkeyinfo(rawkey, multiple=True)
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
            thiskey['hexkeyid'] = misc.keyIdToRPMVer(keyinfo['keyid']).upper()
            keys.append(thiskey)

        return keys

    def askImportKey(self, d ):
        if self.interactive:
          print 'Do you want to import the GPG key 0x%s "%s" from %s? [y/n]:' % (d['hexkeyid'], to_unicode(d['userid']), d['keyurl'],)
          yn = sys.stdin.readline()
          yn = yn.strip()

          if yn in ['y', 'Y', 'j', 'J']:
            return True

        return False

    def initgpgdir(self, gpgdir):
      if not os.path.exists(gpgdir):
        os.makedirs(gpgdir)
 
      ts = initReadOnlyTransaction("/")
      for hdr in ts.dbMatch('name', 'gpg-pubkey'):
        if hdr['description'] != "":
          misc.import_key_to_pubring(hdr['description'], hdr['version'], gpgdir=gpgdir)

    def import_key_to_rpmdb(self, raw, keyid, gpgdir):
      if not os.path.exists(gpgdir):
        os.makedirs(gpgdir)
      tmpfile = os.path.join(gpgdir, keyid)
      fp = open(tmpfile, 'w')
      fp.write(raw)
      fp.close()
      cmd = ['/bin/rpm', '--import', tmpfile]
      p = subprocess.Popen(cmd)
      sts = os.waitpid(p.pid, 0)[1]
      os.remove(tmpfile)
      if sts == 0:
        return True
      return False
