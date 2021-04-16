#
# Copyright (c) 2008--2018 Red Hat, Inc.
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

import time
from uyuni.common.usix import StringType

from uyuni.common import rhnLib
from spacewalk.common.rhnLog import log_debug
from spacewalk.server import rhnSQL


class ArrayIterator:

    def __init__(self, arr):
        self._arr = arr
        if self._arr:
            self._pos = 0
        else:
            # Nothing to iterate over
            self._pos = None

    def get_array(self):
        return self._arr

    def fetchone_dict(self):
        if self._pos is None:
            return None
        i = self._pos
        self._pos = self._pos + 1
        if self._pos == len(self._arr):
            self._pos = None
        return self._arr[i]


class BaseDumper:
    # tag_name has to be set in subclasses

    def __init__(self, writer, data_iterator=None):
        self._writer = writer
        self._attributes = {}
        self._iterator = data_iterator

    # Generic timing function
    @staticmethod
    def timer(debug_level, message, function, *args, **kwargs):
        start = time.time()
        result = function(*args, **kwargs)
        log_debug(debug_level, message, "timing: %.3f" % (time.time() - start))
        return result

    def set_attributes(self):
        return self._attributes

    def set_iterator(self):
        return self._iterator

    def dump(self):
        if not hasattr(self, "tag_name"):
            raise Exception("Programmer error: subclass did not set tag_name")
        tag_name = getattr(self, "tag_name")
        self._attributes = self.set_attributes() or {}
        self._iterator = self.timer(5, "set_iterator", self.set_iterator)

        if not self._iterator:
            self._writer.empty_tag(tag_name, attributes=self._attributes)
            return

        data_found = 0
        while 1:
            data = self.timer(6, "fetchone_dict", self._iterator.fetchone_dict)
            if not data:
                break
            if not data_found:
                data_found = 1
                self._writer.open_tag(tag_name, attributes=self._attributes)

            if isinstance(data, StringType):
                # The iterator produced some XML dump, just write it
                self._writer.stream.write(data)
            else:
                self.timer(6, "dump_subelement", self.dump_subelement, data)

        if data_found:
            self._writer.close_tag(tag_name)
        else:
            self._writer.empty_tag(tag_name, attributes=self._attributes)

    def dump_subelement(self, data):
        # pylint: disable=R0201
        if isinstance(data, BaseDumper):
            data.dump()

    def get_writer(self):
        return self._writer

    def set_writer(self, writer):
        self._writer = writer


class EmptyDumper(BaseDumper):

    def __init__(self, writer, tag_name, attributes=None):
        self.tag_name = tag_name
        self.attributes = attributes or {}
        BaseDumper.__init__(self, writer)

    def dump(self):
        self._writer.empty_tag(self.tag_name, attributes=self.attributes)


class SimpleDumper(BaseDumper):

    def __init__(self, writer, tag_name, value, max_value_bytes=None):
        self.tag_name = tag_name
        self._value = value

        # max number of bytes satellite can handle in the matching db row
        self._max_value_bytes = max_value_bytes
        BaseDumper.__init__(self, writer)

    def dump(self):
        self._writer.open_tag(self.tag_name)
        if self._value is None:
            self._writer.empty_tag('rhn-null')
        else:
            self._writer.data(self._value)
        self._writer.close_tag(self.tag_name)


class BaseRowDumper(BaseDumper):

    def __init__(self, writer, row):
        BaseDumper.__init__(self, writer)
        self._row = row


class BaseChecksumRowDumper(BaseRowDumper):

    def set_iterator(self):
        # checksums
        checksum_arr = [{'type':  self._row['checksum_type'],
                         'value': self._row['checksum']}]
        arr = [_ChecksumDumper(self._writer, data_iterator=ArrayIterator(checksum_arr))]
        return ArrayIterator(arr)


class BaseQueryDumper(BaseDumper):
    iterator_query = None

    def set_iterator(self):
        if self._iterator:
            return self._iterator
        h = rhnSQL.prepare(self.iterator_query)
        h.execute()
        return h


class BaseSubelementDumper(BaseDumper):
    # pylint: disable=E1101
    subelement_dumper_class = object

    def dump_subelement(self, data):
        d = self.subelement_dumper_class(self._writer, data)
        d.dump()

####


class ExportTypeDumper(BaseDumper):

    def __init__(self, writer, start_date=None, end_date=None):
        if start_date:
            self.type = 'incremental'
        else:
            self.type = 'full'
        self.start_date = start_date
        if end_date:
            self.end_date = end_date
        else:
            self.end_date = time.strftime("%Y%m%d%H%M%S")
        BaseDumper.__init__(self, writer)

    def dump(self):
        self._writer.open_tag('export-type')
        self._writer.stream.write(self.type)
        self._writer.close_tag('export-type')
        if self.start_date:
            self._writer.open_tag('export-start-date')
            self._writer.stream.write(self.start_date)
            self._writer.close_tag('export-start-date')
        if self.end_date:
            self._writer.open_tag('export-end-date')
            self._writer.stream.write(self.end_date)
            self._writer.close_tag('export-end-date')


class SatelliteDumper(BaseDumper):
    tag_name = 'rhn-satellite'

    def __init__(self, writer, *dumpers):
        BaseDumper.__init__(self, writer)
        self._dumpers = dumpers

    def set_attributes(self):
        return {
            'version': 'x.y',
        }

    def set_iterator(self):
        return ArrayIterator(self._dumpers)


class _OrgTrustDumper(BaseDumper):
    tag_name = 'rhn-org-trusts'

    def dump_subelement(self, data):
        c = EmptyDumper(self._writer, 'rhn-org-trust', attributes={
            'org-id': data['org_trust_id'],
        })
        c.dump()


class _OrgDumper(BaseDumper):
    tag_name = 'rhn-org'

    def __init__(self, writer, org):
        self.org = org
        BaseDumper.__init__(self, writer)

    _query_org_trusts = """
        select rto.org_trust_id
          from rhnTrustedOrgs rto
         where rto.org_id = :org_id
    """

    def set_iterator(self):
        # trusts
        h = rhnSQL.prepare(self._query_org_trusts)
        h.execute(org_id=self.org['id'])
        return ArrayIterator([_OrgTrustDumper(self._writer, data_iterator=h)])

    def set_attributes(self):
        attributes = {
            'id': self.org['id'],
            'name': self.org['name'],
        }
        return attributes


class OrgsDumper(BaseDumper):
    tag_name = 'rhn-orgs'

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator)

    def dump_subelement(self, data):
        org = _OrgDumper(self._writer, data)
        org.dump()


class ChannelTrustedOrgsDumper(BaseDumper):
    tag_name = 'rhn-channel-trusted-orgs'

    def dump_subelement(self, data):
        d = EmptyDumper(self._writer, 'rhn-channel-trusted-org',
                        attributes={'org-id': data['org_trust_id']})
        d.dump()


