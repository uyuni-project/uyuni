# -*- coding: utf-8 -*-
#
# Copyright (c) 2008--2010 Red Hat, Inc.
# Copyright (c) 2010-2011 SUSE LINUX Products GmbH, Nuernberg, Germany.
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
import hashlib
import os
import re
import socket
import sys
import time
import traceback
from datetime import datetime

from yum import Errors
from yum.i18n import to_unicode

from spacewalk.server import rhnPackage, rhnSQL, rhnChannel, rhnPackageUpload
from spacewalk.common.rhnTB import fetchTraceback
from spacewalk.common import rhnMail, rhnLog, suseLib
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.checksum import getFileChecksum
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.server.importlib.importLib import IncompletePackage, Erratum, Checksum, Bug, Keyword
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription
from spacewalk.server.importlib.errataImport import ErrataImport
from spacewalk.server import taskomatic
from spacewalk.server.rhnSQL.const import ORACLE, POSTGRESQL

HOSTNAME = socket.gethostname()
LOG_LOCATION = '/var/log/rhn/reposync/'

class ChannelException(Exception):
    """
    Channel Error.
    """
    def __init__(self, value=None):
        Exception.__init__(self)
        self.value = value

    def __str__(self):
        return "%s" % self.value

    def __unicode__(self):
        return '%s' % to_unicode(self.value)

class ChannelTimeoutException(ChannelException):
    """Channel timeout error e.g. a remote repository is not responding"""
    pass


