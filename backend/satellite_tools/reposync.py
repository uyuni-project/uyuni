#
# Copyright (c) 2008--2016 Red Hat, Inc.
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
import os
import re
import shutil
import socket
import sys
import time
import traceback
import base64
from datetime import datetime
from HTMLParser import HTMLParser
from dateutil.parser import parse as parse_date
from dateutil.tz import tzutc

from yum import Errors
from yum.i18n import to_unicode

from spacewalk.server import rhnPackage, rhnSQL, rhnChannel, suseEula
from spacewalk.common import fileutils, rhnLog, rhnCache, rhnMail, suseLib
from spacewalk.common.rhnTB import fetchTraceback
from spacewalk.common.rhnLib import isSUSE
from spacewalk.common.checksum import getFileChecksum
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.server.importlib.importLib import IncompletePackage, Erratum, Bug, Keyword
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.errataImport import ErrataImport
from spacewalk.server import taskomatic
from spacewalk.satellite_tools.repo_plugins import ThreadedDownloader, ProgressBarLogger, TextLogger
from spacewalk.satellite_tools.satCerts import verify_certificate_dates

from syncLib import log, log2, log2disk

hostname = socket.gethostname()
if '.' not in hostname:
    hostname = socket.getfqdn()


default_log_location = '/var/log/rhn/'
relative_comps_dir = 'rhn/comps'
checksum_cache_filename = 'reposync/checksum_cache'

# namespace prefixes for parsing SUSE patches XML files
YUM = "{http://linux.duke.edu/metadata/common}"
RPM = "{http://linux.duke.edu/metadata/rpm}"
SUSE = "{http://novell.com/package/metadata/suse/common}"
PATCH = "{http://novell.com/package/metadata/suse/patch}"

class ChannelException(Exception):
    """Channel Error"""
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

class KSDirParser:
    file_blacklist = ["release-notes/"]

    def __init__(self, dir_html, additional_blacklist=None):
        self.dir_content = []

        if additional_blacklist is None:
            additional_blacklist = []
        elif not isinstance(additional_blacklist, type([])):
            additional_blacklist = [additional_blacklist]

        for s in (m.group(1) for m in re.finditer(r'(?i)<a href="(.+?)"', dir_html)):
            if not (re.match(r'/', s) or re.search(r'\?', s) or re.search(r'\.\.', s) or re.match(r'[a-zA-Z]+:', s)):
                if re.search(r'/$', s):
                    file_type = 'DIR'
                else:
                    file_type = 'FILE'

                if s not in (self.file_blacklist + additional_blacklist):
                    self.dir_content.append({'name': s, 'type': file_type})

    def get_content(self):
        return self.dir_content

class TreeInfoError(Exception):
    pass


class TreeInfoParser(object):
    def __init__(self, filename):
        self.parser = ConfigParser.RawConfigParser()
        # do not lowercase
        self.parser.optionxform = str
        fp = open(filename)
        try:
            try:
                self.parser.readfp(fp)
            except ConfigParser.ParsingError:
                raise TreeInfoError("Could not parse treeinfo file!")
        finally:
            if fp is not None:
                fp.close()

    def get_images(self):
        files = []
        for section_name in self.parser.sections():
            if section_name.startswith('images-') or section_name == 'stage2':
                for item in self.parser.items(section_name):
                    files.append(item[1])
        return files

    def get_family(self):
        for section_name in self.parser.sections():
            if section_name == 'general':
                for item in self.parser.items(section_name):
                    if item[0] == 'family':
                        return item[1]

    def get_major_version(self):
        for section_name in self.parser.sections():
            if section_name == 'general':
                for item in self.parser.items(section_name):
                    if item[0] == 'version':
                        return item[1].split('.')[0]

    def get_package_dir(self):
        for section_name in self.parser.sections():
            if section_name == 'general':
                for item in self.parser.items(section_name):
                    if item[0] == 'packagedir':
                        return item[1]



def set_filter_opt(option, opt_str, value, parser):
    # pylint: disable=W0613
    if opt_str in ['--include', '-i']:
        f_type = '+'
    else:
        f_type = '-'
    parser.values.filters.append((f_type, re.split(r'[,\s]+', value)))


def getChannelRepo():

    initCFG('server.satellite')
    rhnSQL.initDB()
    items = {}
    sql = """
           select s.source_url, c.label
             from rhnChannel c
        left join rhnChannelContentSource cs on cs.channel_id=c.id
        left join rhnContentSource s on s.id = cs.source_id
            where s.source_url is not null
               or c.org_id is null
           """
    h = rhnSQL.prepare(sql)
    h.execute()
    while 1:
        row = h.fetchone_dict()
        if not row:
            break
        if not row['label'] in items:
            items[row['label']] = []
        if row['source_url']:
            items[row['label']] += [row['source_url']]

    return items


def getParentsChilds(b_only_custom=False):

    initCFG('server.satellite')
    rhnSQL.initDB()

    sql = """
        select c1.label, c2.label parent_channel, c1.id
        from rhnChannel c1 left outer join rhnChannel c2 on c1.parent_channel = c2.id
        order by c2.label desc, c1.label asc
    """
    h = rhnSQL.prepare(sql)
    h.execute()
    d_parents = {}
    while 1:
        row = h.fetchone_dict()
        if not row:
            break
        if not b_only_custom or rhnChannel.isCustomChannel(row['id']):
            parent_channel = row['parent_channel']
            if not parent_channel:
                d_parents[row['label']] = []
            else:
                # If the parent is not a custom channel treat the child like
                # it's a parent for our purposes
                if parent_channel not in d_parents:
                    d_parents[row['label']] = []
                else:
                    d_parents[parent_channel].append(row['label'])

    return d_parents


def getCustomChannels():

    # with SUSE we sync also Vendor channels with reposync
    # change parameter to False to get not only Custom Channels
    d_parents = getParentsChilds(False)
    l_custom_ch = []

    for ch in d_parents:
        l_custom_ch += [ch] + d_parents[ch]

    return l_custom_ch


def get_single_ssl_set(keys, check_dates=False):
    """Picks one of available SSL sets for given repository."""
    if check_dates:
        for ssl_set in keys:
            if verify_certificate_dates(str(ssl_set['ca_cert'])) and \
                (not ssl_set['client_cert'] or
                 verify_certificate_dates(str(ssl_set['client_cert']))):
                return ssl_set
    # Get first
    else:
        return keys[0]
    return None