class _ChannelDumper(BaseRowDumper):
    tag_name = 'rhn-channel'

    def __init__(self, writer, row, start_date=None, end_date=None, use_rhn_date=True, whole_errata=False):
        BaseRowDumper.__init__(self, writer, row)
        self.start_date = start_date
        self.end_date = end_date
        self.use_rhn_date = use_rhn_date
        self.whole_errata = whole_errata

    def set_attributes(self):
        channel_id = self._row['id']

        packages = ["rhn-package-%s" % x for x in self._get_package_ids()]
        # XXX channel-errata is deprecated and should go away in dump version
        # 3 or higher - we now dump that information in its own subelement
        # rhn-channel-errata
        errata = ["rhn-erratum-%s" % x for x in self._get_errata_ids()]
        ks_trees = self._get_kickstartable_trees()

        return {
            'channel-id': 'rhn-channel-%s' % channel_id,
            'label': self._row['label'],
            'org_id': self._row['org_id'] or "",
            'channel-arch': self._row['channel_arch'],
            'packages': ' '.join(packages),
            'channel-errata': ' '.join(errata),
            'kickstartable-trees': ' '.join(ks_trees),
            'sharing': self._row['channel_access'],
        }

    _query_channel_families = rhnSQL.Statement("""
        select cf.id, cf.label
          from rhnChannelFamily cf, rhnChannelFamilyMembers cfm
         where cfm.channel_family_id = cf.id
           and cfm.channel_id = :channel_id
    """)
    _query_dist_channel_map = rhnSQL.Statement("""
        select dcm.os, dcm.release, ca.label channel_arch
          from rhnDistChannelMap dcm, rhnChannelArch ca
         where dcm.channel_id = :channel_id
           and dcm.channel_arch_id = ca.id
           and dcm.org_id is null
    """)

    _query_get_channel_trusts = rhnSQL.Statement("""
        select org_trust_id
          from rhnChannelTrust
         where channel_id = :channel_id
    """)

    def set_iterator(self):
        channel_id = self._row['id']
        arr = []
        mappings = [
            ('rhn-channel-parent-channel', 'parent_channel'),
            ('rhn-channel-basedir', 'basedir'),
            ('rhn-channel-name', 'name'),
            ('rhn-channel-summary', 'summary'),
            ('rhn-channel-description', 'description'),
            ('rhn-channel-gpg-key-url', 'gpg_key_url'),
            ('rhn-channel-checksum-type', 'checksum_type'),
            ('rhn-channel-update-tag', 'update_tag'),
            ('rhn-channel-installer-updates', 'installer_updates'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))

        arr.append(SimpleDumper(self._writer, 'rhn-channel-last-modified',
                                _dbtime2timestamp(self._row['last_modified'])))
        channel_product_details = self._get_channel_product_details()
        arr.append(SimpleDumper(self._writer, 'rhn-channel-product-name',
                                channel_product_details[0]))
        arr.append(SimpleDumper(self._writer, 'rhn-channel-product-version',
                                channel_product_details[1]))
        arr.append(SimpleDumper(self._writer, 'rhn-channel-product-beta',
                                channel_product_details[2]))

        comp_last_modified = self._channel_comps_last_modified()
        modules_last_modified = self._channel_modules_last_modified()
        if comp_last_modified is not None:
            arr.append(SimpleDumper(self._writer, 'rhn-channel-comps-last-modified',
                                    _dbtime2timestamp(comp_last_modified[0])))
        if modules_last_modified is not None:
            arr.append(SimpleDumper(self._writer, 'rhn-channel-modules-last-modified',
                                    _dbtime2timestamp(modules_last_modified[0])))

        h = rhnSQL.prepare(self._query_get_channel_trusts)
        h.execute(channel_id=channel_id)
        arr.append(ChannelTrustedOrgsDumper(self._writer, data_iterator=h))

        h = rhnSQL.prepare(self._query_channel_families)
        h.execute(channel_id=channel_id)
        arr.append(ChannelFamiliesDumper(self._writer, data_iterator=h,
                                         ignore_subelements=1))

        h = rhnSQL.prepare(self._query_dist_channel_map)
        h.execute(channel_id=channel_id)
        arr.append(DistsDumper(self._writer, h))

        # Source package information (with timestamps)
        h = self._get_cursor_source_packages()
        arr.append(ChannelSourcePackagesDumper(self._writer, h))
        # Errata information (with timestamps)
        query_args = {'channel_id': channel_id}
        if self.start_date:
            if self.use_rhn_date:
                query = self._query__get_errata_ids_by_rhnlimits
            else:
                query = self._query__get_errata_ids_by_limits
            query_args.update({'lower_limit': self.start_date,
                               'upper_limit': self.end_date})
        else:
            query = self._query__get_errata_ids

        h = rhnSQL.prepare(query)
        h.execute(**query_args)
        arr.append(ChannelErrataDumper(self._writer, h))
        arr.append(ExportTypeDumper(self._writer, self.start_date, self.end_date))

        return ArrayIterator(arr)

    _query_get_package_ids = rhnSQL.Statement("""
        select package_id as id
          from rhnChannelPackage
         where channel_id = :channel_id
    """)

    _query_get_package_ids_by_date_limits = rhnSQL.Statement("""
        select package_id as id
          from rhnChannelPackage rcp
         where rcp.channel_id = :channel_id
           and rcp.modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and rcp.modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
     """)

    _query_get_package_ids_by_rhndate_limits = rhnSQL.Statement("""
        select package_id as id
          from rhnPackage rp, rhnChannelPackage rcp
         where rcp.channel_id = :channel_id
           and rcp.package_id = rp.id
           and rp.last_modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and rp.last_modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
     """)

    _query_pkgids_by_date_whole_errata = rhnSQL.Statement("""
        select rcp.package_id as id
          from rhnChannelPackage rcp, rhnPackage rp
            left join rhnErrataPackage rep on rp.id = rep.package_id
            left join rhnErrata re on rep.errata_id = re.id
         where rcp.channel_id = :channel_id
           and rcp.package_id = rp.id
           and ((re.modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
               and re.modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
            ) or (rep.package_id is NULL
               and rcp.modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
               and rcp.modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS'))
            )
     """)

    _query_get_pkgids_by_rhndate_whole_errata = rhnSQL.Statement("""
        select rcp.package_id as id
          from rhnChannelPackage rcp, rhnPackage rp
            left join rhnErrataPackage rep on rp.id = rep.package_id
            left join rhnErrata re on rep.errata_id = re.id
         where rcp.channel_id = :channel_id
           and rcp.package_id = rp.id
           and ((re.last_modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
               and re.last_modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
            ) or (rep.package_id is NULL
               and rp.last_modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
               and rp.last_modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS'))
            )
     """)

    # Things that can be overwriten in subclasses
    def _get_package_ids(self):
        if self.start_date and self.whole_errata:
            return self._get_ids(self._query_pkgids_by_date_whole_errata,
                                 self._query_get_pkgids_by_rhndate_whole_errata,
                                 self._query_get_package_ids)
        else:
            return self._get_ids(self._query_get_package_ids_by_date_limits,
                                 self._query_get_package_ids_by_rhndate_limits,
                                 self._query_get_package_ids)

    def _get_ids(self, query_with_limit, query_with_rhnlimit, query_no_limits):
        query_args = {'channel_id': self._row['id']}
        if self.start_date:
            if self.use_rhn_date:
                query = query_with_rhnlimit
            else:
                query = query_with_limit
            query_args.update({'lower_limit': self.start_date,
                               'upper_limit': self.end_date})
        else:
            query = query_no_limits
        h = rhnSQL.prepare(query)
        h.execute(**query_args)
        return [x['id'] for x in h.fetchall_dict() or []]

    _query_get_source_package_ids = rhnSQL.Statement("""
        select distinct ps.id, sr.name source_rpm,
               TO_CHAR(ps.last_modified, 'YYYYMMDDHH24MISS') last_modified
          from rhnChannelPackage cp, rhnPackage p, rhnPackageSource ps,
               rhnSourceRPM sr
         where cp.channel_id = :channel_id
           and cp.package_id = p.id
           and p.source_rpm_id = ps.source_rpm_id
           and ((p.org_id is null and ps.org_id is null) or
               p.org_id = ps.org_id)
           and ps.source_rpm_id = sr.id
    """)

    def _get_cursor_source_packages(self):
        channel_id = self._row['id']

        h = rhnSQL.prepare(self._query_get_source_package_ids)
        h.execute(channel_id=channel_id)
        return h

    _query__get_errata_ids = rhnSQL.Statement("""
        select ce.errata_id as id, e.advisory_name,
              TO_CHAR(e.last_modified, 'YYYYMMDDHH24MISS') last_modified
          from rhnChannelErrata ce, rhnErrata e
         where ce.channel_id = :channel_id
           and ce.errata_id = e.id
    """)

    _query__get_errata_ids_by_limits = rhnSQL.Statement("""
         %s
           and ce.modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and ce.modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
    """ % _query__get_errata_ids)

    _query__get_errata_ids_by_rhnlimits = rhnSQL.Statement("""
         %s
           and e.last_modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and e.last_modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
    """ % _query__get_errata_ids)

    def _get_errata_ids(self):
        return self._get_ids(self._query__get_errata_ids_by_limits,
                             self._query__get_errata_ids_by_rhnlimits,
                             self._query__get_errata_ids)

    _query_get_kickstartable_trees = rhnSQL.Statement("""
        select kt.label as id
          from rhnKickstartableTree kt
         where kt.channel_id = :channel_id
           and kt.org_id is null
    """)

    _query_get_kickstartable_trees_by_rhnlimits = rhnSQL.Statement("""
         %s
           and kt.last_modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and kt.last_modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
    """ % _query_get_kickstartable_trees)

    _query_get_kickstartable_trees_by_limits = rhnSQL.Statement("""
         %s
           and kt.modified >= TO_TIMESTAMP(:lower_limit, 'YYYYMMDDHH24MISS')
           and kt.modified <= TO_TIMESTAMP(:upper_limit, 'YYYYMMDDHH24MISS')
    """ % _query_get_kickstartable_trees)

    def _get_kickstartable_trees(self):
        ks_trees = self._get_ids(self._query_get_kickstartable_trees_by_limits,
                                 self._query_get_kickstartable_trees_by_rhnlimits,
                                 self._query_get_kickstartable_trees)
        ks_trees.sort()
        return ks_trees

    _query_get_channel_product_details = rhnSQL.Statement("""
        select cp.product as name,
               cp.version as version,
               cp.beta
        from rhnChannel c,
             rhnChannelProduct cp
        where c.id = :channel_id
          and c.channel_product_id = cp.id
    """)

    def _get_channel_product_details(self):
        """
        Export rhnChannelProduct table content through ChannelDumper

        return a tuple containing (product name, product version, beta status)
        or (None, None, None) if the information is missing
        """

        channel_id = self._row['id']

        h = rhnSQL.prepare(self._query_get_channel_product_details)
        h.execute(channel_id=channel_id)
        row = h.fetchone_dict()
        if not row:
            return (None, None, None)
        else:
            return (row['name'], row['version'], row['beta'])

    _query_channel_comps_last_modified = rhnSQL.Statement("""
        select to_char(last_modified, 'YYYYMMDDHH24MISS') as comps_last_modified
        from rhnChannelComps
        where channel_id = :channel_id
        and comps_type_id = 1
        order by id desc
    """)

    def _channel_comps_last_modified(self):
        channel_id = self._row['id']
        h = rhnSQL.prepare(self._query_channel_comps_last_modified)
        h.execute(channel_id=channel_id)
        return h.fetchone()

    _query_channel_modules_last_modified = rhnSQL.Statement("""
        select to_char(last_modified, 'YYYYMMDDHH24MISS') as modules_last_modified
        from rhnChannelComps
        where channel_id = :channel_id
        and comps_type_id = 2
        order by id desc
    """)

    def _channel_modules_last_modified(self):
        channel_id = self._row['id']
        h = rhnSQL.prepare(self._query_channel_modules_last_modified)
        h.execute(channel_id=channel_id)
        return h.fetchone()