class RepoSync:
    regen = False

    def __init__(self, channel_label, repo_type, url=None, fail=False,
                 quiet=False, noninteractive=False):
        self.fail = fail
        self.quiet = quiet
        self.interactive = not noninteractive

        initCFG('server.satellite')
        db_string = CFG.DEFAULT_DB #"rhnsat/rhnsat@rhnsat"
        rhnSQL.initDB(db_string)

        # setup logging
        log_filename = 'reposync.log'
        date = time.localtime()
        datestr = '%d.%02d.%02d-%02d:%02d:%02d' % (
            date.tm_year, date.tm_mon, date.tm_mday, date.tm_hour,
            date.tm_min, date.tm_sec)
        log_filename = channel_label + '-' +  datestr + '.log'
        rhnLog.initLOG(LOG_LOCATION + log_filename)
        #os.fchown isn't in 2.4 :/
        os.system("chgrp www " + LOG_LOCATION + log_filename)

        self.log_msg("\nSync started: %s" % (time.asctime(time.localtime())))
        self.log_msg(str(sys.argv))

        if not url:
            # TODO:need to look at user security across orgs
            h = rhnSQL.prepare("""select s.source_url, s.metadata_signed
                                  from rhnContentSource s,
                                       rhnChannelContentSource cs,
                                       rhnChannel c
                                 where s.id = cs.source_id
                                   and cs.channel_id = c.id
                                   and c.label = :label""")
            h.execute(label=channel_label)
            source_urls = h.fetchall_dict()
            if source_urls:
                self.urls = source_urls
            else:
                # generate empty metadata and quit
                taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                    [channel_label], [], "server.app.yumreposync")
                rhnSQL.commit()
                self.error_msg("Channel has no URL associated")
                sys.exit(1)
        else:
            self.urls = [{'source_url': url, 'metadata_signed' : 'N'}]

        self.repo_plugin = self.load_plugin(repo_type)
        self.channel_label = channel_label

        self.channel = self.load_channel()
        if not self.channel:
            self.print_msg("Channel does not exist.")
            sys.exit(1)

    def load_plugin(self, repo_type):
        """Try to import the repository plugin required to sync the repository

        :repo_type: type of the repository; only 'yum' is currently supported

        """
        name = repo_type + "_src"
        mod = __import__('spacewalk.satellite_tools.repo_plugins',
                         globals(), locals(), [name])
        try:
            submod = getattr(mod, name)
        except AttributeError:
            self.error_msg("Repository type %s is not supported. "
                           "Could not import "
                           "spacewalk.satellite_tools.repo_plugins.%s."
                           % (repo_type, name))
            sys.exit(1)
        return getattr(submod, "ContentSource")

    def sync(self):
        """Trigger a reposync"""
        for data in self.urls:
            url = suseLib.URL(data['source_url'])
            if url.get_query_param("credentials"):
                initCFG('server.susemanager')
                url.username = CFG.get("%s%s" % (url.get_query_param("credentials"), "_user"))
                url.password = CFG.get("%s%s" % (url.get_query_param("credentials"), "_pass"))
                initCFG('server.satellite')
            url.query = ""
            insecure = False
            if data['metadata_signed'] == 'N':
                insecure = True
            try:
                repo = self.repo_plugin(url.getURL(), self.channel_label,
                                        insecure, self.quiet, self.interactive,
                                        proxy=CFG.HTTP_PROXY,
                                        proxy_user=CFG.HTTP_PROXY_USERNAME,
                                        proxy_pass=CFG.HTTP_PROXY_PASSWORD)
                self.import_packages(repo, url.getURL())
                self.import_updates(repo, url.getURL())
            except ChannelTimeoutException, e:
                self.print_msg(e)
                self.sendErrorMail(str(e))
                sys.exit(1)
            except ChannelException, e:
                self.print_msg("ChannelException: %s" % e)
                self.sendErrorMail("ChannelException: %s" % str(e))
                sys.exit(1)
            except Errors.YumGPGCheckError, e:
                self.print_msg("YumGPGCheckError: %s" % e)
                self.sendErrorMail("YumGPGCheckError: %s" % e)
                sys.exit(1)
            except Errors.RepoError, e:
                self.print_msg("RepoError: %s" % e)
                self.sendErrorMail("RepoError: %s" % e)
                sys.exit(1)
            except Errors.RepoMDError, e:
                if "primary not available" in str(e):
                    self.print_msg("Repository has no packages. (%s)" % e)
                    sys.exit(0)
                else:
                    self.print_msg("RepoMDError: %s" % e)
                    self.sendErrorMail("RepoMDError: %s" % e)
                sys.exit(1)
            except:
                self.print_msg("Unexpected error: %s" % sys.exc_info()[0])
                self.print_msg("%s" % traceback.format_exc())
                self.sendErrorMail(fetchTraceback())
                sys.exit(1)

        if self.regen:
            taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                [self.channel_label], [], "server.app.yumreposync")
            taskomatic.add_to_erratacache_queue(self.channel_label)
        self.update_date()
        rhnSQL.commit()
        self.print_msg("Sync complete")


    def update_date(self):
        """ Updates the last sync time"""
        h = rhnSQL.prepare( """update rhnChannel set LAST_SYNCED = current_timestamp
                             where label = :channel""")
        h.execute(channel=self.channel['label'])


    def import_updates(self, plug, url):
        (notices_type, notices) = plug.get_updates()
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        self.print_msg("Repo %s has %s patches." % (saveurl.getURL(),
                                                    len(notices)))
        if notices_type == 'updateinfo' and notices:
            self.upload_updates(notices)
        elif notices_type == 'patches' and notices:
            self.upload_patches(notices)

    def upload_patches(self, notices):
        """Insert the information from patches into the database

        :arg notices: a list of ElementTree roots from individual patch files

        """
        prefix = {'yum': "{http://linux.duke.edu/metadata/common}",
                  'rpm': "{http://linux.duke.edu/metadata/rpm}",
                  'suse': "{http://novell.com/package/metadata/suse/common}",
                  'patch': "{http://novell.com/package/metadata/suse/patch}"
                  }

        typemap = {'security'    : 'Security Advisory',
                   'recommended' : 'Bug Fix Advisory',
                   'bugfix'      : 'Bug Fix Advisory',
                   'optional'    : 'Product Enhancement Advisory',
                   'feature'     : 'Product Enhancement Advisory',
                   'enhancement' : 'Product Enhancement Advisory'
                   }
        batch = []
        for notice in notices:
            error = False
            e = Erratum()

            version = notice.find('%sversion' % prefix['yum']).get('ver')
            category = notice.find('%scategory' % prefix['patch']).text

            e['errata_from'] = 'maint-coord@suse.de'
            e['advisory'] = e['advisory_name'] = '-'.join([notice.get('patchid'),
                                                           self.channel['arch']])
            e['advisory_rel'] = version
            try:
                e['advisory_type'] = typemap[category]
            except KeyError:
                e['advisory_type'] = 'Product Enhancement Advisory'

            existing_errata = self.get_errata(e['advisory'])

            # product name
            query = rhnSQL.prepare("""
                SELECT p.friendly_name
                FROM rhnchannel c, suseproductchannel pc, suseproducts p
                WHERE pc.channel_id = c.id
                  AND pc.product_id = p.id
                  AND c.label = :label
                """)
            query.execute(label=self.channel_label)
            try:
                e['product'] = query.fetchone()[0]
            except TypeError:
                e['product'] = 'unknown product'

            for desc_lang in notice.findall('%sdescription' % prefix['patch']):
                if desc_lang.get('lang') == 'en':
                    e['description'] = desc_lang.text or ""
                    break
            for sum_lang in notice.findall('%ssummary' % prefix['patch']):
                if sum_lang.get('lang') == 'en':
                    e['synopsis'] = sum_lang.text or ""
                    break
            e['topic'] = ' '
            e['solution'] = ' '
            e['issue_date'] = _to_db_date(notice.get('timestamp'))
            e['update_date'] = e['issue_date']
            e['notes'] = ''
            e['org_id'] = self.channel['org_id']
            e['refers_to'] = ''
            e['channels'] = [{'label': self.channel_label}]
            if existing_errata:
                e['channels'].extend(existing_errata['channels'])
            e['packages'] = []
            e['files'] = []

            atoms = notice.find('%satoms' % prefix['patch'])
            packages = atoms.findall('%spackage' % prefix['yum'])

            e['packages'] = self._patches_process_packages(packages, e['advisory_name'], prefix)
            # an update can't have zero packages, so we skip this update
            if not e['packages']:
                continue

            e['keywords'] = []
            if notice.find('%sreboot-needed' % prefix['patch']) is not None:
                kw = Keyword()
                kw.populate({'keyword': 'reboot_suggested'})
                e['keywords'].append(kw)
            if notice.find('%spackage-manager' % prefix['patch']) is not None:
                kw = Keyword()
                kw.populate({'keyword': 'restart_suggested'})
                e['keywords'].append(kw)

            e['bugs'] = find_bugs(e['description'])
            e['cve'] = find_cves(e['description'])

            e['locally_modified'] = None
            batch.append(e)
        backend = SQLBackend()
        backend.init()
        importer = ErrataImport(batch, backend)
        importer.run()
        self.regen = True

    def upload_updates(self, notices):
        batch = []
        typemap = {
                  'security'    : 'Security Advisory',
                  'recommended' : 'Bug Fix Advisory',
                  'bugfix'      : 'Bug Fix Advisory',
                  'optional'    : 'Product Enhancement Advisory',
                  'feature'     : 'Product Enhancement Advisory',
                  'enhancement' : 'Product Enhancement Advisory'
                  }
        for notice in notices:
            e = Erratum()
            e['errata_from'] = notice['from']
            e['advisory'] = e['advisory_name'] = '-'.join([notice['update_id'],
                                                           notice['version'],
                                                           self.channel['arch']])
            existing_errata = self.get_errata(e['advisory'])

            e['advisory_rel'] = notice['version']
            if notice['type'] in typemap:
                e['advisory_type'] = typemap[notice['type']]
            else:
                e['advisory_type'] = 'Product Enhancement Advisory'
            e['product'] =  notice['release']
            e['description'] = notice['description']
            e['synopsis'] = notice['title']
            e['topic'] = ' '
            e['solution'] = ' '
            e['issue_date'] = _to_db_date(notice['issued'])
            if notice['updated']:
                e['update_date'] = _to_db_date(notice['updated'])
            else:
                e['update_date'] = e['issue_date']
            #e['last_modified'] = notice['']
            e['notes'] = ''
            e['org_id'] = self.channel['org_id']
            e['refers_to'] = ''
            e['channels'] = [{'label':self.channel_label}]
            if existing_errata:
                e['channels'].extend(existing_errata['channels'])
            e['files'] = []

            e['packages'] = self._updates_process_packages(
                notice['pkglist'][0]['packages'], e['advisory_name'])
            # an update can't have zero packages, so we skip this update
            if not e['packages']:
                continue

            e['keywords'] = _update_keywords(notice)
            e['bugs'] = _update_bugs(notice)
            e['cve'] = _update_cve(notice)
            if notice['severity']:
                # 'severity' not available in older yum versions
                # do nothing if this key does not exist
                e['security_impact'] = notice['severity']
            e['locally_modified'] = None
            batch.append(e)

        backend = SQLBackend()
        backend.init()
        importer = ErrataImport(batch, backend)
        importer.run()
        self.regen = True

    def _updates_process_packages(self, packages, advisory_name):
        """Check if the packages are in the database

        Go through the list of 'packages' and for each of them
        check to see if it is already present in the database. If it is,
        return a list of IncompletePackage objects, otherwise return an
        empty list.

        :packages: a list of dicts that represent packages (updateinfo style)
        :advisory_name: the name of the current erratum

        """
        erratum_packages = []
        for pkg in packages:
            param_dict = {
                'name': pkg['name'],
                'version': pkg['version'],
                'release': pkg['release'],
                'arch': pkg['arch'],
                'epoch': pkg['epoch'],
                'channel_label': self.channel_label}
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                # This package could not be found in the database
                # so we skip the broken patch.
                return []
            erratum_packages.append(ret)
        return erratum_packages

    def _patches_process_packages(self, packages, advisory_name, prefix):
        """Check if the packages are in the database

        Go through the list of 'packages' and for each of them
        check to see if it is already present in the database. If it is,
        return a list of IncompletePackage objects, otherwise return an
        empty list.

        :packages: a list of dicts that represent packages (patch style)
        :advisory_name: the name of the current erratum
        :prefix xml namespace dict

        """
        erratum_packages = []
        for pkg in packages:
            nevr = pkg.find(
                '%sformat' % prefix['yum']).find(
                '%srequires' % prefix['rpm']).find(
                '%sentry' % prefix['rpm'])
            param_dict = {
                'name': nevr.get('name'),
                'version': nevr.get('ver'),
                'release': nevr.get('rel'),
                'epoch': nevr.get('epoch'),
                'arch': pkg.find('%sarch' % prefix['yum']).text,
                'channel_label': self.channel_label
            }
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                # This package could not be found in the database
                # so we skip the broken patch.
                return []
            erratum_packages.append(ret)
        return erratum_packages


    def _process_package(self, param_dict, advisory_name):
        """Search for a package in the the database

        Search for the package specified by 'param_dict' to see if it is
        already present in the database. If it is, return a
        IncompletePackage objects, otherwise return None.

        :param_dict: dict that represent packages (nerva + channel_label)
        :advisory_name: the name of the current erratum

        """
        pkgepoch = param_dict['epoch']
        del param_dict['epoch']

        if not pkgepoch or pkgepoch == '0':
            epochStatement = "(pevr.epoch is NULL or pevr.epoch = 0)"
        else:
            epochStatement = "pevr.epoch = :epoch"
            param_dict['epoch'] = pkgepoch
        if self.channel['org_id']:
            orgidStatement = " = :org_id"
            param_dict['org_id'] = self.channel['org_id']
        else:
            orgidStatement = " is NULL"

        h = rhnSQL.prepare("""
            select p.id, c.checksum, c.checksum_type, pevr.epoch
              from rhnPackage p
              join rhnPackagename pn on p.name_id = pn.id
              join rhnpackageevr pevr on p.evr_id = pevr.id
              join rhnpackagearch pa on p.package_arch_id = pa.id
              join rhnArchType at on pa.arch_type_id = at.id
              join rhnChecksumView c on p.checksum_id = c.id
              join rhnChannelPackage cp on p.id = cp.package_id
              join rhnChannel ch on cp.channel_id = ch.id
             where pn.name = :name
               and p.org_id %s
               and pevr.version = :version
               and pevr.release = :release
               and pa.label = :arch
               and %s
               and at.label = 'rpm'
               and ch.label = :channel_label
            """ % (orgidStatement, epochStatement))
        h.execute(**param_dict)
        cs = h.fetchone_dict()

        if not cs:
            # package could not be found in the database.
            if 'epoch' not in param_dict:
                param_dict['epoch'] = ''
            else:
                param_dict['epoch'] = '-%s' % param_dict['epoch']
            self.print_msg(
                        "The package "
                        "%(name)s%(epoch)s:%(version)s-%(release)s.%(arch)s "
                        "which is referenced by patch %(patch)s was not found "
                        "in the database. This patch has been skipped." % dict(
                            patch=advisory_name,
                            **param_dict))
            return None

        package = IncompletePackage()
        for k in param_dict:
            if k not in ['epoch', 'channel_label']:
                package[k] = param_dict[k]
        package['epoch'] = cs['epoch']
        package['org_id'] = self.channel['org_id']

        package['checksums'] = {cs['checksum_type'] : cs['checksum']}
        package['checksum_type'] = cs['checksum_type']
        package['checksum'] = cs['checksum']

        package['package_id'] = cs['id']
        return package

    def import_packages(self, repo, url):
        packages = repo.list_packages()
        to_link = []
        to_download = []
        skipped = 0
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        self.print_msg("Repo %s has %s packages." %
                       (saveurl.getURL(), len(packages)))
        compatArchs = self.compatiblePackageArchs()

        for pack in packages:
            if pack.arch in ['src', 'nosrc']:
                # skip source packages
                skipped += 1
                continue
            if pack.arch not in compatArchs:
                # skip packages with incompatible architecture
                skipped += 1
                continue

            # we have a few scenarios here:
            # 1.  package is not on the server (link and download)
            # 2.  package is in the server, but not in the channel (just link if we can)
            # 3.  package is in the server and channel, but not on the file system (just download)
            path = rhnPackage.get_path_for_package(
                [pack.name, pack.version, pack.release, pack.epoch, pack.arch],
                self.channel_label)
            if not path:
                path = rhnPackage.get_path_for_package(
                    [pack.name, pack.version, pack.release, '', pack.arch],
                    self.channel_label)
            if path:
                if os.path.exists(os.path.join(CFG.MOUNT_POINT, path)):
                    continue
                else:
                    to_download.append(pack)
                    continue
            else:
                # the package is not in the rhnPackage table, so we have
                # to link it
                to_link.append(pack)

                # try to find the package by checksum, otherwise download it
                if not self._find_by_checksum(pack):
                    to_download.append(pack)

        if skipped > 0:
            self.print_msg("Skip '%s' incompatible packages." % skipped)

        if to_download:
            self.regen = True
        else:
            self.print_msg("No new packages to download.")

        self._download_packages(to_download, repo, url)
        self._link_packages(to_link)

    def upload_package(self, package, path):
        temp_file = open(path, 'rb')
        header, payload_stream, header_start, header_end = \
                rhnPackageUpload.load_package(temp_file)
        #
        # Getting checksum_type from RPM header does not work.
        # There is always the default 'md5' returned.
        # But with this we will not find package by checksum, if we do not
        # have the checksum from the metadata in the DB.
        # so let's create the best checksum_type we have in the metadata
        #
        #package.checksum_type = header.checksum_type()
        (package.checksum_type, cs_type_orig, md_checksum) = _best_checksum_item(package.checksums)
        package.checksum = getFileChecksum(package.checksum_type, file=temp_file)
        #
        # perform an additional check, if the checksums matches
        #
        if md_checksum and package.checksum != md_checksum:
            raise Exception("Checksum missmatch")

        rel_package_path = rhnPackageUpload.relative_path_from_header(
                header, self.channel['org_id'],
                package.checksum_type, package.checksum)
        package_path = os.path.join(CFG.MOUNT_POINT,
                rel_package_path)
        package_dict, diff_level = rhnPackageUpload.push_package(header,
                payload_stream, package.checksum_type, package.checksum,
                force=False,
                header_start=header_start, header_end=header_end,
                relative_path=rel_package_path,
                org_id=self.channel['org_id'])
        temp_file.close()

    def associate_package(self, pack):
        caller = "server.app.yumreposync"
        backend = SQLBackend()
        backend.init()

        package = {}
        package['name'] = pack.name
        package['version'] = pack.version
        package['release'] = pack.release
        package['epoch'] = pack.epoch
        package['arch'] = pack.arch
        package['checksum'] = pack.checksum
        package['checksum_type'] = pack.checksum_type
        package['channels']  = [{'label':self.channel_label,
                                 'id':self.channel['id']}]
        package['org_id'] = self.channel['org_id']
        try:
            self._importer_run(package, caller, backend)
        except:
            package['epoch'] = ''
            self._importer_run(package, caller, backend)

        backend.commit()

    def _importer_run(self, package, caller, backend):
            importer = ChannelPackageSubscription(
                       [IncompletePackage().populate(package)],
                       backend, caller=caller, repogen=False)
            importer.run()

    def load_channel(self):
        return rhnChannel.channel_info(self.channel_label)

    def compatiblePackageArchs(self):
        h = rhnSQL.prepare("""select pa.label
                              from rhnChannelPackageArchCompat cpac,
                              rhnChannel c,
                              rhnpackagearch pa
                              where c.id = :channel_id
                              and c.channel_arch_id = cpac.channel_arch_id
                              and cpac.package_arch_id = pa.id""")
        h.execute(channel_id=self.channel['id'])
        ca = h.fetchall_dict()
        compatArchs = []
        for arch in ca:
            compatArchs.append(arch['label'])
        return compatArchs

    def _link_packages(self, packages):
        """Create a record in the database for each package on our filesystem"""
        for pack in packages:
            try:
                (pack.checksum_type, cs_type_orig, pack.checksum) = _best_checksum_item(pack.checksums)
                self.associate_package(pack)
            except KeyboardInterrupt:
                raise
            except Exception, e:
                self.error_msg(e)
                if self.fail:
                    raise
                continue

    def _download_packages(self, packages, repo, repo_url):
        """Download packages

        :packages: a list of ContentPackage objects that we need to download
        :repo: repository (ContentSource) from which to download the packages
        :url: url of the repository

        """
        is_non_local_repo = (repo_url.find("file://") < 0)

        for (index, pack) in enumerate(packages):
            path = None # we might not reach the path assignment in 'try'
            # try/except/finally doesn't work in python 2.4 (RHEL5)
            try:
                try:
                    self.print_msg("%s/%s : %s" % (
                            index+1, len(packages), pack.getNVREA()))
                    path = repo.get_package(pack)
                    self.upload_package(pack, path)
                except KeyboardInterrupt:
                    raise
                except Exception, e:
                   self.error_msg(e)
                   if self.fail:
                       raise
                   continue
            finally:
                if is_non_local_repo and path and os.path.exists(path):
                    os.remove(path)

    def _find_by_checksum(self, pack):
        # we know that it's not in the channel, lets try to
        # check the server by checksum!
        # for some repos (sha256), we can check to see if we have them by
        # checksum and not bother downloading.  For older repos, we only
        # have sha1, which satellite doesn't track
        for (checksum_type, checksum) in pack.checksums.items():
            if checksum_type == 'sha': #we use sha1 (instead of sha)
                checksum_type = 'sha1'
            path = rhnPackage.get_path_for_checksum(
                self.channel['org_id'], checksum_type, checksum)
            if path and os.path.exists(os.path.join(CFG.MOUNT_POINT, path)):
                return True
            else:
                return False

    def print_msg(self, message):
        rhnLog.log_clean(0, message)
        if not self.quiet:
            print message

    def error_msg(self, message):
        rhnLog.log_clean(0, message)
        if not self.quiet:
            sys.stderr.write(str(message) + "\n")

    def log_msg(self, message):
        rhnLog.log_clean(0, message)

    def sendErrorMail(self, body):
        to = CFG.TRACEBACK_MAIL
        fr = to
        if isinstance(to, type([])):
            fr = to[0].strip()
            to = ', '.join([s.strip() for s in to])

        headers = {
            "Subject" : "SUSE Manager repository sync failed (%s)" % HOSTNAME,
            "From"    : "%s <%s>" % (HOSTNAME, fr),
            "To"      : to,
        }
        extra = "Syncing Channel '%s' failed:\n\n" % self.channel_label
        rhnMail.send(headers, extra + body)

    def get_errata(self, update_id):
        """Fetch an Errata dict from the database

        Search in the database for the given advisory and return a dict
        with important values.  If the advisory was not found it returns
        None.

        :update_id - the advisory (name)

        """
        h = rhnSQL.prepare("""
            select e.id, e.advisory,
                   e.advisory_name, e.advisory_rel
              from rhnerrata e
             where e.advisory = :name
        """)
        h.execute(name=update_id)
        ret = h.fetchone_dict() or None
        if not ret:
            return None

        h = rhnSQL.prepare("""
            select distinct c.label
              from rhnchannelerrata ce
              join rhnchannel c on c.id = ce.channel_id
             where ce.errata_id = :eid
        """)
        h.execute(eid=ret['id'])
        ret['channels'] = h.fetchall_dict() or []

        return ret


