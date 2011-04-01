# -*- coding: utf-8 -*-
#
# Copyright (c) 2008--2010 Red Hat, Inc.
# Copyright (c) 2010 SUSE LINUX Products GmbH, Nuernberg, Germany.
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
import sys, os, time, grp
import os.path
import hashlib
from datetime import datetime
import traceback
from optparse import OptionParser
from spacewalk.server import rhnPackage, rhnSQL, rhnChannel, rhnPackageUpload
from spacewalk.common import CFG, initCFG, rhnLog, fetchTraceback, rhnMail, rhn_rpm
from spacewalk.common.checksum import getFileChecksum
from spacewalk.common.rhn_mpm import InvalidPackageError
from spacewalk.server.importlib.importLib import IncompletePackage, Erratum, Checksum, Bug, Keyword
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription
from spacewalk.server.importlib.errataImport import ErrataImport
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server import taskomatic
from spacewalk.susemanager import suseLib
from yum import Errors
from yum.i18n import to_unicode, to_utf8

import socket
hostname = socket.gethostname()

default_log_location = '/var/log/rhn/reposync/'
default_hash = 'sha256'

class ChannelException(Exception):
    """
    Channel Error.
    """
    def __init__(self, value=None):
        Exception.__init__(self)
        self.value = value
    def __str__(self):
        return "%s" %(self.value,)

    def __unicode__(self):
        return '%s' % to_unicode(self.value)

class ChannelTimeoutException(ChannelException):
    """Channel timeout error e.g. a remote repository is not responding"""
    pass