class ChannelsDumper(BaseSubelementDumper):
    tag_name = 'rhn-channels'
    subelement_dumper_class = _ChannelDumper

    def __init__(self, writer, channels=()):
        super(BaseSubelementDumper, self).__init__(writer)
        self._channels = channels



class ChannelDumper(_ChannelDumper):

    # pylint: disable=W0231,W0233
    def __init__(self, writer, row):
        BaseRowDumper.__init__(self, writer, row)

    #_query_release_channel_map = rhnSQL.Statement("""
    #    select dcm.os product, dcm.release version,
    #           dcm.eus_release release, ca.label channel_arch,
    #           dcm.is_default is_default
    #      from rhnDistChannelMap dcm, rhnChannelArch ca
    #     where dcm.channel_id = :channel_id
    #       and dcm.channel_arch_id = ca.id
    #       and dcm.is_eus = 'Y'
    #""")

    def set_iterator(self):
        arrayiterator = _ChannelDumper.set_iterator()
        arr = arrayiterator.get_array()
        mappings = [
            ('rhn-channel-receiving-updates', 'receiving_updates'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))

        #channel_id = self._row['id']
        # Add EUS info
        #h = rhnSQL.prepare(self._query_release_channel_map)
        # h.execute(channel_id=channel_id)
        #arr.append(ReleaseDumper(self._writer, h))
        return arrayiterator

# class ReleaseDumper(BaseDumper):
#    tag_name = 'rhn-release'
#
#    def dump_subelement(self, data):
#        d = _ReleaseDumper(self._writer, data)
#        d.dump()
#
# class _ReleaseDumper(BaseRowDumper):
#    tag_name = 'rhn-release'
#
#    def set_attributes(self):
#        return {
#            'product'       : self._row['product'],
#            'version'       : self._row['version'],
#            'release'       : self._row['release'],
#            'channel-arch'  : self._row['channel_arch'],
#            'is-default'  : self._row['is_default'],
#        }


class _ChannelSourcePackageDumper(BaseRowDumper):
    tag_name = 'source-package'

    def set_attributes(self):
        return {
            'id': 'rhn-source-package-%s' % self._row['id'],
            'source-rpm': self._row['source_rpm'],
            'last-modified': _dbtime2timestamp(self._row['last_modified']),
        }


class ChannelSourcePackagesDumper(BaseSubelementDumper):
    # Dumps the erratum id and the last modified for an erratum in this
    # channel
    tag_name = 'source-packages'
    subelement_dumper_class = _ChannelSourcePackageDumper


class _ChannelErratumDumper(BaseRowDumper):
    tag_name = 'erratum'

    def set_attributes(self):
        return {
            'id': 'rhn-erratum-%s' % self._row['id'],
            'advisory-name': self._row['advisory_name'],
            'last-modified': _dbtime2timestamp(self._row['last_modified']),
        }


class ChannelErrataDumper(BaseSubelementDumper):
    # Dumps the erratum id and the last modified for an erratum in this
    # channel
    tag_name = 'rhn-channel-errata'
    subelement_dumper_class = _ChannelErratumDumper


class _DistDumper(BaseRowDumper):
    tag_name = 'rhn-dist'

    def set_attributes(self):
        return {
            'os': self._row['os'],
            'release': self._row['release'],
            'channel-arch': self._row['channel_arch'],
        }


class DistsDumper(BaseSubelementDumper):
    tag_name = 'rhn-dists'
    subelement_dumper_class = _DistDumper


class _SupportInfoDumper(BaseRowDumper):
    tag_name = 'suse-keyword'

    def set_attributes(self):
        return {
            'channel' : self._row['channel_label'],
            'pkgid'   : "rhn-package-%s" % self._row['package_id'],
            'keyword' : self._row['keyword'],
        }

class SupportInfoDumper(BaseQueryDumper):
    tag_name = 'suse-data'
    iterator_query = """
        select c.label channel_label,
               p.id    package_id,
               k.label keyword
          from rhnChannel c
          join suseMdData d on c.id = d.channel_id
          join rhnPackage p on d.package_id = p.id
          join suseMdKeyword k on d.keyword_id = k.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SupportInfoDumper(self._writer, data)
        cf.dump()

class _SuseProductDumper(BaseRowDumper):
    tag_name = 'suse-product'

    def set_attributes(self):
        return {
            'name'          : self._row['name'],
            'version'       : self._row['version'],
            'friendly-name' : self._row['friendly_name'],
            'arch'          : self._row['arch'],
            'release'       : self._row['release'],
            'product-id'    : self._row['product_id'],
            'free'          : self._row['free'],
            'base'          : self._row['base'],
            'release-stage' : self._row['release_stage'],
            'channel-family-label': self._row['channel_family_label']
        }

class SuseProductDumper(BaseQueryDumper):
    tag_name = 'suse-products'
    iterator_query = """
    SELECT p.name, p.version, p.friendly_name,
           pa.label AS arch, p.release, p.product_id,
           p.free, p.base, p.release_stage, cf.label AS channel_family_label
      FROM suseProducts p
 LEFT JOIN rhnPackageArch pa ON p.arch_type_id = pa.id
 LEFT JOIN rhnChannelFamily cf ON p.channel_family_id = cf.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseProductDumper(self._writer, data)
        cf.dump()