def _best_checksum_item(checksums):
    if checksums.has_key('sha256'):
        checksum_type = 'sha256'
        checksum_type_orig = 'sha256'
        checksum = checksums[checksum_type_orig]
    elif checksums.has_key('sha'):
        checksum_type = 'sha1'
        checksum_type_orig = 'sha'
        checksum = checksums[checksum_type_orig]
    elif checksums.has_key('sha1'):
        checksum_type = 'sha1'
        checksum_type_orig = 'sha1'
        checksum = checksums[checksum_type_orig]
    elif checksums.has_key('md5'):
        checksum_type = 'md5'
        checksum_type_orig = 'md5'
        checksum = checksums[checksum_type_orig]
    else:
        checksum_type = 'md5'
        checksum_type_orig = None
        checksum = None
    return (checksum_type, checksum_type_orig, checksum)

def _to_db_date(date):
    if date.isdigit():
        ret = datetime.fromtimestamp(float(date)).isoformat(' ')
    else:
        # we expect to get ISO formated date
        ret = date
    return ret

def _update_keywords(notice):
    """Return a list of Keyword objects for the notice"""
    keywords = []
    if notice['reboot_suggested']:
        kw = Keyword()
        kw.populate({'keyword':'reboot_suggested'})
        keywords.append(kw)
    if notice['restart_suggested']:
        kw = Keyword()
        kw.populate({'keyword':'restart_suggested'})
        keywords.append(kw)
    return keywords