class RepoSync:
    parser = None
    type = None
    urls = None
    channel_label = None
    channel = None
    fail = False
    quiet = False
    regen = False
    noninteractive = False

    def main(self):
        initCFG('server.satellite')
        db_string = CFG.DEFAULT_DB #"rhnsat/rhnsat@rhnsat"
        rhnSQL.initDB(db_string)
        (options, args) = self.process_args()

        log_filename = 'reposync.log'
        if options.channel_label:
            date = time.localtime()
            datestr = '%d.%02d.%02d-%02d:%02d:%02d' % (date.tm_year, date.tm_mon, date.tm_mday, date.tm_hour, date.tm_min, date.tm_sec)
            log_filename = options.channel_label + '-' +  datestr + '.log'

        rhnLog.initLOG(default_log_location + log_filename)
        #os.fchown isn't in 2.4 :/
        os.system("chgrp www " + default_log_location + log_filename)

        if options.type not in ["yum"]:
            print "Error: Unknown type %s" % options.type
            sys.exit(2)

        quit = False
        if not options.url:
            if options.channel_label:
                # TODO:need to look at user security across orgs
                h = rhnSQL.prepare("""select s.source_url, s.metadata_signed
                                      from rhnContentSource s,
                                           rhnChannelContentSource cs,
                                           rhnChannel c
                                     where s.id = cs.source_id
                                       and cs.channel_id = c.id
                                       and c.label = :label""")
                h.execute(label=options.channel_label)
                source_urls = h.fetchall_dict() or []
                if source_urls:
                    self.urls = source_urls
                else:
                    if options.channel_label:
                        # generate empty metadata
                        taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                            [options.channel_label], [], "server.app.yumreposync")
                        rhnSQL.commit()
                    quit = True
                    self.error_msg("Channel has no URL associated")
        else:
            self.urls = [{'source_url':options.url, 'metadata_signed' : 'N'}]
        if not options.channel_label:
            quit = True
            self.error_msg("--channel must be specified")

        self.log_msg("\nSync started: %s" % (time.asctime(time.localtime())))
        self.log_msg(str(sys.argv))


        if quit:
            sys.exit(1)

        self.type = options.type
        self.channel_label = options.channel_label
        self.fail = options.fail
        self.quiet = options.quiet
        self.channel = self.load_channel()
        self.noninteractive = options.noninteractive

        if not self.channel:
            print "Channel does not exist"
            sys.exit(1)

        for data in self.urls:
            url = suseLib.URL(data['source_url'])
            if url.get_query_param("credentials"):
                initCFG('server.susemanager')
                url.username = CFG.get("%s%s" % (url.get_query_param("credentials"), "_user"))
                url.password = CFG.get("%s%s" % (url.get_query_param("credentials"), "_pass"))
                initCFG('server.satellite')
            url.query = ""
            insecure = False;
            if data['metadata_signed'] == 'N':
                insecure = True;
            try:
                plugin = self.load_plugin()(url.getURL(), self.channel_label, insecure, (not self.noninteractive))
                self.import_packages(plugin, url.getURL())
                self.import_updates(plugin, url.getURL())
            except ChannelTimeoutException, e:
                self.print_msg("Repository server is not responding.")
                if taskomatic.schedule_single_sat_repo_sync(int(self.channel['id'])):
                    self.print_msg("Retriggered reposync.")
                else:
                    self.print_msg("Failed to retrigger reposync.")
                sys.exit(1)
            except ChannelException, e:
                self.print_msg("ChannelException: %s" % e)
                self.sendErrorMail(fetchTraceback())
                sys.exit(1)
            except Errors.YumGPGCheckError, e:
                self.print_msg("YumGPGCheckError: %s" % e)
                self.sendErrorMail(fetchTraceback())
                sys.exit(1)
            except Errors.RepoError, e:
                self.print_msg("RepoError: %s" % e)
                self.sendErrorMail(fetchTraceback())
                sys.exit(1)
            except Errors.RepoMDError, e:
                if "primary not available" in str(e):
                    self.print_msg("Repository has no packages. (%s)" % e)
                    sys.exit(0)
                else:
                    self.print_msg("RepoMDError: %s" % e)
                    self.sendErrorMail(fetchTraceback())
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

    def process_args(self):
        self.parser = OptionParser()
        self.parser.add_option('-u', '--url', action='store', dest='url', help='The url to sync')
        self.parser.add_option('-c', '--channel', action='store', dest='channel_label', help='The label of the channel to sync packages to')
        self.parser.add_option('-t', '--type', action='store', dest='type', help='The type of repo, currently only "yum" is supported', default='yum')
        self.parser.add_option('-f', '--fail', action='store_true', dest='fail', default=False , help="If a package import fails, fail the entire operation")
        self.parser.add_option('-q', '--quiet', action='store_true', dest='quiet', default=False, help="Print no output, still logs output")
        self.parser.add_option('-n', '--non-interactive', action='store_true', dest='noninteractive', default=False, help="Do not ask anything, use default answers automatically.")
        return self.parser.parse_args()

    def load_plugin(self):
        name = self.type + "_src"
        mod = __import__('spacewalk.satellite_tools.repo_plugins', globals(), locals(), [name])
        submod = getattr(mod, name)
        return getattr(submod, "ContentSource")

    def import_updates(self, plug, url):
        notices = plug.get_updates()
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        self.print_msg("Repo %s has %s patches." % (saveurl.getURL(),
                                                    len(notices)))
        if notices:
            self.upload_updates(notices)

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
        error = False
        e = Erratum()
        e['errata_from'] = notice['from']
        e['advisory'] = notice['update_id'] + "-" + notice['version'] + "-" + self.channel['arch']
        e['advisory_name'] = notice['update_id'] + "-" + notice['version'] + "-" + self.channel['arch']
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
        e['issue_date'] = self._to_db_date(notice['issued'])
        if notice['updated'] is not None:
          e['update_date'] = self._to_db_date(notice['updated'])
        else:
          e['update_date'] = e['issue_date']
        #e['last_modified'] = notice['']
        e['notes'] = ''
        e['org_id'] = self.channel['org_id']
        e['refers_to'] = ''
        e['channels'] = [{'label':self.channel_label}]
        e['packages'] = []
        e['files'] = []

        for pkg in notice['pkglist'][0]['packages']:
          param_dict = {
            'name'          : pkg['name'],
            'version'       : pkg['version'],
            'release'       : pkg['release'],
            'arch'          : pkg['arch'],
            'channel_label' : self.channel_label
            }
          if pkg['epoch'] is None or pkg['epoch'] == '' or pkg['epoch'] == '0':
            epochStatement = "(pevr.epoch is NULL or pevr.epoch = 0)"
          else:
            epochStatement = "pevr.epoch = :epoch"
            param_dict['epoch'] = pkg['epoch']

          if self.channel['org_id']:
            orgidStatement = " = :org_id"
            param_dict['org_id'] = self.channel['org_id']
          else:
            orgidStatement = " is NULL"

          h = rhnSQL.prepare("""select p.id, c.checksum, c.checksum_type, pevr.epoch
          from rhnPackage p,
          rhnPackagename pn,
          rhnpackageevr  pevr,
          rhnpackagearch pa,
          rhnChecksumView c,
          rhnChannel ch,
          rhnChannelPackage cp,
          rhnArchType rat
          where pn.name = :name
          and p.org_id %s
          and pevr.version = :version
          and pevr.release = :release
          and pa.label = :arch
          and %s
          and rat.label = 'rpm'
          and pa.arch_type_id = rat.id
          and p.checksum_id = c.id
          and p.name_id = pn.id
          and p.evr_id = pevr.id
          and p.package_arch_id = pa.id
          and p.id = cp.package_id
          and cp.channel_id = ch.id
          and ch.label = :channel_label
          """ % (orgidStatement, epochStatement))
          apply(h.execute, (), param_dict)
          cs = h.fetchone_dict() or None

          if not cs:
            if param_dict.has_key('epoch'):
              self.print_msg("No cheksum found for %s-%s:%s-%s.%s. Skipping Patch %s" % (param_dict['name'],
                                                                                         param_dict['epoch'],
                                                                                         param_dict['version'],
                                                                                         param_dict['release'],
                                                                                         param_dict['arch'],
                                                                                         e['advisory_name']))
            else:
              self.print_msg("No cheksum found for %s-%s-%s.%s. Skipping Patch %s" % (param_dict['name'],
                                                                                      param_dict['version'],
                                                                                      param_dict['release'],
                                                                                      param_dict['arch'],
                                                                                      e['advisory_name']))
            error = True
            break

          package = IncompletePackage()
          for k in pkg.keys():
            package[k] = pkg[k]
          package['epoch'] = cs['epoch']
          package['org_id'] = self.channel['org_id']

          package['checksums'] = {cs['checksum_type'] : cs['checksum']}
          package['checksum_type'] = cs['checksum_type']
          package['checksum'] = cs['checksum']

          package['package_id'] = cs['id']
          e['packages'].append(package)

        e['keywords'] = []
        if notice['reboot_suggested']:
          kw = Keyword()
          kw.populate({'keyword':'reboot_suggested'})
          e['keywords'].append(kw)
        if notice['restart_suggested']:
          kw = Keyword()
          kw.populate({'keyword':'restart_suggested'})
          e['keywords'].append(kw)
        e['bugs'] = []
        bzs = filter(lambda r: r['type'] == 'bugzilla', notice['references'])
        if len(bzs):
          tmp = {}
          for bz in bzs:
            if bz['id'] not in tmp:
              bug = Bug()
              bug.populate({'bug_id' : bz['id'], 'summary' : bz['title'], 'href' : bz['href']})
              e['bugs'].append(bug)
              tmp[bz['id']] = None
        e['cve'] = []
        cves = filter(lambda r: r['type'] == 'cve', notice['references'])
        if len(cves):
          tmp = {}
          for cve in cves:
            if cve['id'] not in tmp:
              e['cve'].append(cve['id'])
              tmp[cve['id']] = None
        e['locally_modified'] = None
        if not error:
          batch.append(e)

      backend = SQLBackend()
      importer = ErrataImport(batch, backend)
      importer.run()
      self.regen = True

    def import_packages(self, plug, url):
        packages = plug.list_packages()
        to_process = []
        skipped = 0
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        self.print_msg("Repo " + saveurl.getURL() + " has " + str(len(packages)) + " packages.")
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

            path, package_channel = rhnPackage.get_path_for_package(
                   [pack.name, pack.version, pack.release, pack.epoch, pack.arch],
                   self.channel_label)

            to_download = False
            to_link     = False
            if not path:
                # package is not on disk
                to_download = True
            else:
                # a different package is on disk
                pack.path = os.path.join(CFG.MOUNT_POINT, path)
                if not self.match_package_checksum(pack.path,
                                pack.checksum_type, pack.checksum):
                    to_download = True

            if package_channel != self.channel_label:
                # package is not in the channel
                to_link = True

            if to_download or to_link:
                to_process.append((pack, to_download, to_link))

        num_to_process = len(to_process)
        if num_to_process == 0:
            self.print_msg("No new packages to sync.")
            return

        self.regen=True
        is_non_local_repo = (url.find("file://") < 0)
        # try/except/finally doesn't work in python 2.4 (RHEL5), so here's a hack
        def finally_remove(path):
            if is_non_local_repo and path:
                os.remove(path)

        for (index, what) in enumerate(to_process):
            pack, to_download, to_link = what
            localpath = None
            try:
                self.print_msg("%d/%d : %s" % (index+1, num_to_process, pack.getNVREA()))
                if to_download:
                    pack.path = localpath = plug.get_package(pack)
                pack.load_checksum_from_header()
                if to_download:
                    self.upload_package(pack)
                    finally_remove(localpath)
                if to_link:
                    self.associate_package(pack)
            except KeyboardInterrupt:
                finally_remove(localpath)
                raise
            except Exception, e:
                self.error_msg(e)
                finally_remove(localpath)
                if self.fail:
                    raise
                continue

    def match_package_checksum(self, abspath, checksum_type, checksum):
        if (os.path.exists(abspath) and
            getFileChecksum(checksum_type, filename=abspath) == checksum):
            return 1
        return 0

    def upload_package(self, package):

        rel_package_path = rhnPackageUpload.relative_path_from_header(
                package.header, self.channel['org_id'],
                package.checksum_type, package.checksum)
        package_dict, diff_level = rhnPackageUpload.push_package(package.header,
                package.payload_stream, package.checksum_type, package.checksum,
                force=False,
                header_start=package.header_start, header_end=package.header_end,
                relative_path=rel_package_path,
                org_id=self.channel['org_id'])
        package.payload_stream.close()

    def associate_package(self, pack):
        caller = "server.app.yumreposync"
        backend = SQLBackend()
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
      ca = h.fetchall_dict() or []
      compatArchs = []
      for arch in ca:
        compatArchs.append(arch['label'])
      return compatArchs

    def best_checksum_item(self, checksums):
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

    def short_hash(self, str):
        return hashlib.new(default_hash, str).hexdigest()[0:8]

    def sendErrorMail(self, body):
        to = CFG.TRACEBACK_MAIL
        fr = to
        if isinstance(to, type([])):
            fr = string.strip(to[0])
            to = string.join(map(string.strip, to), ', ')
        headers = {
            "Subject" : "SUSE Manager repository sync failed (%s)" % hostname,
            "From"    : "%s <%s>" % (hostname, fr),
            "To"      : to,
        }
        extra = "Syncing Channel '%s' failed:\n\n" % self.channel_label
        rhnMail.send(headers, extra + body)

    def _to_db_date(self, date):
        ret = ""
        if date.isdigit():
          ret = datetime.fromtimestamp(float(date)).isoformat(' ')
        else:
          # we expect to get ISO formated date
          ret = date
        return ret

class ContentPackage:

    def __init__(self):
        # map of checksums
        self.checksum_type = None
        self.checksum = None

        #unique ID that can be used by plugin
        self.unique_id = None

        self.name = None
        self.version = None
        self.release = None
        self.epoch = None
        self.arch = None

        self.path = None
        self.file = None
        self.header = None
        self.payload_stream = None
        self.header_start = None
        self.header_end = None

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

    def load_checksum_from_header(self):
        if self.path is None:
           raise rhnFault(50, "Unable to load package", explain=0)
        self.file = open(self.path, 'rb')
        self.header, self.payload_stream, self.header_start, self.header_end = \
                rhnPackageUpload.load_package(self.file)
        self.checksum_type = self.header.checksum_type()
        self.checksum = getFileChecksum(self.checksum_type, file=self.file)
        self.file.close()