class _SuseProductChannelDumper(BaseRowDumper):
    tag_name = 'suse-product-channel'

    def set_attributes(self):
        return {
            'product-id'           : self._row['pdid'],
            'channel-label'        : self._row['clabel'],
            'parent-channel-label' : self._row['pclabel'],
            }

class SuseProductChannelDumper(BaseQueryDumper):
    tag_name = 'suse-product-channels'
    iterator_query = """
    SELECT p.product_id AS pdid,
           pr.channel_label as clabel,
           pr.parent_channel_label AS pclabel
      FROM suseProductSCCRepository pr
      JOIN suseProducts p ON pr.product_id = p.id
     WHERE EXISTS (select 1
                     FROM suseProductChannel pc
                     JOIN rhnChannel c ON c.id = pc.channel_id
                    WHERE p.id = pc.product_id
                      AND pc.mandatory = 'Y'
                      AND c.label = pr.channel_label)
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseProductChannelDumper(self._writer, data)
        cf.dump()

class _SuseUpgradePathDumper(BaseRowDumper):
    tag_name = 'suse-upgrade-path'

    def set_attributes(self):
        return {
            'from-product-id' : self._row['fromid'],
            'to-product-id'   : self._row['toid'],
            }

class SuseUpgradePathDumper(BaseQueryDumper):
    tag_name = 'suse-upgrade-paths'
    iterator_query = """
    SELECT p1.product_id AS fromid,
           p2.product_id AS toid
      FROM suseUpgradePath up
      JOIN suseProducts p1 ON up.from_pdid = p1.id
      JOIN suseProducts p2 ON up.to_pdid = p2.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseUpgradePathDumper(self._writer, data)
        cf.dump()

class _SuseProductExtensionDumper(BaseRowDumper):
    tag_name = 'suse-product-extension'

    def set_attributes(self):
        return {
            'root-product-id' : self._row['rootid'],
            'product-id' : self._row['pdid'],
            'ext-product-id' : self._row['extid'],
            'recommended': self._row['recommended']
            }

class SuseProductExtensionDumper(BaseQueryDumper):
    tag_name = 'suse-product-extensions'
    iterator_query = """
    SELECT p1.product_id AS pdid,
           p2.product_id AS rootid,
           p3.product_id AS extid,
           e.recommended AS recommended
      FROM suseProductExtension e
      JOIN suseProducts p1 ON e.base_pdid = p1.id
      JOIN suseProducts p2 ON e.root_pdid = p2.id
      JOIN suseProducts p3 ON e.ext_pdid = p3.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseProductExtensionDumper(self._writer, data)
        cf.dump()

class _SuseProductRepositoryDumper(BaseRowDumper):
    tag_name = 'suse-product-repository'

    def set_attributes(self):
        return {
            'root-product-id' : self._row['rootid'],
            'product-id' : self._row['pdid'],
            'repository-id' : self._row['repo_id'],
            'channel-label': self._row['channel_label'],
            'parent-channel-label': self._row['parent_channel_label'],
            'channel-name': self._row['channel_name'],
            'mandatory': self._row['mandatory'],
            'update-tag': self._row['update_tag']
            }

class SuseProductRepositoryDumper(BaseQueryDumper):
    tag_name = 'suse-product-repositories'
    iterator_query = """
    SELECT p1.product_id AS pdid,
           p2.product_id AS rootid,
           r.scc_id AS repo_id,
           pr.channel_label,
           pr.parent_channel_label,
           pr.channel_name,
           pr.mandatory,
           pr.update_tag
      FROM suseProductSCCRepository pr
      JOIN suseProducts p1 ON pr.product_id = p1.id
      JOIN suseProducts p2 ON pr.root_product_id = p2.id
      JOIN suseSCCRepository r ON pr.repo_id = r.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseProductRepositoryDumper(self._writer, data)
        cf.dump()

class _SCCRepositoryDumper(BaseRowDumper):
    tag_name = 'scc-repository'

    def set_attributes(self):
        return {
            'scc-id' : self._row['sccid'],
            'autorefresh' : self._row['autorefresh'],
            'name' : self._row['name'],
            'distro-target': self._row['distro_target'],
            'description': self._row['description'],
            'url': self._row['url'],
            'signed': self._row['signed'],
            'installer_updates': self._row['installer_updates']
            }

class SCCRepositoryDumper(BaseQueryDumper):
    tag_name = 'scc-repositories'
    iterator_query = """
    SELECT scc_id AS sccid,
           autorefresh, name, distro_target,
           description, url, signed, installer_updates
      FROM suseSCCRepository
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SCCRepositoryDumper(self._writer, data)
        cf.dump()

class _SuseSubscriptionDumper(BaseRowDumper):
    tag_name = 'suse-subscription'

    def set_attributes(self):
        return {
            'sub-label'       : self._row['label'],
            'sub-max-members' : self._row['max_members'],
            'sub-system-ent'  : self._row['system_entitlement'],
            }

class SuseSubscriptionDumper(BaseQueryDumper):
    tag_name = 'suse-subscriptions'
    iterator_query = """
        SELECT cf.label, 0 AS max_members, 0 AS system_entitlement
          FROM rhnPrivateChannelFamily pcf
          JOIN rhnChannelFamily cf ON pcf.channel_family_id = cf.id
         WHERE pcf.org_id = 1
         UNION
        SELECT sgt.label, 10 AS max_members, 1 AS system_entitlement
          FROM rhnServerGroup sg
          JOIN rhnServerGroupType sgt ON sg.group_type = sgt.id
         WHERE org_id = 1
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _SuseSubscriptionDumper(self._writer, data)
        cf.dump()

class _ClonedChannelsDumper(BaseRowDumper):
    tag_name = 'cloned-channel'

    def set_attributes(self):
        return {
            'orig'  : self._row['orig'],
            'clone' : self._row['clone'],
            }

class ClonedChannelsDumper(BaseQueryDumper):
    tag_name = 'cloned-channels'
    iterator_query = """
        SELECT c1.label orig,
               c2.label clone
          FROM rhnChannelCloned cc
          JOIN rhnChannel c1 ON c1.id = cc.original_id
          JOIN rhnChannel c2 ON c2.id = cc.id
    """

    def __init__(self, writer, data_iterator=None):
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        cf = _ClonedChannelsDumper(self._writer, data)
        cf.dump()

class ChannelFamiliesDumper(BaseQueryDumper):
    tag_name = 'rhn-channel-families'
    iterator_query = 'select cf.* from rhnChannelFamily'

    def __init__(self, writer, data_iterator=None, ignore_subelements=0,
                 null_max_members=1):
        BaseQueryDumper.__init__(self, writer, data_iterator=data_iterator)
        self._ignore_subelements = ignore_subelements
        self._null_max_members = null_max_members

    def dump_subelement(self, data):
        cf = _ChannelFamilyDumper(self._writer, data,
                                  ignore_subelements=self._ignore_subelements,
                                  null_max_members=self._null_max_members)
        cf.dump()