class RepoSync(object):

    def __init__(self, channel_label, repo_type, url=None, fail=False,
                 filters=None, no_errata=False, sync_kickstart=False, latest=False,
                 metadata_only=False, strict=0, excluded_urls=None, no_packages=False,
                 log_dir="reposync", log_level=None, force_kickstart=False, force_all_errata=False,
                 check_ssl_dates=False, noninteractive=False, deep_verify=False):
        self.regen = False
        self.fail = fail
        self.filters = filters or []
        self.no_packages = no_packages
        self.no_errata = no_errata
        self.sync_kickstart = sync_kickstart
        self.force_all_errata = force_all_errata
        self.force_kickstart = force_kickstart
        self.latest = latest
        self.metadata_only = metadata_only
        self.ks_tree_type = 'externally-managed'
        self.interactive = not noninteractive
        self.deep_verify = deep_verify
        self.error_messages = []
        self.available_packages = {}
        self.ks_install_type = None

        initCFG('server.susemanager')
        rhnSQL.initDB()

        # setup logging
        log_filename = channel_label + '.log'
        log_path = default_log_location + log_dir + '/' + log_filename
        if log_level is None:
            log_level = 0
        CFG.set('DEBUG', log_level)
        rhnLog.initLOG(log_path, log_level)
        # os.fchown isn't in 2.4 :/
        if isSUSE():
            os.system("chgrp www " + log_path)
        else:
            os.system("chgrp apache " + log_path)

        log2disk(0, "Command: %s" % str(sys.argv))
        log2disk(0, "Sync of channel started.")

        self.channel_label = channel_label
        self.channel = self.load_channel()
        if not self.channel:
            log(0, "Channel %s does not exist." % channel_label)
            sys.exit(1)

        if self.channel['org_id']:
            self.channel['org_id'] = int(self.channel['org_id'])
        else:
            self.channel['org_id'] = None

        if not url:
            # TODO:need to look at user security across orgs
            h = rhnSQL.prepare(
                """
                select s.id, s.source_url, s.metadata_signed, s.label
                from rhnContentSource s,
                     rhnChannelContentSource cs
                where s.id = cs.source_id
                  and cs.channel_id = :channel_id""")
            h.execute(channel_id=int(self.channel['id']))
            sources = h.fetchall_dict()
            self.urls = []
            if excluded_urls is None:
                excluded_urls = []
            if sources:
                self.urls = self._format_sources(sources, excluded_urls)
            else:
                # generate empty metadata and quit
                taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                    [channel_label], [], "server.app.yumreposync")
                rhnSQL.commit()
                log2(0, 0, "Channel has no URL associated", stream=sys.stderr)
                if not self.channel['org_id']:
                    # RES base vendor channels do not have a URL. This is not an error
                    sys.exit(0)
                sys.exit(1)
        else:
            self.urls = [{'id': None, 'source_url': url, 'metadata_signed' : 'N', 'label': None}]

        if not self.urls:
            log2(0, 0, "Channel %s has no URL associated" % channel_label, stream=sys.stderr)

        self.repo_plugin = self.load_plugin(repo_type)
        self.strict = strict
        self.all_packages = []
        self.check_ssl_dates = check_ssl_dates
        # Init cache for computed checksums to not compute it on each reposync run again
        #self.checksum_cache = rhnCache.get(checksum_cache_filename)
        #if self.checksum_cache is None:
        #    self.checksum_cache = {}
        self.arches = self.get_compatible_arches(int(self.channel['id']))

    def set_urls_prefix(self, prefix):
        """If there are relative urls in DB, set their real location in runtime"""
        for index, url in enumerate(self.urls):
            # Make list, add prefix, make tuple and save
            url = list(url)
            url[1] = "%s%s" % (prefix, url[1])
            url = tuple(url)
            self.urls[index] = url

    def sync(self, update_repodata=True):
        """Trigger a reposync"""
        failed_packages = 0
        sync_error = 0
        if not self.urls:
            sync_error = -1
        start_time = datetime.now()
        for data in self.urls:
            data['source_url'] = self.set_repo_credentials(data)
            insecure = False
            if data['metadata_signed'] == 'N':
                insecure = True
            plugin = None

            for url in data['source_url']:
                # If the repository uses a uln:// URL, switch to the ULN plugin, overriding the command-line
                if url.startswith("uln://"):
                    self.repo_plugin = self.load_plugin("uln")

                # pylint: disable=W0703
                try:
                    if data['label']:
                        repo_name = data['label']
                    else:
                        # use modified relative_url as name of repo plugin, because
                        # it used as name of cache directory as well
                        relative_url = '_'.join(url.split('://')[1].split('/')[1:])
                        repo_name = relative_url.replace("?", "_").replace("&", "_").replace("=", "_")

                    plugin = self.repo_plugin(url, repo_name, insecure, self.interactive,
                                              org=str(self.channel['org_id'] or ''),
                                              channel_label=self.channel_label)

                    if update_repodata:
                        plugin.clear_cache()

                    if data['id'] is not None:
                        keys = rhnSQL.fetchone_dict("""
                        select k1.key as ca_cert, k2.key as client_cert, k3.key as client_key
                        from rhncontentsource cs inner join
                             rhncontentsourcessl csssl on cs.id = csssl.content_source_id inner join
                             rhncryptokey k1 on csssl.ssl_ca_cert_id = k1.id left outer join
                             rhncryptokey k2 on csssl.ssl_client_cert_id = k2.id left outer join
                             rhncryptokey k3 on csssl.ssl_client_key_id = k3.id
                        where cs.id = :repo_id
                        """, repo_id=int(data['id']))
                        if keys:
                            ssl_set = get_single_ssl_set(keys, check_dates=self.check_ssl_dates)
                            if ssl_set:
                                plugin.set_ssl_options(ssl_set['ca_cert'], ssl_set['client_cert'], ssl_set['client_key'])
                            else:
                                raise ValueError("No valid SSL certificates were found for repository.")

                    # update the checksum type of channels with org_id NULL
                    self.updateChannelChecksumType(plugin.get_md_checksum_type())

                    if not self.no_packages:
                        ret = self.import_packages(plugin, data['id'], url)
                        failed_packages += ret
                        self.import_groups(plugin, url)

                    if not self.no_errata:
                        self.import_updates(plugin, url)

                    # only for repos obtained from the DB
                    if self.sync_kickstart and data['label']:
                        try:
                            self.import_kickstart(plugin, url, data['label'])
                        except:
                            rhnSQL.rollback()
                            raise
                    self.import_products(plugin)
                    self.import_susedata(plugin)

                except ChannelTimeoutException, e:
                    log(0, e)
                    self.sendErrorMail(str(e))
                    sync_error = -1
                except ChannelException, e:
                    log(0, "ChannelException: %s" % e)
                    self.sendErrorMail("ChannelException: %s" % str(e))
                    sync_error = -1
                except Errors.YumGPGCheckError, e:
                    log(0, "YumGPGCheckError: %s" % e)
                    self.sendErrorMail("YumGPGCheckError: %s" % e)
                    sync_error = -1
                except Errors.RepoError, e:
                    log(0, "RepoError: %s" % e)
                    self.sendErrorMail("RepoError: %s" % e)
                    sync_error = -1
                except Errors.RepoMDError, e:
                    if "primary not available" in str(e):
                        taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                            [self.channel_label], [], "server.app.yumreposync")
                        rhnSQL.commit()
                        log(0, "Repository has no packages. (%s)" % e)
                        sync_error = -1
                    else:
                        log(0, "RepoMDError: %s" % e)
                        self.sendErrorMail("RepoMDError: %s" % e)
                        sync_error = -1
                except:
                    log(0, "Unexpected error: %s" % sys.exc_info()[0])
                    log(0, "%s" % traceback.format_exc())
                    self.sendErrorMail(fetchTraceback())
                    sync_error = -1

                if plugin is not None:
                    plugin.clear_ssl_cache()
        # Update cache with package checksums
        #rhnCache.set(checksum_cache_filename, self.checksum_cache)
        if self.regen:
            taskomatic.add_to_repodata_queue_for_channel_package_subscription(
                [self.channel_label], [], "server.app.yumreposync")
            taskomatic.add_to_erratacache_queue(self.channel_label)
        self.update_date()
        rhnSQL.commit()

        # update permissions
        fileutils.createPath(os.path.join(CFG.MOUNT_POINT, 'rhn'))  # if the directory exists update ownership only
        for root, dirs, files in os.walk(os.path.join(CFG.MOUNT_POINT, 'rhn')):
            for d in dirs:
                fileutils.setPermsPath(os.path.join(root, d), group='www')
            for f in files:
                fileutils.setPermsPath(os.path.join(root, f), group='www')
        elapsed_time = datetime.now() - start_time
        if self.error_messages:
            self.sendErrorMail("Repo Sync Errors: %s" % '\n'.join(self.error_messages))
            sync_error = -1
        if sync_error == 0 and failed_packages == 0:
            log(0, "Sync completed.")
        # if there is no global problems, but some packages weren't synced
        if sync_error == 0 and failed_packages > 0:
            sync_error = failed_packages
        return elapsed_time, sync_error

    def set_ks_tree_type(self, tree_type='externally-managed'):
        self.ks_tree_type = tree_type

    def set_ks_install_type(self, install_type='generic_rpm'):
        self.ks_install_type = install_type

    def update_date(self):
        """ Updates the last sync time"""
        h = rhnSQL.prepare("""update rhnChannel set LAST_SYNCED = current_timestamp
                             where label = :channel""")
        h.execute(channel=self.channel['label'])

    def load_plugin(self, repo_type):
        """Try to import the repository plugin required to sync the repository

        :repo_type: type of the repository; only 'yum' is currently supported

        """
        name = repo_type + "_src"
        mod = __import__('spacewalk.satellite_tools.repo_plugins', globals(), locals(), [name])
        try:
            submod = getattr(mod, name)
        except AttributeError:
            log2(0, 0, "Repository type %s is not supported. "
                       "Could not import "
                       "spacewalk.satellite_tools.repo_plugins.%s."
                       % (repo_type, name), stream=sys.stderr)
            sys.exit(1)
        return getattr(submod, "ContentSource")

    def import_updates(self, plug, url):
        (notices_type, notices) = plug.get_updates()
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        log(0, "Repo %s has %s patches." % (saveurl.getURL(),
                                                    len(notices)))
        if notices:
            if notices_type == 'updateinfo':
                self.upload_updates(notices)
            elif notices_type == 'patches':
                self.upload_patches(notices)

    def import_groups(self, plug, url):
        groupsfile = plug.get_groups()
        if groupsfile:
            basename = os.path.basename(groupsfile)
            log(0, "Repo %s has comps file %s." % (url, basename))
            relativedir = os.path.join(relative_comps_dir, self.channel_label)
            absdir = os.path.join(CFG.MOUNT_POINT, relativedir)
            if not os.path.exists(absdir):
                os.makedirs(absdir)
            relativepath = os.path.join(relativedir, basename)
            abspath = os.path.join(absdir, basename)
            for suffix in ['.gz', '.bz', '.xz']:
                if basename.endswith(suffix):
                    abspath = abspath.rstrip(suffix)
                    relativepath = relativepath.rstrip(suffix)
            src = fileutils.decompress_open(groupsfile)
            dst = open(abspath, "w")
            shutil.copyfileobj(src, dst)
            dst.close()
            src.close()
            # update or insert
            hu = rhnSQL.prepare("""update rhnChannelComps
                                      set relative_filename = :relpath,
                                          modified = current_timestamp
                                    where channel_id = :cid""")
            hu.execute(cid=self.channel['id'], relpath=relativepath)

            hi = rhnSQL.prepare("""insert into rhnChannelComps
                                  (id, channel_id, relative_filename)
                                  (select sequence_nextval('rhn_channelcomps_id_seq'),
                                          :cid,
                                          :relpath
                                     from dual91cf1508a829dbbc07b23753d3e7e8d46a275ae4
                                    where not exists (select 1 from rhnChannelComps
                                                       where channel_id = :cid))""")
            hi.execute(cid=self.channel['id'], relpath=relativepath)

    def upload_updates(self, notices):
        batch = []
        typemap = {
            'security': 'Security Advisory',
            'recommended': 'Bug Fix Advisory',
            'bugfix': 'Bug Fix Advisory',
            'optional': 'Product Enhancement Advisory',
            'feature': 'Product Enhancement Advisory',
            'enhancement': 'Product Enhancement Advisory'
        }
        backend = SQLBackend()
        channel_advisory_names = self.list_errata()
        for notice in notices:
            notice = self.fix_notice(notice)

            if not self.force_all_errata and notice['update_id'] in channel_advisory_names:
                continue

            patch_name = self._patch_naming(notice)
            existing_errata = self.get_errata(patch_name)
            if existing_errata and not self._is_old_suse_style(notice):
                if int(existing_errata['advisory_rel']) < int(notice['version']):
                    # A disaster happens
                    #
                    # re-releasing an errata with a higher release number
                    # only happens in case of a disaster.
                    # This should force mirrored repos to remove the old
                    # errata and take care that the new one is the only
                    # available.
                    # This mean a hard overwrite
                    self._delete_invalid_errata(existing_errata['id'])
                elif int(existing_errata['advisory_rel']) > int(notice['version']):
                    # the existing errata has a higher release than the now
                    # parsed one. We need to skip the current errata
                    continue
                # else: release match, so we update the errata


            if notice['updated']:
                updated_date = self._to_db_date(notice['updated'])
            else:
                updated_date = self._to_db_date(notice['issued'])
            if (existing_errata and
                not self.errata_needs_update(existing_errata, notice['version'], updated_date)):
                continue

            log(0, "Add Patch %s" % patch_name)
            e = Erratum()
            e['errata_from']   = notice['from']
            e['advisory'] = e['advisory_name'] = patch_name
            e['advisory_rel']  = notice['version']
            e['advisory_type'] = typemap.get(notice['type'], 'Product Enhancement Advisory')
            e['product']       = notice['release'] or 'Unknown'
            e['description']   = notice['description'] or 'not set'
            e['synopsis']      = notice['title'] or notice['update_id']
            if (notice['type'] == 'security' and notice['severity'] and
                not e['synopsis'].startswith(notice['severity'] + ': ')):
                e['synopsis'] = notice['severity'] + ': ' + e['synopsis']
            e['topic']         = ' '
            e['solution']      = ' '
            e['issue_date']    = self._to_db_date(notice['issued'])
            e['update_date']   = updated_date
            e['org_id']        = self.channel['org_id']
            e['notes']         = ''
            e['refers_to']     = ''
            e['channels']      = [{'label':self.channel_label}]
            e['packages']      = []
            e['files']         = []
            if existing_errata:
                e['channels'].extend(existing_errata['channels'])
                e['packages'] = existing_errata['packages']

            e['packages'] = self._updates_process_packages(
                notice['pkglist'][0]['packages'], e['advisory_name'], e['packages'])
            # One or more package references could not be found in the Database.
            # To not provide incomplete patches we skip this update
            if not e['packages']:
                # FIXME: print only with higher debug option
                log(2, "Advisory %s has empty package list." % e['advisory_name'])

            e['keywords'] = self._update_keywords(notice)
            e['bugs'] = self._update_bugs(notice)
            e['cve'] = self._update_cve(notice)
            if notice['severity']:
                e['security_impact'] = notice['severity']
            else:
                # 'severity' not available in older yum versions
                # set default to Low to get a correct currency rating
                e['security_impact'] = "Low"
            e['locally_modified'] = None
            batch.append(e)
            if self.deep_verify:
                # import step by step
                importer = ErrataImport(batch, backend)
                importer.run()
                batch = []

        if batch:
            log(0, "Syncing %s new errata to channel." % len(batch))
            importer = ErrataImport(batch, backend)
            importer.run()
            self.regen = True
        elif notices:
            log(0, "No new errata to sync.")

    def import_packages(self, plug, source_id, url):
        failed_packages = 0
        if (not self.filters) and source_id:
            h = rhnSQL.prepare("""
                    select flag, filter
                      from rhnContentSourceFilter
                     where source_id = :source_id
                     order by sort_order """)
            h.execute(source_id=source_id)
            filter_data = h.fetchall_dict() or []
            filters = [(row['flag'], re.split(r'[,\s]+', row['filter']))
                       for row in filter_data]
        else:
            filters = self.filters

        skipped = 0
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"

        packages = plug.list_packages(filters, self.latest)
        self.all_packages.extend(packages)
        to_process = []
        num_passed = len(packages)
        log(0, "Repo URL: %s" % saveurl.getURL())
        log(0, "Packages in repo:             %5d" % plug.num_packages)
        if plug.num_excluded:
            log(0, "Packages passed filter rules: %5d" % num_passed)
        channel_id = int(self.channel['id'])

        for pack in packages:
            if pack.arch in ['src', 'nosrc']:
                # skip source packages
                skipped += 1
                continue
            if pack.arch not in self.arches:
                # skip packages with incompatible architecture
                skipped += 1
                continue
            epoch = ''
            if pack.epoch and pack.epoch != '0':
                epoch = "%s:" % pack.epoch
            ident = "%s-%s%s-%s.%s" % (pack.name, epoch, pack.version, pack.release, pack.arch)
            self.available_packages[ident] = 1

            db_pack = rhnPackage.get_info_for_package(
                [pack.name, pack.version, pack.release, pack.epoch, pack.arch],
                channel_id, self.channel['org_id'])

            to_download = True
            to_link = True
            # Package exists in DB
            if db_pack:
                # Path in filesystem is defined
                if db_pack['path']:
                    pack.path = os.path.join(CFG.MOUNT_POINT, db_pack['path'])
                else:
                    pack.path = ""

                # if the package exists, but under a different org_id we have to download it again
                if self.metadata_only or self.match_package_checksum(pack, db_pack):
                    # package is already on disk or not required
                    to_download = False
                    if db_pack['channel_id'] == channel_id:
                        # package is already in the channel
                        to_link = False

                    # just pass data from DB, they will be used if there is no RPM available
                    pack.set_checksum(db_pack['checksum_type'], db_pack['checksum'])
                    pack.epoch = db_pack['epoch']

                elif db_pack['channel_id'] == channel_id:
                    # different package with SAME NVREA
                    self.disassociate_package(db_pack)
            else:
                # fix epoch
                if pack.epoch == '0':
                    pack.epoch = ''

            if to_download or to_link:
                to_process.append((pack, to_download, to_link))

        num_to_process = len(to_process)
        if num_to_process == 0:
            log(0, "No new packages to sync.")
            if plug.num_packages == 0:
                self.regen = True
            # If we are just appending, we can exit
            if not self.strict:
                return failed_packages
        else:
            log(0, "Packages already synced:      %5d" % (num_passed - num_to_process))
            log(0, "Packages to sync:             %5d" % num_to_process)

        is_non_local_repo = (url.find("file:/") < 0)

        downloader = ThreadedDownloader()
        to_download_count = 0
        for what in to_process:
            pack, to_download, to_link = what
            if to_download:
                target_file = os.path.join(plug.repo.pkgdir, os.path.basename(pack.unique_id.relativepath))
                pack.path = target_file
                params = {}
                if self.metadata_only:
                    bytes_range = (0, pack.unique_id.hdrend)
                    checksum_type = None
                    checksum = None
                else:
                    bytes_range = None
                    checksum_type = pack.checksum_type
                    checksum = pack.checksum
                plug.set_download_parameters(params, pack.unique_id.relativepath, target_file,
                                             checksum_type=checksum_type, checksum_value=checksum,
                                             bytes_range=bytes_range)
                downloader.add(params)
                to_download_count += 1
        if num_to_process != 0:
            log(0, "New packages to download:     %5d" % to_download_count)
        logger = TextLogger(None, to_download_count)
        downloader.set_log_obj(logger)
        downloader.run()

        log2disk(0, "Importing packages started.")
        progress_bar = ProgressBarLogger("Importing packages:    ", to_download_count)
        for (index, what) in enumerate(to_process):
            pack, to_download, to_link = what
            if not to_download:
                continue
            localpath = pack.path
            # pylint: disable=W0703
            try:
                if os.path.exists(localpath):
                    pack.load_checksum_from_header()
                    rel_package_path = pack.upload_package(self.channel, metadata_only=self.metadata_only)
                    # Save uploaded package to cache with repository checksum type
                    #if rel_package_path:
                    #    self.checksum_cache[rel_package_path] = {pack.checksum_type: pack.checksum}

                    # we do not want to keep a whole 'a_pkg' object for every package in memory,
                    # because we need only checksum. see BZ 1397417
                    pack.checksum = pack.a_pkg.checksum
                    pack.checksum_type = pack.a_pkg.checksum_type
                    pack.epoch = pack.a_pkg.header['epoch']
                    pack.a_pkg = None
                else:
                    raise Exception
                progress_bar.log(True, None)
            except KeyboardInterrupt:
                raise
            except Exception:
                failed_packages += 1
                e = sys.exc_info()[1]
                log2(0, 0, e, stream=sys.stderr)
                if self.fail:
                    raise
                to_process[index] = (pack, False, False)
                self.all_packages.remove(pack)
                progress_bar.log(False, None)
            finally:
                if is_non_local_repo and localpath and os.path.exists(localpath):
                    os.remove(localpath)
            pack.clear_header()
        log2disk(0, "Importing packages finished.")

        if self.strict:
            # Need to make sure all packages from all repositories are associated with channel
            import_batch = [self.associate_package(pack)
                            for pack in self.all_packages]
        else:
            # Only packages from current repository are appended to channel
            import_batch = [self.associate_package(pack)
                            for (pack, to_download, to_link) in to_process
                            if to_link]
        # Do not re-link if nothing was marked to link
        if any([to_link for (pack, to_download, to_link) in to_process]):
            log(0, "Linking packages to channel.")
            backend = SQLBackend()
            caller = "server.app.yumreposync"
            importer = ChannelPackageSubscription(import_batch,
                                                  backend, caller=caller, repogen=False,
                                                  strict=self.strict)
            importer.run()
            backend.commit()
            self.regen = True
        self._normalize_orphan_vendor_packages()
        return failed_packages

    def _normalize_orphan_vendor_packages(self):
        # Sometimes reposync disassociates vendor packages (org_id = 0) from
        # channels.
        # These orphans are then hard to work with in spacewalk (nobody has
        # permissions to view/delete them). We workaround this issue by
        # assigning such packages to the default organization, so that they can
        # be deleted using the existing orphan-deleting procedure.
        h = rhnSQL.prepare("""
            UPDATE rhnPackage
            SET org_id = 1
            WHERE id IN (SELECT p.id
                         FROM rhnPackage p LEFT JOIN rhnChannelPackage cp
                         ON p.id = cp.package_id
                         WHERE p.org_id IS NULL and cp.channel_id IS NULL)
        """)
        affected_row_count = h.execute()
        if (affected_row_count > 0):
            log(
                0,
                "Transferred {} orphaned vendor packages to the default organization"
                .format(affected_row_count)
            )

    def match_package_checksum(self, md_pack, db_pack):
        """compare package checksum"""

        abspath = md_pack.path
        if (self.deep_verify or
            md_pack.checksum_type != db_pack['checksum_type'] or
            md_pack.checksum != db_pack['checksum']):

            if (os.path.exists(abspath) and
                getFileChecksum(md_pack.checksum_type, filename=abspath) == md_pack.checksum):

                return True
            else:
                return False
        return True

    def associate_package(self, pack):
        package = {}
        package['name'] = pack.name
        package['version'] = pack.version
        package['release'] = pack.release
        package['arch'] = pack.arch
        if pack.a_pkg:
            package['checksum'] = pack.a_pkg.checksum
            package['checksum_type'] = pack.a_pkg.checksum_type
            # use epoch from file header because createrepo puts epoch="0" to
            # primary.xml even for packages with epoch=''
            package['epoch'] = pack.a_pkg.header['epoch']
        else:
            # RPM not available but package metadata are in DB, reuse these values
            package['checksum'] = pack.checksum
            package['checksum_type'] = pack.checksum_type
            package['epoch'] = pack.epoch
        package['channels'] = [{'label': self.channel_label,
                                'id': self.channel['id']}]
        package['org_id'] = self.channel['org_id']

        return IncompletePackage().populate(package)

    def disassociate_package(self, pack):
        h = rhnSQL.prepare("""
            delete from rhnChannelPackage cp
             where cp.channel_id = :channel_id
               and cp.package_id in (select p.id
                                       from rhnPackage p
                                       join rhnChecksumView c
                                         on p.checksum_id = c.id
                                      where c.checksum = :checksum
                                        and c.checksum_type = :checksum_type
                                    )
        """)
        h.execute(channel_id=int(self.channel['id']),
                  checksum_type=pack['checksum_type'], checksum=pack['checksum'])

    def load_channel(self):
        return rhnChannel.channel_info(self.channel_label)

    @staticmethod
    def _to_db_date(date):
        if not date:
            ret = datetime.utcnow()
        elif date.isdigit():
            ret = datetime.fromtimestamp(float(date))
        else:
            ret = parse_date(date)
            try:
                ret = ret.astimezone(tzutc())
            except ValueError, e:
                log(2, e)
        return ret.isoformat(' ')[:19]  # return 1st 19 letters of date, therefore preventing ORA-01830 caused by fractions of seconds

    @staticmethod
    def fix_notice(notice):
        if "." in notice['version']:
            new_version = 0
            for n in notice['version'].split('.'):
                new_version = (new_version + int(n)) * 100
            try:
                notice['version'] = new_version / 100
            except TypeError: # yum in RHEL5 does not have __setitem__
                notice._md['version'] = new_version / 100
        if RepoSync._is_old_suse_style(notice):
            # old suse style; we need to append the version to id
            # to get a seperate patch for every issue
            try:
                notice['update_id'] = notice['update_id'] + '-' + notice['version']
            except TypeError: # yum in RHEL5 does not have __setitem__
                notice._md['update_id'] = notice['update_id'] + '-' + notice['version']
        return notice

    @staticmethod
    def get_errata(update_id):
        """ Return an Errata dict

        search in the database for the given advisory and
        return a dict with important values.
        If the advisory was not found it returns None

        :update_id - the advisory (name)
        """
        h = rhnSQL.prepare("""
            select e.id, e.advisory,
                   e.advisory_name, e.advisory_rel,
                   TO_CHAR(e.update_date, 'YYYY-MM-DD HH24:MI:SS') as update_date
              from rhnerrata e
             where e.advisory = :name
        """)
        h.execute(name=update_id)
        ret = h.fetchone_dict()
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
        ret['packages'] = []

        h = rhnSQL.prepare("""
            select p.id as package_id,
                   pn.name,
                   pevr.epoch,
                   pevr.version,
                   pevr.release,
                   pa.label as arch,
                   p.org_id,
                   cv.checksum,
                   cv.checksum_type
              from rhnerratapackage ep
              join rhnpackage p on p.id = ep.package_id
              join rhnpackagename pn on pn.id = p.name_id
              join rhnpackageevr pevr on pevr.id = p.evr_id
              join rhnpackagearch pa on pa.id = p.package_arch_id
              join rhnchecksumview cv on cv.id = p.checksum_id
             where ep.errata_id = :eid
        """)
        h.execute(eid=ret['id'])
        packages = h.fetchall_dict() or []
        for pkg in packages:
            ipackage = IncompletePackage().populate(pkg)
            ipackage['epoch'] = pkg.get('epoch', '')

            ipackage['checksums'] = {ipackage['checksum_type']: ipackage['checksum']}
            ret['packages'].append(ipackage)

        return ret

    def list_errata(self):
        """List advisory names present in channel"""
        h = rhnSQL.prepare("""select e.advisory_name
            from rhnChannelErrata ce
            inner join rhnErrata e on e.id = ce.errata_id
            where ce.channel_id = :cid
        """)
        h.execute(cid=self.channel['id'])
        advisories = [row['advisory_name'] for row in h.fetchall_dict()]
        return advisories

    def import_kickstart(self, plug, repo_label):
        ks_path = 'rhn/kickstart/'
        ks_tree_label = re.sub(r'[^-_0-9A-Za-z@.]', '', repo_label.replace(' ', '_'))
        if len(ks_tree_label) < 4:
            ks_tree_label += "_repo"

        # construct ks_path and check we already have this KS tree synced
        id_request = """
                select id
                from rhnKickstartableTree
                where channel_id = :channel_id and label = :label
                """

        if 'org_id' in self.channel and self.channel['org_id']:
            ks_path += str(self.channel['org_id']) + '/' + ks_tree_label
            # Trees synced from external repositories are expected to have full path it database
            db_path = os.path.join(CFG.MOUNT_POINT, ks_path)
            row = rhnSQL.fetchone_dict(id_request + " and org_id = :org_id", channel_id=self.channel['id'],
                                       label=ks_tree_label, org_id=self.channel['org_id'])
        else:
            ks_path += ks_tree_label
            db_path = ks_path
            row = rhnSQL.fetchone_dict(id_request + " and org_id is NULL", channel_id=self.channel['id'],
                                       label=ks_tree_label)

        treeinfo_path = ['treeinfo', '.treeinfo']
        treeinfo_parser = None
        for path in treeinfo_path:
            log(1, "Trying " + path)
            treeinfo = plug.get_file(path, os.path.join(plug.repo.basecachedir, plug.name))
            if treeinfo:
                try:
                    treeinfo_parser = TreeInfoParser(treeinfo)
                    break
                except TreeInfoError:
                    pass

        if not treeinfo_parser:
            log(0, "Kickstartable tree not detected (no valid treeinfo file)")
            return

        if self.ks_install_type is None:
            family = treeinfo_parser.get_family()
            if family == 'Fedora':
                self.ks_install_type = 'fedora18'
            elif family == 'CentOS':
                self.ks_install_type = 'rhel_' + treeinfo_parser.get_major_version()
            else:
                self.ks_install_type = 'generic_rpm'

        fileutils.createPath(os.path.join(CFG.MOUNT_POINT, ks_path))
        # Make sure images are included
        to_download = set()
        for repo_path in treeinfo_parser.get_images():
            local_path = os.path.join(CFG.MOUNT_POINT, ks_path, repo_path)
            # TODO: better check
            if not os.path.exists(local_path) or self.force_kickstart:
                to_download.add(repo_path)

        if row:
            log(0, "Kickstartable tree %s already synced. Updating content..." % ks_tree_label)
            ks_id = row['id']
        else:
            row = rhnSQL.fetchone_dict("""
                select sequence_nextval('rhn_kstree_id_seq') as id from dual
                """)
            ks_id = row['id']

            rhnSQL.execute("""
                       insert into rhnKickstartableTree (id, org_id, label, base_path, channel_id, kstree_type,
                                                         install_type, last_modified, created, modified)
                       values (:id, :org_id, :label, :base_path, :channel_id,
                                 ( select id from rhnKSTreeType where label = :ks_tree_type),
                                 ( select id from rhnKSInstallType where label = :ks_install_type),
                                 current_timestamp, current_timestamp, current_timestamp)""", id=ks_id,
                           org_id=self.channel['org_id'], label=ks_tree_label, base_path=db_path,
                           channel_id=self.channel['id'], ks_tree_type=self.ks_tree_type,
                           ks_install_type=self.ks_install_type)

            log(0, "Added new kickstartable tree %s. Downloading content..." % ks_tree_label)

        insert_h = rhnSQL.prepare("""
                insert into rhnKSTreeFile (kstree_id, relative_filename, checksum_id, file_size, last_modified, created,
                 modified) values (:id, :path, lookup_checksum('sha256', :checksum), :st_size,
                 epoch_seconds_to_timestamp_tz(:st_time), current_timestamp, current_timestamp)
        """)

        delete_h = rhnSQL.prepare("""
                delete from rhnKSTreeFile where kstree_id = :id and relative_filename = :path
        """)

        # Downloading/Updating content of KS Tree
        # start from root dir
        is_root = True
        dirs_queue = ['']
        log(0, "Gathering all files in kickstart repository...")
        while len(dirs_queue) > 0:
            cur_dir_name = dirs_queue.pop(0)
            cur_dir_html = plug.get_file(cur_dir_name)
            if cur_dir_html is None:
                continue

            blacklist = None
            if is_root:
                blacklist = [treeinfo_parser.get_package_dir() + '/']
                is_root = False

            parser = KSDirParser(cur_dir_html, blacklist)

            for ks_file in parser.get_content():
                repo_path = cur_dir_name + ks_file['name']
                # if this is a directory, just add a name into queue (like BFS algorithm)
                if ks_file['type'] == 'DIR':
                    dirs_queue.append(repo_path)
                    continue

                if not os.path.exists(os.path.join(CFG.MOUNT_POINT, ks_path, repo_path)) or self.force_kickstart:
                    to_download.add(repo_path)

        if to_download:
            log(0, "Downloading %d kickstart files." % len(to_download))
            progress_bar = ProgressBarLogger("Downloading kickstarts:", len(to_download))
            downloader = ThreadedDownloader(force=self.force_kickstart)
            for item in to_download:
                params = {}
                plug.set_download_parameters(params, item, os.path.join(CFG.MOUNT_POINT, ks_path, item))
                downloader.add(params)
            downloader.set_log_obj(progress_bar)
            downloader.run()
            log2disk(0, "Download finished.")
            for item in to_download:
                st = os.stat(os.path.join(CFG.MOUNT_POINT, ks_path, item))
                # update entity about current file in a database
                delete_h.execute(id=ks_id, path=item)
                insert_h.execute(id=ks_id, path=item,
                                 checksum=getFileChecksum('sha256', os.path.join(CFG.MOUNT_POINT, ks_path, item)),
                                 st_size=st.st_size, st_time=st.st_mtime)
        else:
            log(0, "No new kickstart files to download.")

        # set permissions recursively
        rhnSQL.commit()


