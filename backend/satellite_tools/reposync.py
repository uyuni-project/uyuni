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
import hashlib
import os
import re
import socket
import sys
import time
import traceback
from datetime import datetime
from optparse import OptionParser

from yum import Errors
from yum.i18n import to_unicode

from spacewalk.server import rhnPackage, rhnSQL, rhnChannel, rhnPackageUpload
from spacewalk.common import rhnMail, rhnLog, suseLib, rhn_pkg
from spacewalk.common.rhnTB import fetchTraceback
from spacewalk.common.rhnLog import log_debug
from spacewalk.common.checksum import getFileChecksum
from spacewalk.common.rhnConfig import CFG, initCFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.server.importlib.importLib import IncompletePackage, Erratum, Bug, Keyword
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.errataImport import ErrataImport
from spacewalk.server import taskomatic

hostname = socket.gethostname()

default_log_location = '/var/log/rhn/reposync/'
default_hash = 'sha256'

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
        return '%s' % unicode(self.value, "utf-8")

class ChannelTimeoutException(ChannelException):
    """Channel timeout error e.g. a remote repository is not responding"""
    pass

class RepoSync(object):
    def __init__(self, channel_label, repo_type, url=None, fail=False,
                 quiet=False, noninteractive=False, filters=[],
                 deep_verify=False):
        self.regen = False
        self.fail = fail
        self.quiet = quiet
        self.interactive = not noninteractive
        self.filters = filters
        self.deep_verify = deep_verify

        initCFG('server.susemanager')
        db_string = CFG.DEFAULT_DB #"rhnsat/rhnsat@rhnsat"
        rhnSQL.initDB(db_string)

        # setup logging
        log_filename = 'reposync.log'
        date = time.localtime()
        datestr = '%d.%02d.%02d-%02d:%02d:%02d' % (
            date.tm_year, date.tm_mon, date.tm_mday, date.tm_hour,
            date.tm_min, date.tm_sec)
        log_filename = channel_label + '-' +  datestr + '.log'
        rhnLog.initLOG(default_log_location + log_filename)
        #os.fchown isn't in 2.4 :/
        os.system("chgrp www " + default_log_location + log_filename)

        self.log_msg("\nSync started: %s" % (time.asctime(time.localtime())))
        self.log_msg(str(sys.argv))

        if not url:
            # TODO:need to look at user security across orgs
            h = rhnSQL.prepare("""select s.id, s.source_url, s.metadata_signed
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
            self.urls = [{'id': None, 'source_url': url, 'metadata_signed' : 'N'}]

        self.repo_plugin = self.load_plugin(repo_type)
        self.channel_label = channel_label

        self.channel = self.load_channel()
        if not self.channel:
            self.print_msg("Channel does not exist.")
            sys.exit(1)

        self.arches = get_compatible_arches(self.channel['id'])

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
        start_time = datetime.now()
        for data in self.urls:
            url = suseLib.URL(data['source_url'])
            if url.get_query_param("credentials"):
                url.username = CFG.get("%s%s" % (url.get_query_param("credentials"), "_user"))
                url.password = CFG.get("%s%s" % (url.get_query_param("credentials"), "_pass"))
            url.query = ""
            insecure = False
            if data['metadata_signed'] == 'N':
                insecure = True
            try:
                repo = self.repo_plugin(url.getURL(), self.channel_label,
                                        insecure, self.quiet, self.interactive)
                self.import_packages(repo, data['id'], url.getURL())
                self.import_products(repo)
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
        total_time = datetime.now() - start_time
        self.print_msg("Sync completed.")
        self.print_msg("Total time: %s" % str(total_time).split('.')[0])


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
        if notices:
            if notices_type == 'updateinfo':
                self.upload_updates(notices)
            elif notices_type == 'patches':
                self.upload_patches(notices)

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

            existing_errata = get_errata(e['advisory'])

            # product name
            query = rhnSQL.prepare("""
                SELECT p.friendly_name
                  FROM suseproducts p
                  JOIN suseproductchannel pc on p.id = pc.product_id
                  JOIN rhnchannel c on pc.channel_id = c.id
                 WHERE c.label = :label
                """)
            query.execute(label=self.channel_label)
            try:
                e['product'] = query.fetchone()[0]
            except TypeError:
                e['product'] = 'unknown product'

            for desc_lang in notice.findall(PATCH+'description'):
                if desc_lang.get('lang') == 'en':
                    e['description'] = desc_lang.text or ""
                    break
            for sum_lang in notice.findall(PATCH+'summary'):
                if sum_lang.get('lang') == 'en':
                    e['synopsis'] = sum_lang.text or ""
                    break
            e['topic']       = ' '
            e['solution']    = ' '
            e['issue_date']  = _to_db_date(notice.get('timestamp'))
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

            e['bugs'] = find_bugs(e['description'])
            e['cve'] = find_cves(e['description'])
            # set severity to Low to get a currency rating
            e['security_impact'] = "Low"

            e['locally_modified'] = None
            batch.append(e)

        if skipped_updates > 0:
            self.print_msg("%d patches skipped because of incomplete package list." % skipped_updates)
        backend = SQLBackend()
        importer = ErrataImport(batch, backend)
        importer.run()
        self.regen = True

    def upload_updates(self, notices):
        skipped_updates = 0
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
            notice = _fix_notice(notice)
            patch_name = self._patch_naming(notice)
            existing_errata = get_errata(patch_name)
            if existing_errata and not _is_old_suse_style(notice['from'], notice['version']):
                if existing_errata['advisory_rel'] < notice['version']:
                    # A disaster happens
                    #
                    # re-releasing an errata with a higher release number
                    # only happens in case of a disaster.
                    # This should force mirrored repos to remove the old
                    # errata and take care that the new one is the only
                    # available.
                    # This mean a hard overwrite
                    _delete_invalid_errata(existing_errata['id'])
                elif existing_errata['advisory_rel'] > notice['version']:
                    # the existing errata has a higher release than the now
                    # parsed one. We need to skip the current errata
                    continue
                # else: release match, so we update the errata
            e = Erratum()
            e['errata_from']   = notice['from']
            e['advisory'] = e['advisory_name'] = patch_name
            e['advisory_rel']  = notice['version']
            e['advisory_type'] = typemap.get(notice['type'], 'Product Enhancement Advisory')
            e['product']       = notice['release']
            e['description']   = notice['description']
            e['synopsis']      = notice['title']
            e['topic']         = ' '
            e['solution']      = ' '
            e['issue_date']    = _to_db_date(notice['issued'])
            if notice['updated']:
                e['update_date'] = _to_db_date(notice['updated'])
            else:
                e['update_date'] = e['issue_date']
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
                skipped_updates = skipped_updates + 1
                continue

            e['keywords'] = _update_keywords(notice)
            e['bugs'] = _update_bugs(notice)
            e['cve'] = _update_cve(notice)
            if notice['severity']:
                e['security_impact'] = notice['severity']
            else:
                # 'severity' not available in older yum versions
                # set default to Low to get a correct currency rating
                e['security_impact'] = "Low"
            e['locally_modified'] = None
            batch.append(e)

        if skipped_updates > 0:
            self.print_msg("%d patches skipped because of empty package list." % skipped_updates)
        backend = SQLBackend()
        importer = ErrataImport(batch, backend)
        importer.run()
        self.regen = True

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
                'channel_label' : self.channel_label
            }
            if self.channel['org_id']:
                org_statement = "and p.org_id = :channel_org"
                params['org_id'] = self.channel['org_id']
            else:
                org_statement = "and p.org_id is NULL"

            query = rhnSQL.prepare("""
                select p.id
                  from rhnPackage p
                  join rhnPackageProvides pp on pp.package_id = p.id
                  join rhnPackageCapability pc on pc.id = pp.capability_id
                  join rhnChannelPackage cp on cp.package_id = p.id
                  join rhnChannel c on c.id = cp.channel_id
                 where pc.name = :product_cap
                   and pc.version = :cap_version
                   and c.label = :channel_label
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
        prefix = self.channel['update_tag']
        if prefix and patch_name.startswith(prefix):
            patch_name = patch_name[len(prefix)+1:] # +1 for the hyphen

        return patch_name

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
                'channel_label': self.channel_label}
            if param_dict['arch'] not in self.arches:
                continue
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                # This package could not be found in the database
                # so we skip the broken patch.
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
                'channel_label': self.channel_label
            }
            if param_dict['arch'] not in self.arches:
                continue
            ret = self._process_package(param_dict, advisory_name)
            if not ret:
                # This package could not be found in the database
                # so we skip the broken patch.
                return []

            # add new packages to the errata
            found = False
            for oldpkg in erratum_packages:
                if oldpkg['package_id'] == ret['package_id']:
                    found = True
            if not found:
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

    def import_packages(self, plug, source_id, url):
        if (not self.filters) and source_id:
            h = rhnSQL.prepare("""
                    select flag, filter
                      from rhnContentSourceFilter
                     where source_id = :source_id
                     order by sort_order """)
            h.execute(source_id = source_id)
            filter_data = h.fetchall_dict() or []
            filters = [(row['flag'], re.split('[,\s]+', row['filter']))
                                                         for row in filter_data]
        else:
            filters = self.filters

        packages = plug.list_packages(filters)
        to_process = []
        skipped = 0
        saveurl = suseLib.URL(url)
        if saveurl.password:
            saveurl.password = "*******"
        num_passed = len(packages)
        self.print_msg("Repo URL: %s" % saveurl.getURL())
        self.print_msg("Packages in repo:             %5d" % plug.num_packages)
        if plug.num_excluded:
            self.print_msg("Packages passed filter rules: %5d" % num_passed)

        for pack in packages:
            if pack.arch in ['src', 'nosrc']:
                # skip source packages
                skipped += 1
                continue
            if pack.arch not in self.arches:
                # skip packages with incompatible architecture
                skipped += 1
                continue

            db_pack = rhnPackage.get_info_for_package(
                   [pack.name, pack.version, pack.release, pack.epoch, pack.arch],
                   self.channel_label)

            to_download = True
            to_link     = True
            if db_pack['path']:
                # if the package exists, but under a different org_id we have to download it again
                if db_pack['org_id'] == self.channel['org_id'] and self.match_package_checksum(pack, db_pack):
                    # package is already on disk
                    to_download = False
                    pack.set_checksum(db_pack['checksum_type'], db_pack['checksum'])
                    if db_pack['channel_label'] == self.channel_label:
                        # package is already in the channel
                        to_link = False
                elif db_pack['channel_label'] == self.channel_label:
                    # different package with SAME NVREA
                    self.disassociate_package(db_pack)
                
            if to_download or to_link:
                to_process.append((pack, to_download, to_link))

        num_to_process = len(to_process)
        if num_to_process == 0:
            self.print_msg("No new packages to sync.")
            return
        else:
            self.print_msg("Packages already synced:      %5d" %
                                                  (num_passed - num_to_process))
            self.print_msg("Packages to sync:             %5d" % num_to_process)

        self.regen=True
        is_non_local_repo = (url.find("file://") < 0)

        def finally_remove(path):
            if is_non_local_repo and path and os.path.exists(path):
                os.remove(path)

        # try/except/finally doesn't work in python 2.4 (RHEL5), so here's a hack
        for (index, what) in enumerate(to_process):
            pack, to_download, to_link = what
            localpath = None

            try:
                self.print_msg("%d/%d : %s" % (index+1, num_to_process, pack.getNVREA()))
                if to_download:
                    pack.path = localpath = plug.get_package(pack)
                pack.load_checksum_from_header()
                if to_download:
                    pack.upload_package(self.channel)
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
            pack.clear_header()

    def match_package_checksum(self, md_pack, db_pack):
        """compare package checksum"""

        md_pack.path = abspath = os.path.join(CFG.MOUNT_POINT, db_pack['path'])
        if (self.deep_verify or
            md_pack.predef_checksum_type != db_pack['checksum_type'] or
            md_pack.predef_checksum != db_pack['checksum']):

            if (os.path.exists(abspath) and
                getFileChecksum(md_pack.predef_checksum_type, filename=abspath) == md_pack.predef_checksum):

                return True
            else:
                return False
        return True

    def associate_package(self, pack):
        caller = "server.app.yumreposync"
        backend = SQLBackend()
        package = {}
        package['name'] = pack.name
        package['version'] = pack.version
        package['release'] = pack.release
        package['arch'] = pack.arch
        package['checksum'] = pack.a_pkg.checksum
        package['checksum_type'] = pack.a_pkg.checksum_type
        package['channels']  = [{'label':self.channel_label,
                                 'id':self.channel['id']}]
        package['org_id'] = self.channel['org_id']

        imported = False
        # yum's createrepo puts epoch="0" to primary.xml even for packages
        # with epoch='' so we have to check empty epoch first because it's
        # more common situation
        if pack.epoch == '0':
            package['epoch'] = ''
            try:
                self._importer_run(package, caller, backend)
                imported = True
            except:
                pass
        if not imported:
            package['epoch'] = pack.epoch
            self._importer_run(package, caller, backend)

        backend.commit()

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
        h.execute(channel_id=self.channel['id'],
                  checksum_type=pack['checksum_type'], checksum=pack['checksum'])

    def _importer_run(self, package, caller, backend):
        importer = ChannelPackageSubscription(
            [IncompletePackage().populate(package)],
            backend, caller=caller, repogen=False)
        importer.run()

    def load_channel(self):
        return rhnChannel.channel_info(self.channel_label)

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
            fr = to[0].strip()
            to = ', '.join([s.strip() for s in to])

        headers = {
            "Subject" : "SUSE Manager repository sync failed (%s)" % hostname,
            "From"    : "%s <%s>" % (hostname, fr),
            "To"      : to,
        }
        extra = "Syncing Channel '%s' failed:\n\n" % self.channel_label
        rhnMail.send(headers, extra + body)