class _ChannelFamilyDumper(BaseRowDumper):
    tag_name = 'rhn-channel-family'

    def __init__(self, writer, row, ignore_subelements=0, null_max_members=1):
        BaseRowDumper.__init__(self, writer, row)
        self._ignore_subelements = ignore_subelements
        self._null_max_members = null_max_members

    def set_iterator(self):
        if self._ignore_subelements:
            return None

        arr = []

        mappings = [
            ('rhn-channel-family-name', 'name'),
            ('rhn-channel-family-product-url', 'product_url'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))

        return ArrayIterator(arr)

    _query_get_channel_family_channels = rhnSQL.Statement("""
        select c.label
          from rhnChannelFamilyMembers cfm, rhnChannel c
         where cfm.channel_family_id = :channel_family_id
           and cfm.channel_id = c.id
    """)

    def set_attributes(self):
        # Get all channels that are part of this channel family
        h = rhnSQL.prepare(self._query_get_channel_family_channels)
        channel_family_id = self._row['id']
        h.execute(channel_family_id=channel_family_id)
        channels = [x['label'] for x in h.fetchall_dict() or []]

        attributes = {
            'id': "rhn-channel-family-%s" % channel_family_id,
            'label': self._row['label'],
            'channel-labels': ' '.join(channels),
        }

        if self._ignore_subelements:
            return attributes
        if self._row['label'] != 'rh-public':
            if self._null_max_members:
                attributes['max-members'] = 0
            elif ('max_members' in self._row) and self._row['max_members']:
                attributes['max-members'] = self._row['max_members']
        return attributes

##


class _PackageDumper(BaseRowDumper):
    tag_name = 'rhn-package'

    def set_attributes(self):
        attrs = ["name", "version", "release", "package_arch",
                 "package_group", "rpm_version", "package_size", "payload_size",
                 "installed_size", "build_host", "source_rpm", "payload_format",
                 "compat"]
        attr_dict = {
            'id': "rhn-package-%s" % self._row['id'],
            'org_id': self._row['org_id'] or "",
            'epoch': self._row['epoch'] or "",
            'cookie': self._row['cookie'] or "",
            'build-time': _dbtime2timestamp(self._row['build_time']),
            'last-modified': _dbtime2timestamp(self._row['last_modified']),
        }
        for attr in attrs:
            attr_dict[attr.replace('_', '-')] = self._row[attr]
        if self._row['checksum_type'] == 'md5':
            # compatibility with older satellite
            attr_dict['md5sum'] = self._row['checksum']
        return attr_dict

    def set_iterator(self):
        arr = []

        mappings = [
            ('rhn-package-summary', 'summary'),
            ('rhn-package-description', 'description'),
            ('rhn-package-vendor', 'vendor'),
            ('rhn-package-copyright', 'copyright'),
            ('rhn-package-header-sig', 'header_sig'),
            ('rhn-package-header-start', 'header_start'),
            ('rhn-package-header-end', 'header_end')
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))

        # checksums
        checksum_arr = [{'type':  self._row['checksum_type'],
                         'value': self._row['checksum']}]
        arr.append(_ChecksumDumper(self._writer,
                                   data_iterator=ArrayIterator(checksum_arr)))

        h = rhnSQL.prepare("""
            select
                name, text,
                TO_CHAR(time, 'YYYYMMDDHH24MISS') as time
            from rhnPackageChangeLog
            where package_id = :package_id
        """)
        h.execute(package_id=self._row['id'])
        arr.append(_ChangelogDumper(self._writer, data_iterator=h))

        # Dependency information
        mappings = [
            ['rhnPackageRequires',    'rhn-package-requires',    'rhn-package-requires-entry'],
            ['rhnPackageProvides',    'rhn-package-provides',    'rhn-package-provides-entry'],
            ['rhnPackageConflicts',   'rhn-package-conflicts',   'rhn-package-conflicts-entry'],
            ['rhnPackageObsoletes',   'rhn-package-obsoletes',   'rhn-package-obsoletes-entry'],
            ['rhnPackageRecommends',  'rhn-package-recommends',  'rhn-package-recommends-entry'],
            ['rhnPackageSuggests',    'rhn-package-suggests',    'rhn-package-suggests-entry'],
            ['rhnPackageSupplements', 'rhn-package-supplements', 'rhn-package-supplements-entry'],
            ['rhnPackageEnhances',    'rhn-package-enhances',    'rhn-package-enhances-entry'],
            ['rhnPackageBreaks',      'rhn-package-breaks',      'rhn-package-breaks-entry'],
            ['rhnPackagePredepends',  'rhn-package-predepends',  'rhn-package-predepends-entry'],
        ]
        for table_name, container_name, entry_name in mappings:
            h = rhnSQL.prepare("""
                select pc.name, pc.version, pd.sense
                from %s pd, rhnPackageCapability pc
                where pd.capability_id = pc.id
                and pd.package_id = :package_id
            """ % table_name)
            h.execute(package_id=self._row['id'])
            arr.append(_DependencyDumper(self._writer, data_iterator=h,
                                         container_name=container_name,
                                         entry_name=entry_name))

        # Files
        h = rhnSQL.prepare("""
            select
                pc.name, pf.device, pf.inode, pf.file_mode, pf.username,
                pf.groupname, pf.rdev, pf.file_size,
                TO_CHAR(mtime, 'YYYYMMDDHH24MISS') mtime,
                c.checksum_type as "checksum-type",
                c.checksum, pf.linkto, pf.flags, pf.verifyflags, pf.lang
            from rhnPackageFile pf
            left join rhnChecksumView c
              on pf.checksum_id = c.id,
                rhnPackageCapability pc
            where pf.capability_id = pc.id
            and pf.package_id = :package_id
        """)
        h.execute(package_id=self._row['id'])
        arr.append(_PackageFilesDumper(self._writer, data_iterator=h))

        # SUSE Product Files
        h = rhnSQL.prepare("""
            SELECT spf.name, rpe.epoch, rpe.version, rpe.release,
                   rpa.label as arch, spf.vendor, spf.summary,
                   spf.description
              FROM susePackageProductFile sppf
              JOIN suseProductFile spf ON spf.id = sppf.prodfile_id
              JOIN rhnPackageEvr rpe ON rpe.id = spf.evr_id
              JOIN rhnPackageArch rpa ON rpa.id = spf.package_arch_id
             WHERE sppf.package_id = :package_id
        """)
        h.execute(package_id=self._row['id'])
        arr.append(_SuseProductFilesDumper(self._writer, data_iterator=h))

        # SUSE EULAs
        h = rhnSQL.prepare("""
            SELECT se.text, se.checksum
              FROM suseEula se
              JOIN susePackageEula spe ON se.id = spe.eula_id
             WHERE spe.package_id = :package_id
        """)
        h.execute(package_id=self._row['id'])
        arr.append(_SuseEulasDumper(self._writer, data_iterator=h))

        # Debian Extra Tags
        h = rhnSQL.prepare("""
            SELECT k.name, e.value
              FROM rhnPackageExtraTag e
              JOIN rhnPackageExtraTagKey k ON e.key_id = k.id
             WHERE e.package_id = :package_id
        """)
        h.execute(package_id=self._row['id'])
        arr.append(_PkgExtraTagDumper(self._writer, data_iterator=h))

        return ArrayIterator(arr)


class PackagesDumper(BaseSubelementDumper, BaseQueryDumper):
    tag_name = 'rhn-packages'
    subelement_dumper_class = _PackageDumper

    def set_iterator(self):
        return BaseQueryDumper.set_iterator(self)

##


class ShortPackageEntryDumper(BaseChecksumRowDumper):
    tag_name = 'rhn-package-short'

    def set_attributes(self):
        attr = {
            'id': "rhn-package-%s" % self._row['id'],
            'name': self._row['name'],
            'version': self._row['version'],
            'release': self._row['release'],
            'epoch': self._row['epoch'] or "",
            'package-arch': self._row['package_arch'],
            'package-size': self._row['package_size'],
            'last-modified': _dbtime2timestamp(self._row['last_modified']),
            'org-id': self._row['org_id'] or "",
        }
        if self._row['checksum_type'] == 'md5':
            # compatibility with older satellite
            attr['md5sum'] = self._row['checksum']
        return attr