def _update_bugs(notice):
    """Return a list of Bug objects from the notice's references"""
    bugs = {}
    for bz in notice['references']:
        if bz['type'] == 'bugzilla' and bz['id'] not in bugs:
            bug = Bug()
            bug.populate({'bug_id': bz['id'],
                          'summary': bz['title'],
                          'href': bz['href']})
            bugs[bz['id']] = bug
    return bugs.values()

def _update_cve(notice):
    """Return a list of unique ids from notice references of type 'cve'"""
    cves = [cve['id'] for cve in notice['references'] if cve['type'] == 'cve']
    # remove duplicates
    cves = list(set(cves))

    return cves

class ContentPackage:

    def __init__(self):
        # map of checksums
        self.checksums = {}
        self.checksum_type = None
        self.checksum = None

        #unique ID that can be used by plugin
        self.unique_id = None

        self.name = None
        self.version = None
        self.release = None
        self.epoch = None
        self.arch = None

    def setNVREA(self, name, version, release, epoch, arch):
        self.name = name
        self.version = version
        self.release = release
        self.arch = arch
        self.epoch = epoch

    def getNVREA(self):
        if self.epoch:
            return self.name + '-' + self.version + '-' + self.release + '-' + self.epoch + '.' + self.arch
        else:
            return self.name + '-' + self.version + '-' + self.release + '.' + self.arch

def find_bugs(text):
    """Find and return a list of Bug objects from the bug ids in the `text`

    Matches:
     - [#123123], (#123123)

    N.B. We assume that all the bugs are Novell Bugzilla bugs.

    """
    bug_numbers = set(re.findall('[\[\(]#(\d{6})[\]\)]', text))
    bugs = []
    for bug_number in bug_numbers:
        bug = Bug()
        bug.populate(
            {'bug_id': bug_number,
             'summary': 'bug number %s' % bug_number,
             'href':
                 'https://bugzilla.novell.com/show_bug.cgi?id=%s' % bug_number})
        bugs.append(bug)
    return bugs

def find_cves(text):
    """Find and return a list of CVE ids

    Matches:
     - CVE-YEAR-NUMB

    """
    return list(set(re.findall('CVE-\d{4}-\d{4}', text)))