def get_errata(update_id):
    """ Return an Errata dict

    search in the database for the given advisory and
    return a dict with important values.
    If the advisory was not found it returns None

    :update_id - the advisory (name)
    """
    h = rhnSQL.prepare("""
        select e.id, e.advisory,
               e.advisory_name, e.advisory_rel
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

        ipackage['checksums'] = {ipackage['checksum_type'] : ipackage['checksum']}
        ret['packages'].append(ipackage)

    return ret

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
    arches = [k['label'] for k in  h.fetchall_dict()]
    return arches

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

def _fix_notice(notice):
    if "." in notice['version']:
        new_version = 0
        for n in notice['version'].split('.'):
            new_version = (new_version + int(n)) * 100
        try:
            notice['version'] = new_version / 100
        except TypeError: # yum in RHEL5 does not have __setitem__
            notice._md['version'] = new_version / 100
    if _is_old_suse_style(notice['from'], notice['version']):
        # old suse style; we need to append the version to id
        # to get a seperate patch for every issue
        try:
            notice['update_id'] = notice['update_id'] + '-' + notice['version']
        except TypeError: # yum in RHEL5 does not have __setitem__
            notice._md['update_id'] = notice['update_id'] + '-' + notice['version']
    return notice

def _is_old_suse_style(notice_from, notice_version):
    if "suse" in notice_from.lower() and int(notice_version) >= 1000:
        # old style suse updateinfo starts with version >= 1000
        return True
    return False

class ContentPackage:

    def __init__(self):
        # map of checksums
        self.checksums = {}
        self.predef_checksum_type = None
        self.predef_checksum = None

        #unique ID that can be used by plugin
        self.unique_id = None

        self.name = None
        self.version = None
        self.release = None
        self.epoch = None
        self.arch = None

        self.path = None
        self.file = None

        self.a_pkg = None

    def clear_header(self):
        """a_pkg hold the header data. Remove it to not waste memory"""
        self.a_pkg = None

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
        self.a_pkg = rhn_pkg.package_from_stream(self.file, packaging='rpm')
        self.a_pkg.read_header()
	if self.predef_checksum_type:
            self.a_pkg.set_checksum_type(self.predef_checksum_type)
        if not self.a_pkg.checksum:
            self.a_pkg.payload_checksum()
        self.file.close()
        if self.predef_checksum != self.a_pkg.checksum:
            raise rhnFault(50, "checksums did not match %s vs %s" % (self.predef_checksum, self.a_pkg.checksum), explain=0)

    def upload_package(self, channel):
        rel_package_path = rhnPackageUpload.relative_path_from_header(
                self.a_pkg.header, channel['org_id'],
                self.a_pkg.checksum_type, self.a_pkg.checksum)
        package_dict, diff_level = rhnPackageUpload.push_package(self.a_pkg,
                force=False,
                relative_path=rel_package_path,
                org_id=channel['org_id'])

    def set_checksum(self, checksum_type=None, checksum=None):
        if checksum_type and checksum:
            self.predef_checksum_type = checksum_type
            self.predef_checksum = checksum
            if not((checksum_type in self.checksums) and (self.checksums[checksum_type] == checksum)):
                self.checksums[checksum_type] = checksum

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

def set_filter_opt(option, opt_str, value, parser):
    if opt_str in [ '--include', '-i']: f_type = '+'
    else:                               f_type = '-'
    parser.values.filters.append((f_type, re.split('[,\s]+', value)))

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