class ShortPackagesDumper(BaseSubelementDumper, BaseQueryDumper):
    tag_name = 'rhn-packages-short'
    subelement_dumper_class = ShortPackageEntryDumper

    def set_iterator(self):
        return BaseQueryDumper.set_iterator(self)

##


class SourcePackagesDumper(BaseQueryDumper):
    tag_name = 'rhn-source-packages'

    def dump_subelement(self, data):
        attributes = {}
        attrs = [
            "id", "source_rpm", "package_group", "rpm_version",
            "payload_size", "build_host", "sigchecksum_type", "sigchecksum", "vendor",
            "cookie", "package_size", "checksum_type", "checksum"
        ]
        for attr in attrs:
            attributes[attr.replace('_', '-')] = data[attr]
        attributes['id'] = "rhn-source-package-%s" % data['id']
        attributes['build-time'] = _dbtime2timestamp(data['build_time'])
        attributes['last-modified'] = _dbtime2timestamp(data['last_modified'])
        d = EmptyDumper(self._writer, 'rhn-source-package',
                        attributes=attributes)
        d.dump()

##


class _ChecksumDumper(BaseDumper):
    tag_name = 'checksums'

    def dump_subelement(self, data):
        c = EmptyDumper(self._writer, 'checksum', attributes={
            'type': data['type'],
            'value': data['value'],
        })
        c.dump()

##


class _ChangelogEntryDumper(BaseRowDumper):
    tag_name = 'rhn-package-changelog-entry'

    def set_iterator(self):
        arr = []
        mappings = [
            ('rhn-package-changelog-entry-name', 'name'),
            ('rhn-package-changelog-entry-text', 'text'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))

        arr.append(SimpleDumper(self._writer, 'rhn-package-changelog-entry-time',
                                _dbtime2timestamp(self._row['time'])))

        return ArrayIterator(arr)


class _ChangelogDumper(BaseSubelementDumper):
    tag_name = 'rhn-package-changelog'
    subelement_dumper_class = _ChangelogEntryDumper

##
class _SuseProductEntryDumper(BaseRowDumper):
    tag_name = 'suse-product-file-entry'

    def set_iterator(self):
        arr = []
        mappings = [
            ('suse-product-file-entry-name', 'name'),
            ('suse-product-file-entry-epoch', 'epoch'),
            ('suse-product-file-entry-version', 'version'),
            ('suse-product-file-entry-release', 'release'),
            ('suse-product-file-entry-arch', 'arch'),
            ('suse-product-file-entry-vendor', 'vendor'),
            ('suse-product-file-entry-summary', 'summary'),
            ('suse-product-file-entry-description', 'description'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))
        return ArrayIterator(arr)

class _SuseProductFilesDumper(BaseSubelementDumper):
    tag_name = 'suse-product-file'
    subelement_dumper_class = _SuseProductEntryDumper

##
class _SuseEulasEntryDumper(BaseRowDumper):
    tag_name = 'suse-eula-entry'

    def set_iterator(self):
        arr = []
        mappings = [
            ('suse-eula-entry-text', 'text'),
            ('suse-eula-entry-checksum', 'checksum'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))
        return ArrayIterator(arr)

class _SuseEulasDumper(BaseSubelementDumper):
    tag_name = 'suse-eula'
    subelement_dumper_class = _SuseEulasEntryDumper

##
class _PkgExtraTagEntryDumper(BaseRowDumper):
    tag_name = 'pkg-extratag-entry'

    def set_iterator(self):
        arr = []
        mappings = [
            ('pkg-extratag-entry-name', 'name'),
            ('pkg-extratag-entry-value', 'value'),
        ]
        for k, v in mappings:
            arr.append(SimpleDumper(self._writer, k, self._row.get(v)))
        return ArrayIterator(arr)

class _PkgExtraTagDumper(BaseSubelementDumper):
    tag_name = 'pkg-extratag'
    subelement_dumper_class = _PkgExtraTagEntryDumper


##
class _DependencyDumper(BaseDumper):

    def __init__(self, writer, data_iterator, container_name, entry_name):
        self.tag_name = container_name
        self.entry_name = entry_name
        BaseDumper.__init__(self, writer, data_iterator=data_iterator)

    def dump_subelement(self, data):
        d = EmptyDumper(self._writer, self.entry_name, attributes={
            'name': data['name'],
            'version': data['version'] or "",
            'sense': data['sense'],
        })
        d.dump()

# Files


class _PackageFilesDumper(BaseDumper):
    tag_name = 'rhn-package-files'

    def dump_subelement(self, data):
        data['mtime'] = _dbtime2timestamp(data['mtime'])
        data['checksum-type'] = data['checksum-type'] or ""
        data['checksum'] = data['checksum'] or ""
        if data['checksum-type'] in ('md5', ''):
            # generate md5="..." attribute
            # for compatibility with older satellites
            data['md5'] = data['checksum']
        data['linkto'] = data['linkto'] or ""
        data['lang'] = data['lang'] or ""
        d = EmptyDumper(self._writer, 'rhn-package-file',
                        attributes=data)
        d.dump()

# Errata


class _ErratumDumper(BaseRowDumper):
    tag_name = 'rhn-erratum'

    def set_attributes(self):
        h = rhnSQL.prepare("""
            select c.label
            from rhnChannelErrata ec, rhnChannel c
            where ec.channel_id = c.id
            and ec.errata_id = :errata_id
        """)
        h.execute(errata_id=self._row['id'])
        channels = [x['label'] for x in h.fetchall_dict() or []]

        h = rhnSQL.prepare("""
            select ep.package_id
            from rhnErrataPackage ep
            where ep.errata_id = :errata_id
        """)
        h.execute(errata_id=self._row['id'])
        packages = ["rhn-package-%s" % x['package_id'] for x in
                    h.fetchall_dict() or []]

        h = rhnSQL.prepare("""
            select c.name cve
            from rhnErrataCVE ec, rhnCVE c
            where ec.errata_id = :errata_id
            and ec.cve_id = c.id
        """)
        h.execute(errata_id=self._row['id'])
        cves = [x['cve'] for x in h.fetchall_dict() or []]

        return {
            'id': 'rhn-erratum-%s' % self._row['id'],
            'org_id': self._row['org_id'] or "",
            'advisory': self._row['advisory'],
            'channels': ' '.join(channels),
            'packages': ' '.join(packages),
            'cve-names': ' '.join(cves),
        }

    type_id_column = ""

    def set_iterator(self):
        arr = []

        mappings = [
            ('rhn-erratum-advisory-name', 'advisory_name', 100),
            ('rhn-erratum-advisory-rel', 'advisory_rel', 32),
            ('rhn-erratum-advisory-type', 'advisory_type', 32),
            ('rhn-erratum-advisory-status', 'advisory_status', 32),
            ('rhn-erratum-product', 'product', 64),
            ('rhn-erratum-description', 'description', 4000),
            ('rhn-erratum-synopsis', 'synopsis', 4000),
            ('rhn-erratum-topic', 'topic', 4000),
            ('rhn-erratum-solution', 'solution', 4000),
            ('rhn-erratum-refers-to', 'refers_to', 4000),
            ('rhn-erratum-notes', 'notes', 4000),
            ('rhn-erratum-errata-from', 'errata_from', 127),
            ('rhn-erratum-severity', 'severity_id', 127)
        ]
        for k, v, b in mappings:
            value = self._row.get(v) if self._row.get(v) is not None else ""
            arr.append(SimpleDumper(self._writer, k, value, b))
        arr.append(SimpleDumper(self._writer, 'rhn-erratum-issue-date',
                                _dbtime2timestamp(self._row['issue_date'])))
        arr.append(SimpleDumper(self._writer, 'rhn-erratum-update-date',
                                _dbtime2timestamp(self._row['update_date'])))
        arr.append(SimpleDumper(self._writer, 'rhn-erratum-last-modified',
                                _dbtime2timestamp(self._row['last_modified'])))


        h = rhnSQL.prepare("""
            select label
            from rhnErrataSeverity
            where id = :severity_id
        """)
        h.execute(severity_id=self._row['severity_id'])
        sev = h.fetchone_dict() or None
        if sev:
            arr.append(SimpleDumper(self._writer, 'rhn-erratum-severity',
                sev['label']))
        else:
            arr.append(SimpleDumper(self._writer, 'rhn-erratum-severity', ''))

        h = rhnSQL.prepare("""
            select keyword
            from rhnErrataKeyword
            where errata_id = :errata_id
        """)
        h.execute(errata_id=self._row['id'])
        arr.append(_ErratumKeywordDumper(self._writer, data_iterator=h))

        h = rhnSQL.prepare("""
            select bug_id, summary, href
            from rhnErrataBuglist
            where errata_id = :errata_id
        """)
        h.execute(errata_id=self._row['id'])
        arr.append(_ErratumBuglistDumper(self._writer, data_iterator=h))
        _query_errata_file_info = """
             select ef.id errata_file_id, c.checksum_type, c.checksum,
                    ef.filename, eft.label as type,
                    efp.package_id, efps.package_id as source_package_id
               from rhnErrataFile ef left outer join rhnErrataFilePackage efp on ef.id = efp.errata_file_id
                    left outer join rhnErrataFilePackageSource efps on ef.id = efps.errata_file_id,
                    rhnErrataFileType eft, rhnChecksumView c
              where ef.errata_id = :errata_id
                and ef.type = eft.id
                and ef.checksum_id = c.id
                %s
        """
        h = rhnSQL.prepare(_query_errata_file_info % self.type_id_column)
        h.execute(errata_id=self._row['id'])
        arr.append(_ErratumFilesDumper(self._writer, data_iterator=h))

        return ArrayIterator(arr)