##############################################################################
### SUSE only code                                                         ###
##############################################################################

    def _format_sources(self, sources, excluded_urls):
        ret = []
        for item in sources:
            if item['source_url'] not in excluded_urls:
                ret.append(
                    dict(
                        id=item['id'],
                        source_url=[item['source_url']],
                        metadata_signed=item['metadata_signed'],
                        label=item['label']
                    )
                )
        return ret

    def set_repo_credentials(self, url_dict):
        """Set the credentials in the url_dict['source_url'] url list from the config file"""
        return [self._url_with_repo_credentials(url) for url in url_dict['source_url']]

    def _url_with_repo_credentials(self, url_in):
        """Adds the credentials to the given url from the config file

        We look for the `credentials` query argument and use its value
        as the location of the username and password in the current
        configuration file.

        Examples:
        ?credentials=mirrcred - read 'mirrcred_user' and 'mirrcred_pass'
        ?credeentials=mirrcred_5 - read 'mirrcred_user_5' and 'mirrcred_pass_5'

        """
        url = suseLib.URL(url_in)
        creds = url.get_query_param('credentials')
        if creds:
            creds_no = 0
            try:
                creds_no = int(creds.split("_")[1])
            except (ValueError, IndexError):
                log2(0, 0,
                    "Could not figure out which credentials to use "
                    "for this URL: {0}".format(url.getURL(), stream=sys.stderr)
                )
                sys.exit(1)
            # SCC - read credentials from DB
            h = rhnSQL.prepare("SELECT username, password FROM suseCredentials WHERE id = :id")
            h.execute(id=creds_no)
            credentials = h.fetchone_dict() or None
            if not credentials:
                log2(0, 0, "Could not figure out which credentials to use "
                           "for this URL: "+url.getURL(), stream=sys.stderr)
                sys.exit(1)
            url.username = credentials['username']
            url.password = base64.decodestring(credentials['password'])
            # remove query parameter from url
            url.query = ""
        return url.getURL()

    def upload_patches(self, notices):
        """Insert the information from patches into the database

        :arg notices: a list of ElementTree roots from individual patch files

        """
        typemap = {'security'    : 'Security Advisory',
                   'recommended' : 'Bug Fix Advisory',
                   'bugfix'      : 'Bug Fix Advisory',
                   'optional'    : 'Product Enhancement Advisory',
                   'feature'     : 'Product Enhancement Advisory',
                   'enhancement' : 'Product Enhancement Advisory'
                   }
        backend = SQLBackend()
        skipped_updates = 0
        batch = []

        for notice in notices:
            e = Erratum()

            version = notice.find(YUM+'version').get('ver')
            category = notice.findtext(PATCH+'category')

            e['advisory']     = e['advisory_name'] = self._patch_naming(notice)
            e['errata_from']  = 'maint-coord@suse.de'
            e['advisory_rel'] = version
            e['advisory_type'] = typemap.get(category,
                                             'Product Enhancement Advisory')

            existing_errata = self.get_errata(e['advisory'])

            if (existing_errata and
                not self.errata_needs_update(existing_errata, version,
                                             self._to_db_date(notice.get('timestamp')))):
                continue
            log(0, "Add Patch %s" % e['advisory'])

            # product name
            query = rhnSQL.prepare("""
                SELECT p.friendly_name
                  FROM suseproducts p
                  JOIN suseproductchannel pc on p.id = pc.product_id
                 WHERE pc.channel_id = :channel_id
                """)
            query.execute(channel_id=int(self.channel['id']))
            try:
                e['product'] = query.fetchone()[0]
            except TypeError:
                e['product'] = 'unknown product'

            for desc_lang in notice.findall(PATCH+'description'):
                if desc_lang.get('lang') == 'en':
                    e['description'] = desc_lang.text or 'not set'
                    break
            for sum_lang in notice.findall(PATCH+'summary'):
                if sum_lang.get('lang') == 'en':
                    e['synopsis'] = sum_lang.text or 'not set'
                    break
            e['topic']       = ' '
            e['solution']    = ' '
            e['issue_date']  = self._to_db_date(notice.get('timestamp'))
            e['update_date'] = e['issue_date']
            e['notes']       = ''
            e['org_id']      = self.channel['org_id']
            e['refers_to']   = ''
            e['channels']    = [{'label': self.channel_label}]
            e['packages']    = []
            e['files']       = []
            if existing_errata:
                e['channels'].extend(existing_errata['channels'])
                e['packages'] = existing_errata['packages']

            atoms = notice.find(PATCH+'atoms')
            packages = atoms.findall(YUM+'package')

            e['packages'] = self._patches_process_packages(packages,
                                                           e['advisory_name'],
                                                           e['packages'])
            # an update can't have zero packages, so we skip this update
            if not e['packages']:
                skipped_updates = skipped_updates + 1
                continue

            e['keywords'] = []
            if notice.find(PATCH+'reboot-needed') is not None:
                kw = Keyword()
                kw.populate({'keyword': 'reboot_suggested'})
                e['keywords'].append(kw)
            if notice.find(PATCH+'package-manager') is not None:
                kw = Keyword()
                kw.populate({'keyword': 'restart_suggested'})
                e['keywords'].append(kw)

            e['bugs'] = self.find_bugs(e['description'])
            e['cve'] = self.find_cves(e['description'])
            # set severity to Low to get a currency rating
            e['security_impact'] = "Low"

            e['locally_modified'] = None
            batch.append(e)
            if self.deep_verify:
                # import step by step
                importer = ErrataImport(batch, backend)
                importer.run()
                batch = []

        if skipped_updates > 0:
            log(0, "%d patches skipped because of incomplete package list." % skipped_updates)
        if len(batch) > 0:
            importer = ErrataImport(batch, backend)
            importer.run()
        self.regen = True

    def errata_needs_update(self, existing_errata, new_errata_version, new_errata_changedate):
        """check, if the errata in the DB needs an update

           new_errata_version: integer version number
           new_errata_changedate: date of the last change in DB format "%Y-%m-%d %H:%M:%S"
        """
        if self.deep_verify:
            # with deep_verify always re-import all errata
            return True

        if int(existing_errata['advisory_rel']) < int(new_errata_version):
            log(2, "Patch need update: higher version")
            return True
        newdate = datetime.strptime(new_errata_changedate,
                                    "%Y-%m-%d %H:%M:%S")
        olddate = datetime.strptime(existing_errata['update_date'],
                                    "%Y-%m-%d %H:%M:%S")
        if newdate > olddate:
            log(2, "Patch need update: newer update date - %s > %s" % (newdate, olddate))
            return True
        for c in existing_errata['channels']:
            if self.channel_label == c['label']:
                log(2, "No update needed")
                return False
        log(2, "Patch need update: channel not yet part of the patch")
        return True

    def _updates_process_packages(self, packages, advisory_name,
                                  existing_packages):
        """Check if the packages are in the database

        Go through the list of 'packages' and for each of them
        check to see if it is already present in the database. If it is,
        return a list of IncompletePackage objects, otherwise return an
        empty list.

        :packages: a list of dicts that represent packages (updateinfo style)
        :advisory_name: the name of the current erratum
        :existing_packages: list of already existing packages for this errata

        """
        erratum_packages = existing_packages
        for pkg in packages:
            param_dict = {
                'name': pkg['name'],
                'version': pkg['version'],
                'release': pkg['release'],
                'arch': pkg['arch'],
                'epoch': pkg['epoch'],
                'channel_id': int(self.channel['id'])}
            if param_dict['arch'] not in self.arches:
                continue
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                if 'epoch' not in param_dict:
                    param_dict['epoch'] = ''
                else:
                    param_dict['epoch'] = '%s:' % param_dict['epoch']
                if "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s" % param_dict not in self.available_packages:
                    continue
                # This package could not be found in the database
                # but should be available in this repo
                # so we skip the broken patch.
                errmsg = ("The package "
                          "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s "
                          "which is referenced by patch %(patch)s was not found "
                          "in the database. This patch has been skipped." % dict(
                              patch=advisory_name,
                              **param_dict))
                log(0, errmsg)
                self.error_messages.append(errmsg)
                return []

            # add new packages to the errata
            found = False
            for oldpkg in erratum_packages:
                if oldpkg['package_id'] == ret['package_id']:
                    found = True
            if not found:
                erratum_packages.append(ret)
        return erratum_packages

    def _patches_process_packages(self, packages, advisory_name, existing_packages):
        """Check if the packages are in the database

        Go through the list of 'packages' and for each of them
        check to see if it is already present in the database. If it is,
        return a list of IncompletePackage objects, otherwise return an
        empty list.

        :packages: a list of dicts that represent packages (patch style)
        :advisory_name: the name of the current erratum
        :existing_packages: list of already existing packages for this errata

        """
        erratum_packages = existing_packages
        for pkg in packages:
            nevr = pkg.find(YUM+'format').find(RPM+'requires').find(RPM+'entry')
            param_dict = {
                'name': nevr.get('name'),
                'version': nevr.get('ver'),
                'release': nevr.get('rel'),
                'epoch': nevr.get('epoch'),
                'arch': pkg.findtext(YUM+'arch'),
                'channel_id': int(self.channel['id'])
            }
            if param_dict['arch'] not in self.arches:
                continue
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                if 'epoch' not in param_dict:
                    param_dict['epoch'] = ''
                else:
                    param_dict['epoch'] = '%s:' % param_dict['epoch']
                if "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s" % param_dict not in self.available_packages:
                    continue
                # This package could not be found in the database
                # but should be available in this repo
                # so we skip the broken patch.
                errmsg = ("The package "
                          "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s "
                          "which is referenced by patch %(patch)s was not found "
                          "in the database. This patch has been skipped." % dict(
                              patch=advisory_name,
                              **param_dict))
                log(0, errmsg)
                self.error_messages.append(errmsg)
                return []

            # add new packages to the errata
            found = False
            for oldpkg in erratum_packages:
                if oldpkg['package_id'] == ret['package_id']:
                    found = True
            if not found:
                erratum_packages.append(ret)
        return erratum_packages

    def import_products(self, repo):
        products = repo.get_products()
        for product in products:
            query = rhnSQL.prepare("""
                select spf.id
                  from suseProductFile spf
                  join rhnpackageevr pe on pe.id = spf.evr_id
                  join rhnpackagearch pa on pa.id = spf.package_arch_id
                 where spf.name = :name
                   and spf.evr_id = LOOKUP_EVR(:epoch, :version, :release)
                   and spf.package_arch_id = LOOKUP_PACKAGE_ARCH(:arch)
                   and spf.vendor = :vendor
                   and spf.summary = :summary
                   and spf.description = :description
            """)
            query.execute(**product)
            row = query.fetchone_dict()
            if not row or not row.has_key('id'):
                get_id_q = rhnSQL.prepare("""SELECT sequence_nextval('suse_prod_file_id_seq') as id FROM dual""")
                get_id_q.execute()
                row = get_id_q.fetchone_dict() or {}
                if not row or not row.has_key('id'):
                    print "no id for sequence suse_prod_file_id_seq"
                    continue

                h = rhnSQL.prepare("""
                    insert into suseProductFile
                        (id, name, evr_id, package_arch_id, vendor, summary, description)
                    VALUES (:id, :name, LOOKUP_EVR(:epoch, :version, :release),
                            LOOKUP_PACKAGE_ARCH(:arch), :vendor, :summary, :description)
                """)
                h.execute(id=row['id'], **product)

            params = {
                'product_cap'   : "product(%s)" % product['name'],
                'cap_version'   : product['version'] + "-" + product['release'],
                'channel_id'    : int(self.channel['id'])
            }
            if self.channel['org_id']:
                org_statement = "and p.org_id = :channel_org"
                params['channel_org'] = self.channel['org_id']
            else:
                org_statement = "and p.org_id is NULL"

            query = rhnSQL.prepare("""
                select p.id
                  from rhnPackage p
                  join rhnPackageProvides pp on pp.package_id = p.id
                  join rhnPackageCapability pc on pc.id = pp.capability_id
                  join rhnChannelPackage cp on cp.package_id = p.id
                 where pc.name = :product_cap
                   and pc.version = :cap_version
                   and cp.channel_id = :channel_id
                   %s
            """ % org_statement)

            query.execute(**params)
            packrow = query.fetchone_dict()
            if not packrow or not packrow.has_key('id'):
                # package not in DB
                continue

            h = rhnSQL.prepare("""select 1 from susePackageProductFile where package_id = :paid and prodfile_id = :prid""")
            h.execute(paid=packrow['id'], prid=row['id'])
            ex = h.fetchone_dict() or None
            if not ex:
                h = rhnSQL.prepare("""insert into susePackageProductFile (package_id, prodfile_id)
                    VALUES (:package_id, :product_id)
                """)
                h.execute(package_id=packrow['id'], product_id=row['id'])
                self.regen = True

    def import_susedata(self, repo):
        kwcache = {}
        susedata = repo.get_susedata()
        for package in susedata:
            query = rhnSQL.prepare("""
                SELECT p.id
                  FROM rhnPackage p
                  JOIN rhnPackagename pn ON p.name_id = pn.id
                  JOIN rhnChecksumView c ON p.checksum_id = c.id
                  JOIN rhnChannelPackage cp ON p.id = cp.package_id
                 WHERE pn.name = :name
                   AND p.evr_id = LOOKUP_EVR(:epoch, :version, :release)
                   AND p.package_arch_id = LOOKUP_PACKAGE_ARCH(:arch)
                   AND cp.channel_id = :channel_id
                   AND c.checksum = :pkgid
                """)
            query.execute(name=package['name'], epoch=package['epoch'],
                          version=package['version'], release=package['release'],
                          arch=package['arch'], pkgid=package['pkgid'],
                          channel_id=int(self.channel['id']))
            row = query.fetchone_dict() or None
            if not row or not row.has_key('id'):
                # package not found in DB
                continue
            pkgid = int(row['id'])
            log(4, "import_susedata pkgid: %s channelId: %s" % (pkgid, int(self.channel['id'])))

            h = rhnSQL.prepare("""
                SELECT smk.id, smk.label
                  FROM suseMdData smd
                  JOIN suseMdKeyword smk ON smk.id = smd.keyword_id
                 WHERE smd.package_id = :package_id
                   AND smd.channel_id = :channel_id
            """)
            h.execute(package_id=pkgid, channel_id=int(self.channel['id']))
            ret = h.fetchall_dict() or {}
            pkgkws = {}
            for row in ret:
                log(4, "DB keyword: %s kid: %s" % (row['label'], row['id']))
                pkgkws[row['label']] = False
                kwcache[row['label']] = row['id']

            for keyword in package['keywords']:
                log(4, "Metadata keyword: %s" % keyword)
                if keyword not in kwcache:
                    kw = rhnSQL.prepare("""select LOOKUP_MD_KEYWORD(:label) id from dual""")
                    kw.execute(label=keyword)
                    kwid = kw.fetchone_dict()['id']
                    kwcache[keyword] = kwid

                if keyword in pkgkws:
                    pkgkws[keyword] = True
                else:
                    log(4, "Insert new keywordId: %s pkgId: %s channelId: %s" % (kwcache[keyword], pkgid, int(self.channel['id'])))
                    kadd = rhnSQL.prepare("""INSERT INTO suseMdData (package_id, channel_id, keyword_id)
                                              VALUES(:package_id, :channel_id, :keyword_id)""")
                    kadd.execute(package_id=pkgid, channel_id=int(self.channel['id']), keyword_id=kwcache[keyword])
                    self.regen = True

            if package.has_key('eula'):
                eula_id = suseEula.find_or_create_eula(package['eula'])
                rhnPackage.add_eula_to_package(
                  package_id=pkgid,
                  eula_id=eula_id
                )

            # delete all removed keywords
            for label in pkgkws:
                if not pkgkws[label]:
                    log(4, "Delete obsolete keywordId: %s pkgId: %s channelId: %s" % (kwcache[label], pkgid, int(self.channel['id'])))
                    kdel = rhnSQL.prepare("""DELETE FROM suseMdData WHERE package_id = :package_id
                                             AND channel_id = :channel_id AND keyword_id = :keyword_id""")
                    kdel.execute(package_id=pkgid, channel_id=int(self.channel['id']), keyword_id=kwcache[label])
                    self.regen = True

    def _patch_naming(self, notice):
        """Return the name of the patch according to our rules

        :notice: a notice/patch object (this could be a dictionary
        (new-style) or an ElementTree element (old code10 style))

        """
        try:
            version = int(notice.find(YUM+'version').get('ver'))
        except AttributeError:
            # normal yum updates (dicts)
            patch_name = notice['update_id']
        else:
            # code10 patches
            if version >= 1000:
                # old suse style patch naming
                patch_name = notice.get('patchid')
            else:
                # new suse style patch naming
                patch_name = notice.find(YUM+'name').text

        # remove the channel-specific prefix
        # this way we can merge patches from different channels like
        # SDK, HAE and SLES
        update_tag = self.channel['update_tag']
        if update_tag and patch_name.startswith(update_tag):
            patch_name = patch_name[len(update_tag)+1:] # +1 for the hyphen
        elif update_tag and update_tag in patch_name:
            # SLE12 has SUSE-<update-tag>-...
            patch_name = patch_name.replace('SUSE-' + update_tag , 'SUSE', 1)

        return patch_name

    def _process_package(self, param_dict, advisory_name):
        """Search for a package in the the database

        Search for the package specified by 'param_dict' to see if it is
        already present in the database. If it is, return a
        IncompletePackage objects, otherwise return None.

        :param_dict: dict that represent packages (nerva + channel_id)
        :advisory_name: the name of the current erratum

        """
        pkgepoch = param_dict['epoch']
        del param_dict['epoch']

        if not pkgepoch or pkgepoch == '0':
            epochStatement = "(pevr.epoch is NULL or pevr.epoch = '0')"
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
             where pn.name = :name
               and p.org_id %s
               and pevr.version = :version
               and pevr.release = :release
               and pa.label = :arch
               and %s
               and at.label = 'rpm'
               and cp.channel_id = :channel_id
            """ % (orgidStatement, epochStatement))
        h.execute(**param_dict)
        cs = h.fetchone_dict()

        if not cs:
            return None

        package = IncompletePackage()
        for k in param_dict:
            if k not in ['epoch', 'channel_label', 'channel_id']:
                package[k] = param_dict[k]
        package['epoch'] = cs['epoch']
        package['org_id'] = self.channel['org_id']

        package['checksums'] = {cs['checksum_type'] : cs['checksum']}
        package['checksum_type'] = cs['checksum_type']
        package['checksum'] = cs['checksum']

        package['package_id'] = cs['id']
        return package

    def sendErrorMail(self, body):
        to = CFG.TRACEBACK_MAIL
        fr = to
        if isinstance(to, type([])):
            fr = to[0].strip()
            to = ', '.join([s.strip() for s in to])

        headers = {
            "Subject" : "SUSE Manager repository sync failed (%s)" % hostname,
            "From"    : "%s <%s>" % (hostname, fr),
            "To"      : to,
        }
        extra = "Syncing Channel '%s' failed:\n\n" % self.channel_label
        rhnMail.send(headers, extra + body)

    def updateChannelChecksumType(self, repo_checksum_type):
        """
        check, if the checksum_type of the channel matches the one of the repo
        if not, change the type of the channel
        """
        if self.channel['org_id']:
            # custom channels are user managed.
            # Do not autochange this
            return

        h = rhnSQL.prepare("""SELECT ct.label
                                FROM rhnChannel c
                                JOIN rhnChecksumType ct ON c.checksum_type_id = ct.id
                               WHERE c.id = :cid""")
        h.execute(cid=self.channel['id'])
        d = h.fetchone_dict() or None
        if d and d['label'] == repo_checksum_type:
            # checksum_type is the same, no need to change anything
            return
        h = rhnSQL.prepare("""SELECT id FROM rhnChecksumType WHERE label = :clabel""")
        h.execute(clabel=repo_checksum_type)
        d = h.fetchone_dict() or None
        if not (d and d['id']):
            # unknown or invalid checksum_type
            # better not change the channel
            return
        # update the checksum_type
        h = rhnSQL.prepare("""UPDATE rhnChannel
                                 SET checksum_type_id = :ctid
                               WHERE id = :cid""")
        h.execute(ctid=d['id'], cid=self.channel['id'])

    @staticmethod
    def get_compatible_arches(channel_id):
        """Return a list of compatible package arch labels for this channel"""
        h = rhnSQL.prepare("""select pa.label
                              from rhnChannelPackageArchCompat cpac,
                              rhnChannel c,
                              rhnpackagearch pa
                              where c.id = :channel_id
                              and c.channel_arch_id = cpac.channel_arch_id
                              and cpac.package_arch_id = pa.id""")
        h.execute(channel_id=channel_id)
        # We do not mirror source packages. If they are listed in patches
        # we need to know, that it is safe to skip them
        arches = [k['label'] for k in  h.fetchall_dict() if k['label'] not in ['src', 'nosrc']]
        return arches

    @staticmethod
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

    @staticmethod
    def _update_bugs(notice):
        """Return a list of Bug objects from the notice's references"""
        bugs = {}
        if notice['references'] is None:
            return []
        for bz in notice['references']:
            if bz['type'] == 'bugzilla':
                # Fix: in case of non-integer id try to parse it from href
                if not bz['id'].isdigit():
                    log(2, "Bugzilla ID is wrong: {0}. Trying to parse ID from from URL".format(bz["id"]))
                    bz_id_match = re.search("/show_bug.cgi\?id=(\d+)", bz["href"])
                    if bz_id_match:
                        bz["id"] = bz_id_match.group(1)
                        log(2, "Bugzilla ID found: {0}".format(bz["id"]))
                    else:
                        log2(0, 0, "Unable to found Bugzilla ID for {0}. Omitting".format(bz["id"]), stream=sys.stderr)
                        continue
                if bz['id'] not in bugs:
                    bug = Bug()
                    bug.populate({'bug_id': bz['id'],
                                  'summary': bz['title'] or ("Bug %s" % bz['id']),
                                  'href': bz['href']})
                    bugs[bz['id']] = bug
        return bugs.values()

    @staticmethod
    def _update_cve(notice):
        """Return a list of unique ids from notice references of type 'cve'"""
        cves = []
        if notice['description'] is not None:
            # sometimes CVE numbers appear in the description, but not in
            # the reference list
            cves = RepoSync.find_cves(notice['description'])
        if notice['references'] is not None:
            cves.extend([cve['id'][:20] for cve in notice['references'] if cve['type'] == 'cve'])
        # remove duplicates
        cves = list(set(cves))

        return cves

    @staticmethod
    def _is_old_suse_style(notice):
        if((notice['from'] and "suse" in notice['from'].lower() and
            int(notice['version']) >= 1000) or
            (notice['update_id'][:4] in ('res5', 'res6') and int(notice['version']) > 6 ) or
            (notice['update_id'][:4] == 'res4')):
            # old style suse updateinfo starts with version >= 1000 or
            # have the res update_tag
            return True
        return False

    @staticmethod
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

    @staticmethod
    def find_cves(text):
        """Find and return a list of CVE ids

        Matches:
         - CVE-YEAR-NUMBER

         Beginning 2014, the NUMBER has no maximal length anymore.
         We limit the length at 20 chars, because of the DB column size
        """
        cves = list()
        cves.extend([cve[:20] for cve in set(re.findall('CVE-\d{4}-\d+', text))])
        return cves


    @staticmethod
    def _delete_invalid_errata(errata_id):
        """
        Remove the errata from all channels
        This should only be alled in case of a disaster
        """
        # first get a list of all channels where this errata exists
        h = rhnSQL.prepare("""
            SELECT channel_id
              FROM rhnChannelErrata
             WHERE errata_id = :errata_id
        """)
        h.execute(errata_id=errata_id)
        channels = map(lambda x: x['channel_id'], h.fetchall_dict() or [])

        # delete channel from errata
        h = rhnSQL.prepare("""
            DELETE FROM rhnChannelErrata
             WHERE errata_id = :errata_id
        """)
        h.execute(errata_id=errata_id)

        # delete all packages from errata
        h = rhnSQL.prepare("""
            DELETE FROM rhnErrataPackage ep
             WHERE ep.errata_id = :errata_id
        """)
        h.execute(errata_id=errata_id)

        # delete files from errata
        h = rhnSQL.prepare("""
            DELETE FROM rhnErrataFile
             WHERE errata_id = :errata_id
        """)
        h.execute(errata_id=errata_id)

        # delete erratatmp
        h = rhnSQL.prepare("""
            DELETE FROM rhnErrataTmp
             WHERE id = :errata_id
        """)
        h.execute(errata_id=errata_id)

        # delete errata
        # removes also references from rhnErrataCloned
        # and rhnServerNeededCache
        h = rhnSQL.prepare("""
            DELETE FROM rhnErrata
             WHERE id = :errata_id
        """)
        h.execute(errata_id=errata_id)
        rhnSQL.commit()
        update_needed_cache = rhnSQL.Procedure("rhn_channel.update_needed_cache")

        for cid in channels:
            update_needed_cache(cid)
        rhnSQL.commit()