class ErrataDumper(BaseSubelementDumper):
    tag_name = 'rhn-errata'
    subelement_dumper_class = _ErratumDumper

    def set_iterator(self):
        if self._iterator:
            return self._iterator
        raise NotImplementedError("To be overridden in a child class")


class _ErratumKeywordDumper(BaseDumper):
    tag_name = 'rhn-erratum-keywords'

    def dump_subelement(self, data):
        d = SimpleDumper(self._writer, 'rhn-erratum-keyword', data['keyword'])
        d.dump()


class _ErratumBugDumper(BaseRowDumper):
    tag_name = 'rhn-erratum-bug'

    def set_iterator(self):
        arr = [
            SimpleDumper(self._writer, 'rhn-erratum-bug-id', self._row['bug_id']),
            SimpleDumper(self._writer, 'rhn-erratum-bug-summary',
                         self._row['summary'] or ""),
            SimpleDumper(self._writer, 'rhn-erratum-bug-href', self._row['href']),
        ]
        return ArrayIterator(arr)


class _ErratumBuglistDumper(BaseSubelementDumper):
    tag_name = 'rhn-erratum-bugs'
    subelement_dumper_class = _ErratumBugDumper


class _ErratumFileEntryDumper(BaseChecksumRowDumper):
    tag_name = 'rhn-erratum-file'

    def set_attributes(self):
        attributes = {
            'filename': self._row['filename'][:4000],
            'type': self._row['type'],
        }
        if self._row['checksum_type'] == 'md5':
            attributes['md5sum'] = self._row['checksum']

        # Compute the channels for this file
        h = rhnSQL.prepare("""
            select c.label
            from rhnErrataFileChannel efc, rhnChannel c
            where efc.errata_file_id = :errata_file_id
            and efc.channel_id = c.id
        """)
        h.execute(errata_file_id=self._row['errata_file_id'])
        channels = ' '.join(
            [x['label'] for x in h.fetchall_dict() or []])
        if channels:
            attributes['channels'] = channels

        # Get the package id or source_package_id
        if self._row['type'] == 'RPM':
            package_id = self._row['package_id']
            if package_id is not None:
                attributes['package'] = 'rhn-package-%s' % package_id
        elif self._row['type'] == 'SRPM':
            package_id = self._row['source_package_id']
            if package_id is not None:
                attributes['source-package'] = 'rhn-package-source-%s' % package_id
        return attributes


class _ErratumFilesDumper(BaseSubelementDumper):
    tag_name = 'rhn-erratum-files'
    subelement_dumper_class = _ErratumFileEntryDumper

# Arches


class BaseArchesDumper(BaseDumper):
    table_name = 'foo'
    subelement_tag = 'foo'

    def set_iterator(self):
        h = rhnSQL.prepare("""
            select id, label, name
            from %s
        """ % self.table_name)
        h.execute()
        return h

    def dump_subelement(self, data):
        attributes = {
            'id': "%s-id-%s" % (self.subelement_tag, data['id']),
            'label': data['label'],
            'name': data['name'],
        }
        EmptyDumper(self._writer, self.subelement_tag, attributes).dump()


class RestrictedArchesDumper(BaseArchesDumper):

    def __init__(self, writer, data_iterator=None, rpm_arch_type_only=0):
        BaseArchesDumper.__init__(self, writer=writer,
                                  data_iterator=data_iterator)
        self.rpm_arch_type_only = rpm_arch_type_only

    def set_iterator(self):
        query_templ = """
            select aa.id, aa.label, aa.name,
                   at.label arch_type_label, at.name arch_type_name
              from %s aa,
                   rhnArchType at
             where aa.arch_type_id = at.id
               %s
        """
        if self.rpm_arch_type_only:
            h = rhnSQL.prepare(query_templ % (self.table_name, "and at.label = 'rpm'"))
        else:
            h = rhnSQL.prepare(query_templ % (self.table_name, ""))
        h.execute()
        return h

    def dump_subelement(self, data):
        attributes = {
            'id': "%s-id-%s" % (self.subelement_tag, data['id']),
            'label': data['label'],
            'name': data['name'],
            'arch-type-label': data['arch_type_label'],
            'arch-type-name': data['arch_type_name'],
        }
        EmptyDumper(self._writer, self.subelement_tag, attributes).dump()


class ChannelArchesDumper(RestrictedArchesDumper):
    tag_name = 'rhn-channel-arches'
    subelement_tag = 'rhn-channel-arch'
    table_name = 'rhnChannelArch'


class PackageArchesDumper(RestrictedArchesDumper):
    tag_name = 'rhn-package-arches'
    subelement_tag = 'rhn-package-arch'
    table_name = 'rhnPackageArch'


class ServerArchesDumper(RestrictedArchesDumper):
    tag_name = 'rhn-server-arches'
    subelement_tag = 'rhn-server-arch'
    table_name = 'rhnServerArch'


class CPUArchesDumper(BaseArchesDumper):
    tag_name = 'rhn-cpu-arches'
    subelement_tag = 'rhn-cpu-arch'
    table_name = 'rhnCPUArch'


class RestrictedArchCompatDumper(BaseArchesDumper):
    _query_rpm_arch_type_only = ""
    _query_arch_type_all = ""
    _subelement_tag = ""

    def __init__(self, writer, data_iterator=None, rpm_arch_type_only=0, virt_filter=0):
        BaseArchesDumper.__init__(self, writer=writer,
                                  data_iterator=data_iterator)
        self.rpm_arch_type_only = rpm_arch_type_only
        self.virt_filter = virt_filter

    def set_iterator(self):
        _virt_filter_sql = ""
        if self.virt_filter:
            _virt_filter_sql = """and sgt.label not like 'virt%'"""

        if self._subelement_tag == 'rhn-server-group-server-arch-compat':
            if self.rpm_arch_type_only:
                h = rhnSQL.prepare(self._query_rpm_arch_type_only % _virt_filter_sql)
            else:
                h = rhnSQL.prepare(self._query_arch_type_all % _virt_filter_sql)
        else:
            if self.rpm_arch_type_only:
                h = rhnSQL.prepare(self._query_rpm_arch_type_only)
            else:
                h = rhnSQL.prepare(self._query_arch_type_all)

        h.execute()
        return h

    def dump_subelement(self, data):
        EmptyDumper(self._writer, self._subelement_tag, data).dump()


class ServerPackageArchCompatDumper(RestrictedArchCompatDumper):
    tag_name = 'rhn-server-package-arch-compatibility-map'
    _subelement_tag = 'rhn-server-package-arch-compat'

    _query_rpm_arch_type_only = rhnSQL.Statement("""
        select sa.label "server-arch",
            pa.label "package-arch",
            spac.preference
        from rhnServerPackageArchCompat spac,
            rhnServerArch sa,
            rhnPackageArch pa,
            rhnArchType aas,
            rhnArchType aap
        where spac.server_arch_id = sa.id
        and spac.package_arch_id = pa.id
        and sa.arch_type_id = aas.id
        and aas.label = 'rpm'
        and pa.arch_type_id = aap.id
        and aap.label = 'rpm'
    """)

    _query_arch_type_all = rhnSQL.Statement("""
        select sa.label "server-arch",
            pa.label "package-arch",
            spac.preference
        from rhnServerPackageArchCompat spac,
            rhnServerArch sa,
            rhnPackageArch pa
        where spac.server_arch_id = sa.id
        and spac.package_arch_id = pa.id
    """)


class ServerChannelArchCompatDumper(RestrictedArchCompatDumper):
    tag_name = 'rhn-server-channel-arch-compatibility-map'
    _subelement_tag = 'rhn-server-channel-arch-compat'

    _query_rpm_arch_type_only = rhnSQL.Statement("""
        select sa.label "server-arch",
               ca.label "channel-arch"
          from rhnServerChannelArchCompat scac,
               rhnServerArch sa,
               rhnChannelArch ca,
               rhnArchType aas,
               rhnArchType aac
         where scac.server_arch_id = sa.id
           and scac.channel_arch_id = ca.id
           and sa.arch_type_id = aas.id
           and aas.label = 'rpm'
           and ca.arch_type_id = aac.id
           and aac.label = 'rpm'
    """)

    _query_arch_type_all = rhnSQL.Statement("""
        select sa.label "server-arch",
               ca.label "channel-arch"
          from rhnServerChannelArchCompat scac,
               rhnServerArch sa,
               rhnChannelArch ca
         where scac.server_arch_id = sa.id
           and scac.channel_arch_id = ca.id
    """)


class ChannelPackageArchCompatDumper(RestrictedArchCompatDumper):
    tag_name = 'rhn-channel-package-arch-compatibility-map'
    _subelement_tag = 'rhn-channel-package-arch-compat'

    _query_rpm_arch_type_only = rhnSQL.Statement("""
        select ca.label "channel-arch",
               pa.label "package-arch"
          from rhnChannelPackageArchCompat cpac,
               rhnChannelArch ca,
               rhnPackageArch pa,
               rhnArchType aac,
               rhnArchType aap
         where cpac.channel_arch_id = ca.id
           and cpac.package_arch_id = pa.id
           and ca.arch_type_id = aac.id
           and aac.label = 'rpm'
           and pa.arch_type_id = aap.id
           and aap.label = 'rpm'
    """)

    _query_arch_type_all = rhnSQL.Statement("""
        select ca.label "channel-arch",
               pa.label "package-arch"
          from rhnChannelPackageArchCompat cpac,
               rhnChannelArch ca,
               rhnPackageArch pa
         where cpac.channel_arch_id = ca.id
           and cpac.package_arch_id = pa.id
    """)


class ServerGroupTypeServerArchCompatDumper(RestrictedArchCompatDumper):
    tag_name = 'rhn-server-group-server-arch-compatibility-map'
    _subelement_tag = 'rhn-server-group-server-arch-compat'

    _query_rpm_arch_type_only = """
        select sgt.label "server-group-type",
               sa.label "server-arch"
          from rhnServerGroupType sgt,
               rhnServerArch sa,
               rhnArchType aas,
               rhnServerServerGroupArchCompat ssgac
         where ssgac.server_arch_id = sa.id
           and sa.arch_type_id = aas.id
           and aas.label = 'rpm'
           and ssgac.server_group_type = sgt.id
           %s
    """

    #_query_arch_type_all = rhnSQL.Statement("""
    _query_arch_type_all = """
        select sgt.label "server-group-type",
               sa.label "server-arch"
          from rhnServerGroupType sgt,
               rhnServerArch sa,
               rhnServerServerGroupArchCompat ssgac
         where ssgac.server_arch_id = sa.id
           and ssgac.server_group_type = sgt.id
           %s
    """


class BlacklistObsoletesDumper(BaseDumper):
    tag_name = 'rhn-blacklist-obsoletes'

    def dump(self):
        note = """\n<!-- This file is intentionally left empty.
     Older Satellites and Spacewalks require this file to exist in the dump. -->\n"""
        self._writer.stream.write(note)
        self._writer.empty_tag(self.tag_name)


class _KickstartableTreeDumper(BaseRowDumper):
    tag_name = 'rhn-kickstartable-tree'

    def set_attributes(self):
        row_dict = self._row.copy()
        del row_dict['id']
        last_modified = row_dict['last-modified']
        row_dict['last-modified'] = _dbtime2timestamp(last_modified)
        return row_dict

    def set_iterator(self):
        kstree_id = self._row['id']
        h = rhnSQL.prepare("""
            select relative_filename,
                   c.checksum_type,
                   c.checksum,
                   file_size,
                    TO_CHAR(last_modified, 'YYYYMMDDHH24MISS') "last-modified"
              from rhnKSTreeFile, rhnChecksumView c
             where kstree_id = :kstree_id
               and checksum_id = c.id
        """)
        h.execute(kstree_id=kstree_id)
        return ArrayIterator([_KickstartFilesDumper(self._writer, h)])


class KickstartableTreesDumper(BaseSubelementDumper, BaseQueryDumper):
    tag_name = 'rhn-kickstartable-trees'
    subelement_dumper_class = _KickstartableTreeDumper
    iterator_query = """
            select kt.id,
                   c.label channel,
                   kt.base_path "base-path",
                   kt.label,
                   kt.boot_image "boot-image",
                   ktt.name "kstree-type-name",
                   ktt.label "kstree-type-label",
                   kit.name "install-type-name",
                   kit.label "install-type-label",
                   TO_CHAR(kt.last_modified, 'YYYYMMDDHH24MISS') "last-modified"
              from rhnKickstartableTree kt,
                   rhnKSTreeType ktt,
                   rhnKSInstallType kit,
                   rhnChannel c
             where kt.channel_id = c.id
               and ktt.id = kt.kstree_type
               and kit.id = kt.install_type
               and kt.org_id is NULL
        """

    def set_iterator(self):
        return BaseQueryDumper.set_iterator(self)


class _KickstartFileEntryDumper(BaseChecksumRowDumper):
    tag_name = 'rhn-kickstart-file'

    def set_attributes(self):
        attr = {
            'relative-path': self._row['relative_filename'],
            'file-size': self._row['file_size'],
            'last-modified': _dbtime2timestamp(self._row['last-modified']),
        }
        if self._row['checksum_type'] == 'md5':
            attr['md5sum'] = self._row['checksum']
        return attr


class _KickstartFilesDumper(BaseSubelementDumper):
    tag_name = 'rhn-kickstart-files'
    subelement_dumper_class = _KickstartFileEntryDumper


def _dbtime2timestamp(val):
    return int(rhnLib.timestamp(val))


class ProductNamesDumper(BaseDumper):
    tag_name = "rhn-product-names"

    def dump_subelement(self, data):
        EmptyDumper(self._writer, 'rhn-product-name', data).dump()
