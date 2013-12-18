# -*- coding: utf-8 -*-
#
# Copyright (C) 2009, 2010, 2011, 2012 Novell, Inc.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

import socket
import sys
import time
import os
import io
import re
import xml.etree.ElementTree as etree
from datetime import date
from urlparse import urlparse, urljoin
from xml.parsers.expat import ExpatError

from spacewalk.server import rhnSQL, taskomatic
from spacewalk.common import rhnLog, suseLib
from spacewalk.common.rhnConfig import initCFG, CFG
from spacewalk.common.rhnLog import log_debug, log_error

from spacewalk.susemanager.simpleproduct import SimpleProduct, create_product_ident

CHANNELS = '/usr/share/susemanager/channels.xml'
CHANNEL_FAMILIES = '/usr/share/susemanager/channel_families.xml'
UPGRADE_PATHS = '/usr/share/susemanager/upgrade_paths.xml'

DEFAULT_LOG_LOCATION = '/var/log/rhn/'

MASTER_CACHE_LOCATION = '/var/cache/rhn/ncc-data/'

INFINITE = 200000 # a very big number that we use for unlimited subscriptions

# location of proxy credentials in yast-generated config
YAST_PROXY = "/root/.curlrc"

def memoize(function):
    """Basic function memoizer"""
    cache = {}
    def decorated_function(*args):
        if args in cache:
            return cache[args]
        else:
            val = function(*args)
            cache[args] = val
            return val
    return decorated_function

class ChannelNotMirrorable(Exception): pass

class NCCSync(object):
    """This class is used to sync SUSE Manager Channels and NCC repositories"""

    def __init__(self, quiet=False, debug=-1, fromdir=None):
        """Setup configuration"""
        self.quiet = quiet
        self.debug = debug
        self.reset_ent_value = 10
        self.is_iss_master = False
        self.is_iss_slave = False

        if fromdir is not None:
            fdir = os.path.abspath(fromdir)
            if not os.path.isdir(fdir):
                sys.stderr.write("'%s' is not a directory\n" % fdir)
                sys.exit(1)
            fromdir = urljoin('file://', fdir)
        self.fromdir = fromdir

        self.ncc_rhn_ent_mapping = {
            "SM_ENT_MON_S"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_S"      : [ "provisioning_entitled" ],
            "SM_ENT_MGM_S"       : [ "enterprise_entitled", "bootstrap_entitled" ],
            "SM_ENT_MGM_V"       : [ "virtualization_host_platform", "enterprise_entitled", "bootstrap_entitled" ],
            "SM_ENT_MON_V"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_V"      : [ "provisioning_entitled" ],
            "SM_ENT_MON_Z"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_Z"      : [ "provisioning_entitled" ],
            "SM_ENT_MGM_Z"       : [ "enterprise_entitled", "bootstrap_entitled" ],
        }

        initCFG("server.susemanager")
        if self.debug == -1:
            self.debug = CFG.DEBUG

        if self.debug > 1:
            rhnLog.initLOG(DEFAULT_LOG_LOCATION + 'mgr-ncc-sync.log', self.debug)
        else:
            rhnLog.initLOG(DEFAULT_LOG_LOCATION + 'mgr-ncc-sync.log')

        self.log_msg("\nStarted: %s" % (time.asctime(time.localtime())))

        self.smtguid  = suseLib.getProductProfile()['guid']
        self.authuser = CFG.mirrcred_user
        self.authpass = CFG.mirrcred_pass
        if not self.authuser or not self.authpass:
            raise Exception("Could not read mirror credentials. Please make "
                            "sure that server.susemanager.mirrcred_user and "
                            "server.susemanager.mirrcred_pass are set correctly "
                            "in the configuration file.")

        try:
            rhnSQL.initDB()
            rhnSQL.clear_log_id()
            rhnSQL.set_log_auth_login('SETUP')
        except rhnSQL.SQLConnectError, e:
            self.error_msg("Could not connect to the database. %s" % e)
            sys.exit(1)

        if CFG.disable_iss == 0 and suseLib.hasISSSlaves():
            self.is_iss_master = True
            if not os.path.exists(MASTER_CACHE_LOCATION):
                os.mkdir(MASTER_CACHE_LOCATION)

        if CFG.iss_parent:
            self.is_iss_slave = True

        self.namespace = "http://www.novell.com/xml/center/regsvc-1_0"

        if self.fromdir:
            fdir = self.fromdir[7:] # strip the file://
            self.ncc_url_prods = self.fromdir + "/productdata.xml"
            if not os.path.isfile(fdir + "/productdata.xml"):
                sys.stderr.write("productdata.xml not found in fromdir\n")
                sys.exit(1)
            self.ncc_url_subs  = self.fromdir + "/listsubscriptions.xml"
            if not os.path.isfile(fdir + "/listsubscriptions.xml"):
                sys.stderr.write("listsubscriptions.xml not found in fromdir\n")
                sys.exit(1)
            self.ncc_repoindex = self.fromdir + "/repo/repoindex.xml"
            if not os.path.isfile(fdir + "/repo/repoindex.xml"):
                sys.stderr.write("repo/repoindex.xml not found in fromdir\n")
                sys.exit(1)
            self.subs_req = None
            self.prod_req = None
        elif self.is_iss_slave:
            self.ncc_url_prods = 'https://%s/center/regsvc/?command=regdata&lang=en-US&version=1.0' % CFG.iss_parent
            self.ncc_url_subs  = "https://%s/center/regsvc/?command=listsubscriptions&lang=en-US&version=1.0" % CFG.iss_parent
            self.ncc_repoindex = None
            # XML documents which are used in POST requests to the NCC
            self.subs_req = ('<?xml version="1.0" encoding="UTF-8"?>'
                             '<listsubscriptions xmlns="%(namespace)s" '
                             'client_version="1.2.3" lang="en">'
                             '<authuser>%(authuser)s</authuser>'
                             '<authpass>%(authpass)s</authpass>'
                             '<smtguid>%(smtguid)s</smtguid>'
                             '</listsubscriptions>\n')
            self.prod_req = ('<?xml version="1.0" encoding="UTF-8"?>'
                             '<productdata xmlns="%(namespace)s" '
                             'client_version="1.2.3" lang="en">'
                             '<authuser>%(authuser)s</authuser>'
                             '<authpass>%(authpass)s</authpass>'
                             '<smtguid>%(smtguid)s</smtguid>'
                             '</productdata>\n')
        else:
            self.ncc_url_prods = "%s/?command=regdata&lang=en-US&version=1.0" % CFG.reg_url
            self.ncc_url_subs  = "%s/?command=listsubscriptions&lang=en-US&version=1.0" % CFG.reg_url
            self.ncc_repoindex = "https://%(authuser)s:%(authpass)s@nu.novell.com/repo/repoindex.xml"
            # XML documents which are used in POST requests to the NCC
            self.subs_req = ('<?xml version="1.0" encoding="UTF-8"?>'
                             '<listsubscriptions xmlns="%(namespace)s" '
                             'client_version="1.2.3" lang="en">'
                             '<authuser>%(authuser)s</authuser>'
                             '<authpass>%(authpass)s</authpass>'
                             '<smtguid>%(smtguid)s</smtguid>'
                             '</listsubscriptions>\n')
            self.prod_req = ('<?xml version="1.0" encoding="UTF-8"?>'
                             '<productdata xmlns="%(namespace)s" '
                             'client_version="1.2.3" lang="en">'
                             '<authuser>%(authuser)s</authuser>'
                             '<authpass>%(authpass)s</authpass>'
                             '<smtguid>%(smtguid)s</smtguid>'
                             '</productdata>\n')
        if not os.path.exists(MASTER_CACHE_LOCATION):
            os.makedirs(MASTER_CACHE_LOCATION)
        self.connect_retries = 10


    def dump_to(self, path):
        """Dump NCC XML data about subscriptions, products and repos

        For each available credential, a new directory is created under
        :path: and the following three files are downloaded from NCC
        into that directory: listsubscriptions.xml, productdata.xml,
        repoindex.xml.

        :arg path: the destination directory path (will be created if it
        does not exist)

        """
        if not os.path.exists(path):
            os.makedirs(path)
        if not os.path.isdir(path):
            self.error_msg("'%s' is not a directory." % path)
            sys.exit(1)

        for user_id in range(len(suseLib.get_mirror_credentials())):
            os.makedirs('%s/%s' % (path, user_id))

        self.print_msg("Downloading Subscription information...")
        for user, subs in self._multi_get_ncc(self.ncc_url_subs, self.subs_req):
            sub_path = '%s/%s/listsubscriptions.xml' % (path, user)
            with io.FileIO(sub_path, 'w') as f:
                f.write(subs.read())

        self.print_msg("Downloading Product information...")
        for user, prds in self._multi_get_ncc(self.ncc_url_prods, self.prod_req):
            prod_path = '%s/%s/productdata.xml' % (path, user)
            with io.FileIO(prod_path, 'w') as f:
                f.write(prds.read())

        for user, idx in self._multi_get_ncc(self.ncc_repoindex):
            idx_path = '%s/%s/repoindex.xml' % (path, user)
            with io.FileIO(idx_path, 'w') as f:
                f.write(idx.read())

    def _get_ncc(self, url, send=None):
        """Connect to NCC and return a file descriptor of an XML document

        :arg url: the url where the request will be sent
        :kwarg send: do a POST request when "send" is given.

        """
        try:
            return suseLib.send(url, send)
        except:
            self.error_msg("NCC connection failed.")
            sys.exit(1)

    def _parse_ncc_xml(self, xml):
        """Parse an NCC XML document returning the root xml.ElementTree object"""
        try:
            tree = etree.parse(xml)
        except ExpatError:
            self.error_msg("Could not parse XML. The remote document "
                           "does not appear to be a valid XML document. "
                           "This document was written to the logfile: %s." %
                           (rhnLog.LOG.file))
            xml.seek(0)
            log_error("Invalid XML document (got ExpatError): %s" %
                      (xml.read()))
            sys.exit(1)

        return tree.getroot()

    def _multi_get_ncc(self, url, send=None):
        """Returns a list of (user_id, document) tuples

        Returns:
        :user_id: string
        :document: file descriptor of an XML document from NCC

        """
        if not url:
            return []

        # trivial case: no credentials are used
        if self.fromdir:
            return [('0', self._get_ncc(url, send))]

        # other case: iterate through the list of credentials and return
        # a list of tuples (authuser, xml document)
        creds = suseLib.get_mirror_credentials()
        xml_list = []

        for (user_id, (authuser, authpass)) in enumerate(creds):
            user_id = str(user_id)
            self.log_msg(
                "Downloading NCC data from %s using credentials #%s" %
                (url, user_id))

            sub_url = url % {'authuser': authuser, 'authpass': authpass}
            if send:
                sub_send = send % {'authuser': authuser,
                                   'authpass': authpass,
                                   'namespace': self.namespace,
                                   'smtguid': self.smtguid}
            else:
                sub_send = None
            xml_list.append((user_id, self._get_ncc(sub_url, sub_send)))
        return xml_list

    def _multi_get_ncc_xml(self, url, send=None):
        """Returns a list of (user_id, document) tuples

        Returns:
        :user_id: string
        :document: xml.ElementTree object of a parsed XML document from NCC

        """
        xmls = self._multi_get_ncc(url, send)
        return [(user, self._parse_ncc_xml(xml)) for (user, xml) in xmls]

    def get_subscriptions_from_ncc(self):
        """Returns all the subscriptions for this customer.

        This is the format of the returned list of susbscriptions:
        [{'consumed-virtual': '0',
          'productlist': '2380,2502',
          'start-date': '1167782194',
          'end-date': '0',
          'consumed': '1',
          'substatus': 'ACTIVE',
          'subid': '123452cdb3e6f82961cdc0561322423a',
          'nodecount': '0',
          'subname': 'SUSE Linux Enterprise Desktop 10',
          'duration': '0',
          'regcode': 'XXXXX@YYY-SLED-abc2c3def',
          'type': 'FULL',
          'server-class': 'OS',
          'product-class': '7260'},
          { ... },
          ...]

        XXX: This method is tightly coupled with consolidate_subscriptions().

        """
        self.print_msg("Downloading Subscription information...")
        xmls = self._multi_get_ncc_xml(self.ncc_url_subs, self.subs_req)
        cache_sub = None #self._parse_ncc_xml('<?xml version="1.0" encoding="UTF-8"?><productdata xmlns="http://www.novell.com/xml/center/regsvc-1_0"/>')
        subscriptions = []
        # iterate through all the xml documents we got from NCC and then
        # through the individual subscriptions in those documents,
        # adding all the matching subscriptions to a flat list
        for (user_id, xml) in xmls:
            if user_id == '0':
                cache_sub = xml
            for row in xml.findall('{%s}subscription' % self.namespace):
                if self.is_iss_master and int(user_id) > 0:
                    cache_sub.append(row)
                subscription = {}
                for col in row.getchildren():
                    dummy = col.tag.split( '}' )
                    key = dummy[1]
                    subscription[key] = col.text
                subscriptions.append(subscription)

        self.print_msg("Found %d subscriptions using %d mirror credentials."
                       % (len(subscriptions), len(xmls)))

        with io.FileIO(MASTER_CACHE_LOCATION+'subscriptions.xml', 'w') as f:
            f.write(etree.tostring(cache_sub, encoding="UTF-8"))

        return subscriptions

    #   FIXME: not sure about 'consumed' yet. We could calculate:
    #          max_members = nodecount - ( current_members - consumed )
    #          to get a more precise max_members
    def consolidate_subscriptions(self, subs):
        """Return a dictionary with the number of subscriptions for each family.

        :arg subs: a dictionary in the format returned by
        get_subscriptions_from_ncc()

        Returns a dictionary in this format:
        {'SLE-HAE-PPC': {'consumed': 0, 'nodecount': 10 },
         '10040': {'consumed': 0, 'nodecount': INFINITE},
         ... }

        If there are families which have subscriptions in the database,
        but are not in the subscription list from NCC, set their
        nodecount to 0.

        """
        subscription_count = {}

        for s in subs:
            start = float(s["start-date"])
            end   = float(s["end-date"])

            start_date = date.fromtimestamp( start )
            end_date = date.fromtimestamp( end )
            today = date.today()

            # FIXME, some product-classes are comma separated
            prods = s["product-class"].split(",")
            for p in prods:
                if today >= start_date and (end == 0 or today <= end_date) and s["type"] != "PROVISIONAL":
                    # FIXME: for correct counting, remove the "or > 0" (bnc#670551)
                    if s["nodecount"] == "-1" or s["nodecount"] > 0 or True:
                        subscription_count[ p ] = { "consumed" : int(s["consumed"]),
                                                    "nodecount" : INFINITE,
                                                    "start-date" : s["start-date"],
                                                    "end-date"   : s["end-date"] }
                    elif subscription_count.has_key( p ):
                        subscription_count[ p ]["nodecount"] += int(s["nodecount"])
                    else:
                        subscription_count[ p ] = { "consumed"   : int(s["consumed"]),
                                                    "nodecount"  : int(s["nodecount"]),
                                                    "start-date" : s["start-date"],
                                                    "end-date"   : s["end-date"] }

        # get information about unlimited subscriptions from CHANNEL_FAMILIES
        for family in etree.parse(CHANNEL_FAMILIES).getroot():
            if family.get('default_nodecount') == '-1':
                label = family.get('label')
                subscription_count[label] = {'consumed': 0,
                                             'nodecount': INFINITE}

        # delete subscriptions that are no longer available in NCC
        q = rhnSQL.prepare(
            "UPDATE rhnprivatechannelfamily p SET max_members = 0 "
            "WHERE p.channel_family_id IN "
            "(SELECT p.channel_family_id "
            "FROM rhnprivatechannelfamily p JOIN rhnchannelfamily f "
            "ON p.channel_family_id = f.id "
            "WHERE p.max_members > 0 AND f.label NOT IN %s)"
            # XXX rhnSQL fails on SQL IN string substitution, we do it manually
            % sql_list(subscription_count.keys()))
        q.execute()
        rhnSQL.commit()

        return subscription_count

    def get_suse_products_from_ncc(self):
        """Get all the products known by NCC

        Returns a list of dicts with all the keys the NCC has for a product

        """
        self.print_msg("Downloading Product information...")
        xmls = self._multi_get_ncc_xml(self.ncc_url_prods, self.prod_req)

        # we can't index dictionaries, so we'll use a set of
        # PRODUCTDATAIDs to keep the product dictionaries unique in the
        # suse_products list
        suse_products = []
        product_ids = set()
        for (user_id, productdata) in xmls:
            if user_id == '0':
                # always the same for all users. So we need to write it only once.
                with io.FileIO(MASTER_CACHE_LOCATION+'productdata.xml', 'w') as f:
                    f.write(etree.tostring(productdata, encoding="UTF-8"))
            for row in productdata:
                if row.tag == ("{%s}row" % self.namespace):
                    suseProduct = {}
                    for col in row.findall("{%s}col" % self.namespace):
                        key = col.get("name")
                        if key in ["start-date", "end-date"]:
                            suseProduct[key] = float(col.text)
                        else:
                            suseProduct[key] = col.text
                    if (suseProduct["PRODUCT_CLASS"] and
                        suseProduct['PRODUCTDATAID'] not in product_ids):
                        # FIXME: skip buggy NCC entries. Some have no
                        # product_class, which is invalid data
                        suse_products.append(suseProduct)
                        product_ids.add(suseProduct['PRODUCTDATAID'])

        self.print_msg("Found %d products using %d mirror credentials."
                       % (len(suse_products), len(xmls)))

        return suse_products

    def get_arch_id(self, arch):
        """Returns the database id of an package arch"""
        arch_type_id = None

        if arch != None:
            select_sql = "SELECT id from RHNPACKAGEARCH where LABEL = :arch"
            query = rhnSQL.prepare(select_sql)
            query.execute(arch = arch)
            row = query.fetchone_dict() or {}
            if row:
                arch_type_id = row["id"]
        return arch_type_id

    def update_channel_family_table_by_config(self):
        """Insert subscription information for channels that don't have any

        Subscription information is read from the CHANNEL_FAMILIES .xml
        file and is only added into the database if there is no existing
        subscription information about that ChannelFamily.

        """
        self.print_msg("Updating Channel Family Information")
        tree = etree.parse(CHANNEL_FAMILIES)
        for family in tree.getroot():
            name = family.get("name")
            label = family.get("label")
            d_nc  = int(family.get("default_nodecount"))
            if d_nc == -1:
                d_nc = INFINITE
            cf_id = self.edit_channel_family_table(label, name)
            select_sql = ("SELECT max_members, org_id, current_members "
                          "FROM RHNPRIVATECHANNELFAMILY "
                          "WHERE channel_family_id = %s order by org_id" % cf_id)
            query = rhnSQL.prepare(select_sql)
            query.execute()
            result = query.fetchall()

            if len(result) == 0:
                log_debug(1, "no entry for channel family %s in RHNPRIVATECHANNELFAMILY" % cf_id )
                # NCC has a subscription for us that is missing in the DB
                insert_sql = """
                    INSERT INTO RHNPRIVATECHANNELFAMILY
                      (channel_family_id, org_id, max_members, current_members )
                    VALUES
                      ( :cf_id, 1, :max_m, :current_m )
                """
                query = rhnSQL.prepare(insert_sql)
                query.execute(
                    cf_id = cf_id,
                    max_m = d_nc,
                    current_m = 0
                )
                rhnSQL.commit()

    def __get_id_from_product_id(self, product_id):
        select_id = rhnSQL.prepare("""SELECT id FROM suseProducts WHERE product_id = :product_id""")
        select_id.execute(product_id=product_id)
        r = select_id.fetchone_dict() or None
        if r:
            return r['id']
        return None

    def update_upgrade_pathes_by_config(self):
        """Update table suseUpgradePath with values read from XML config

        Upgrade path information are read from UPGRADE_PATHS .xml
        file and the table suseUpgradePath is updated according to it

        """
        self.print_msg("Updating Upgrade Path Information")

        select_sql = ("SELECT from_pdid, to_pdid from suseUpgradePath")
        query = rhnSQL.prepare(select_sql)
        query.execute()
        result = query.fetchall_dict() or []
        pathes = {}
        for row in result:
            key = "%s-%s" % (row['from_pdid'], row['to_pdid'])
            pathes[key] = 1

        tree = etree.parse(UPGRADE_PATHS)
        for upgrade in tree.getroot():
            from_pdid = self.__get_id_from_product_id(int(upgrade.get("from_pdid")))
            to_pdid   = self.__get_id_from_product_id(int(upgrade.get("to_pdid")))
            if not from_pdid or not to_pdid:
                continue

            key = "%s-%s" % (from_pdid, to_pdid)
            if pathes.has_key(key):
                log_debug(1, "found existing entry for upgrade path %s => %s - skip" % (from_pdid, to_pdid))
                del pathes[key]
            else:
                log_debug(1, "no entry for upgrade path %s => %s - adding" % (from_pdid, to_pdid))
                # missing in the DB
                insert_sql = """
                    INSERT INTO suseUpgradePath (from_pdid, to_pdid)
                    VALUES ( :from_pdid, :to_pdid )
                """
                query = rhnSQL.prepare(insert_sql)
                query.execute(
                    from_pdid = from_pdid,
                    to_pdid = to_pdid
                )

        for key in pathes:
            # all entries here needs to be removed
            (from_pdid, to_pdid) = key.split('-', 1)
            log_debug(1, "obsolete entry for upgrade path %s => %s - removing" % (from_pdid, to_pdid))
            delete_sql = """DELETE FROM suseUpgradePath
                            WHERE from_pdid = :from_pdid AND to_pdid = :to_pdid"""
            query = rhnSQL.prepare(delete_sql)
            query.execute(
                from_pdid = from_pdid,
                to_pdid = to_pdid
            )
        rhnSQL.commit()


    def add_channel_family_row(self, label, name=None, org_id=None, url="some url"):
        """Insert a new channel_family row"""
        self.print_msg("Adding Channel %s" % label)
        channel_family_id = self.get_channel_family_id(label)
        if name == None:
            name = label
        if channel_family_id == None:
            insert_sql = """
            INSERT INTO RHNCHANNELFAMILY
                ( id, org_id, name, label, product_url )
            VALUES
                ( sequence_nextval('rhn_channel_family_id_seq'),
                  :org_id, :name, :label, :product_url )
            """
            query = rhnSQL.prepare(insert_sql)
            query.execute(
                org_id = org_id,
                name = name,
                label = label,
                product_url = url
            )
            select_sql = "SELECT id from RHNCHANNELFAMILY where LABEL = :label"
            query = rhnSQL.prepare(select_sql)
            query.execute( label = label )
            row = query.fetchone_dict() or {}
            channel_family_id = row["id"]
            rhnSQL.commit()
        return channel_family_id

    # FIXME: only given values should be updated in DB
    def update_channel_family_table(self, label, name=None,
                                    org_id=1, url="some url"):
        """Update an existing channel_family row"""
        channel_family_id = rhnSQL.Row("RHNCHANNELFAMILY",
                                       "LABEL", label)['id']
        if name == None:
            name = label
        if channel_family_id != None:
            update_sql = """UPDATE RHNCHANNELFAMILY
                            SET name        = :name,
                                label       = :label,
                                org_id      = :org_id,
                                product_url = :product_url
                            WHERE id = :id"""
            query = rhnSQL.prepare(update_sql)
            query.execute( id = channel_family_id,
                           name = name,
                           label = label,
                           org_id = org_id,
                           product_url = url
            )
            rhnSQL.commit()
        return channel_family_id

    def get_channel_family_id(self, label):
        """returns the id of a channel_family or None if not existend"""
        select_sql = "SELECT id from RHNCHANNELFAMILY where LABEL = :label"
        query = rhnSQL.prepare(select_sql)
        query.execute(label=label)
        row = query.fetchone_dict() or {}
        if row:
            return row["id"]
        return None

    def edit_channel_family_table(self, label, name=None,
                                  org_id=None, url="some url" ):
        """Create or update an existing channel family

        Returns the id of the channel_family.

        """
        channel_family_id = self.get_channel_family_id(label)
        if name is None:
            name = label

        if not channel_family_id:
            channel_family_id = self.add_channel_family_row(
                label, name, org_id, url)
        else:
            self.update_channel_family_table(label, name, org_id, url)
        return channel_family_id

    def update_suse_products_table(self, suse_products):
        """Creates/updates entries in the DB Products table

        Expects a get_suse_products_from_ncc() datastructure

        """
        for p in suse_products:
            arch_type_id = self.get_arch_id( p["ARCH"] )
            params = {}

            channel_family_id = self.get_channel_family_id(
                p["PRODUCT_CLASS"])

            # add missing channel family definitions, but not the system entitlements
            if not channel_family_id and p["PRODUCT_CLASS"] not in self.ncc_rhn_ent_mapping:
               channel_family_id = self.add_channel_family_row( p["PRODUCT_CLASS"])

            # NAME+VERSION+RELEASE+ARCH are uniq
            select_sql = """SELECT id from SUSEPRODUCTS
                            where name       = :name """
            if p["PRODUCT"] != None:
                p["PRODUCT"] = p["PRODUCT"].lower()
            if p["VERSION"] != None:
                p["VERSION"] = p["VERSION"].lower()
                select_sql = select_sql + "and version      = :version "
                params["version"] = p["VERSION"]
            if p["REL"] != None:
                p["REL"] = p["REL"].lower()
                select_sql = select_sql + "and release      = :release "
                params["release"] = p["REL"]
            if arch_type_id != None:
                select_sql = select_sql + "and arch_type_id = :arch "
                params["arch"] = arch_type_id

            params["name"] = p["PRODUCT"]
            query = rhnSQL.prepare(select_sql)
            apply(query.execute, (), params)
            row = query.fetchone_dict() or {}
            if row:
                update_sql = """
                    UPDATE SUSEPRODUCTS
                    SET
                      friendly_name     = :friendly_name,
                      channel_family_id = :channel_family_id,
                      product_list      = :product_list
                    WHERE id = :id
                """
                query = rhnSQL.prepare(update_sql)
                query.execute(
                    id = row["id"],
                    friendly_name = p["FRIENDLY"],
                    channel_family_id = channel_family_id,
                    product_list = p["PRODUCT_LIST"]
                )
            else:
                insert_sql = """
                    INSERT INTO SUSEPRODUCTS
                        (id, NAME, VERSION, FRIENDLY_NAME,
                         ARCH_TYPE_ID, RELEASE, CHANNEL_FAMILY_ID,
                         PRODUCT_LIST, PRODUCT_ID)
                    VALUES
                        (sequence_nextval('suse_products_id_seq'),
                         :name, :version, :friendly_name, :arch_type_id,
                         :release, :channel_family_id, :product_list,
                         :product_id)
                """
                query = rhnSQL.prepare(insert_sql)
                if p["PRODUCT"] != None:
                    p["PRODUCT"] = p["PRODUCT"].lower()
                if p["VERSION"] != None:
                    p["VERSION"] = p["VERSION"].lower()
                if p["REL"] != None:
                    p["REL"] = p["REL"].lower()
                query.execute(
                    name = p["PRODUCT"],
                    version = p["VERSION"],
                    friendly_name = p["FRIENDLY"],
                    arch_type_id = arch_type_id,
                    release = p["REL"],
                    channel_family_id = channel_family_id,
                    product_list = p["PRODUCT_LIST"],
                    product_id = p["PRODUCTDATAID"])
        rhnSQL.commit()

    def get_entitlement_id( self, ent ):
        id = None

        if ent != None:
            select_sql = "SELECT id from RHNSERVERGROUPTYPE where LABEL = :ent"
            query = rhnSQL.prepare(select_sql)
            query.execute(ent = ent)
            row = query.fetchone_dict() or {}
            if row:
                id = row["id"]
        return id

    def reset_entitlements_in_table(self):
        """Reset all the entitlements to the our default small value"""

        # Entitlements are stored in the same table as Server Groups (as
        # created from the WebUI). Server Groups should have GROUP_TYPE
        # = NULL. We don't want to touch these.
        query = rhnSQL.prepare("""
            UPDATE RHNSERVERGROUP SET max_members = :val
            WHERE GROUP_TYPE IS NOT NULL""")
        log_debug(2, "SQL: " + query.sql)
        query.execute(val=self.reset_ent_value)

        # this is a backward fix for bnc#740813. We used to wrongly set
        # the max_members of a system group to the "demo" entitlements
        # value.
        query = rhnSQL.prepare("""
            UPDATE RHNSERVERGROUP SET max_members = NULL
            WHERE GROUP_TYPE IS NULL""")
        query.execute()
        rhnSQL.commit()

    def edit_entitlement_in_table( self, prod, data ):
        if self.is_entitlement( prod ):
            for ent in self.ncc_rhn_ent_mapping[prod]:
                id = self.get_entitlement_id( ent )
                available = 0

                start = float(data["start-date"])
                end   = float(data["end-date"])
                start_date = date.fromtimestamp( start )
                end_date = date.fromtimestamp( end )
                today = date.today()

                # expired? leave it alone (keep the reset-value)
                if today > end_date:
                    continue

                # FIXME: setting always to INFINITE if in NCC
                if True or data["nodecount"] > 0:
                    available = INFINITE # unlimited
                else:
                    available = data["nodecount"]

                update_sql = """
                    UPDATE RHNSERVERGROUP SET
                    max_members = :max_m
                    WHERE GROUP_TYPE = :gid
                    """
                query = rhnSQL.prepare(update_sql)
                log_debug(2, "SQL: " % query )
                query.execute(
                    max_m = available,
                    gid   = id
                )
            rhnSQL.commit()

    def do_subscription_calculation(self, all_subs_in_db, all_subs_sum, prod,
                                    data, cf_id):
        # Two things can happen:
        # 1. we have more subscriptions in NCC than in DB
        #    we'll add (substract a negative value) from org_id=1 max_members
        #
        # 2. NCC says we have less subscriptions than we know of in the DB
        #    we have to reduce the max_members of some org's
        #    We'll substract the max_members of org_id=1 until needed_subscriptions=0 or max_members=current_members
        #    We'll substract the max_members of org_id++ until needed_subscriptions=0 or max_members=current_members
        #    We'll reduce the max_members of org_id=1 until needed_subscriptions=0 or max_members=0 (!!!)
        #    We'll reduce the max_members of org_id++ until needed_subscriptions=0 or max_members=0 (!!!)
        needed_subscriptions = all_subs_sum - data["nodecount"]
        log_debug(1, "NCC says: %s, DB says: %s for channel family %s" %
                  (data["nodecount"], all_subs_sum, cf_id))
        for org in sorted(all_subs_in_db.keys()):
            log_debug(1, "working on org_id %s" % org)
            free = all_subs_in_db[ org ]["max_members"] - all_subs_in_db[ org ]["current_members"]
            if (free >= 0 and needed_subscriptions <= free) or (free < 0 and needed_subscriptions < 0):
                log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], needed_subscriptions) )
                all_subs_in_db[ org ]["max_members"] -= needed_subscriptions
                all_subs_in_db[ org ]["dirty"] = True
                needed_subscriptions = 0
                break
            elif free > 0 and needed_subscriptions > free:
                log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], free) )
                needed_subscriptions -= free
                all_subs_in_db[ org ]["max_members"] -= free
                all_subs_in_db[ org ]["dirty"] = True
        if needed_subscriptions > 0:
            # we reduced all max_members but still don't have enough
            # That means, we use more subscriptions than we have in NCC
            self.error_msg("More subscriptions used than registered in NCC. Left subscriptions: %s for family %s" % (needed_subscriptions, cf_id) )
            for org in all_subs_in_db.keys():
                free = all_subs_in_db[ org ]["max_members"]
                if free > 0 and needed_subscriptions <= free:
                    log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], needed_subscriptions) )
                    all_subs_in_db[ org ]["max_members"] -= needed_subscriptions
                    all_subs_in_db[ org ]["dirty"] = True
                    needed_subscriptions = 0
                    break
                elif free > 0 and needed_subscriptions > free:
                    log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], free) )
                    needed_subscriptions -= free
                    all_subs_in_db[ org ]["max_members"] -= free
                    all_subs_in_db[ org ]["dirty"] = True
        if needed_subscriptions > 0:
            self.error_msg("still too many subscripts are in use. No solution found: left subscriptions: %s" % needed_subscriptions)
        return all_subs_in_db

    def test_subscription_calculation( self ):
        test_data = [ { 'all_subs_in_db' : {1: {'max_members': 0, 'current_members': 0, 'dirty': False}},
                        'all_subs_sum' : 0, 'prod' : 'TEST-SLE-HAE-PPC', 'data' : {'nodecount': 10, 'consumed': 0}, 'cf_id' : 1031 },

                      { 'all_subs_in_db' : {1: {'max_members': INFINITE, 'current_members': 0, 'dirty': False}},
                        'all_subs_sum' : INFINITE, 'prod' : 'TEST-10040', 'data' : {'nodecount': INFINITE, 'consumed': 9}, 'cf_id' : 1004 },

                      { 'all_subs_in_db' : {1: {'max_members': 100, 'current_members': 90, 'dirty': False}},
                        'all_subs_sum' : 100, 'prod' : 'TEST-RES', 'data' : {'nodecount': 100, 'consumed': 97}, 'cf_id' : 1005 },

                      { 'all_subs_in_db' : {1: {'max_members': 10, 'current_members': 10, 'dirty': False}},
                        'all_subs_sum' : 20, 'prod' : 'TEST-NAM-AGA', 'data' : {'nodecount': 20, 'consumed': 15}, 'cf_id' : 1019 },

                      { 'all_subs_in_db' : {2: {'max_members': 10, 'current_members': 5, 'dirty': False}},
                        'all_subs_sum' : 20, 'prod' : 'TEST-NAM-AGA', 'data' : {'nodecount': 20, 'consumed': 15}, 'cf_id' : 1019 },

                      { 'all_subs_in_db' : {1: {'max_members': 100, 'current_members': 60, 'dirty': False}},
                        'all_subs_sum' : 100, 'prod' : 'TEST-STUDIOONSITE', 'data' : {'nodecount': 80, 'consumed': 60}, 'cf_id' : 1021 },

                      { 'all_subs_in_db' : {1: {'max_members': 100, 'current_members': 60, 'dirty': False}},
                        'all_subs_sum' : 100, 'prod' : 'TEST-STUDIOONSITE2', 'data' : {'nodecount': 50, 'consumed': 50}, 'cf_id' : 10212 } ]
        for data in test_data:
            all_subs_in_db = self.do_subscription_calculation( data["all_subs_in_db"], data["all_subs_sum"], data["prod"], data["data"], data["cf_id"] )
            for org in all_subs_in_db.keys():
                if all_subs_in_db[ org ]["dirty"]:
                    update_sql = "UPDATE RHNPRIVATECHANNELFAMILY SET "
                    update_sql += "max_members = %s " % all_subs_in_db[ org ]["max_members"]
                    update_sql += "WHERE channel_family_id = %s and org_id = %s" % ( org, data["cf_id"] )
                    print "SQL: %s" % update_sql
        return True

    def edit_subscription_in_table( self, prod, data ):
        """Updates/inserts a subscription in the DB.

        Expects a product like from consolidate_subscriptions()

        """
        cf_id = (self.get_channel_family_id(prod) or
                 self.add_channel_family_row(prod))

        select_sql = ("SELECT max_members, org_id, current_members "
                      "FROM RHNPRIVATECHANNELFAMILY "
                      "WHERE channel_family_id = %s ORDER BY org_id" % cf_id)
        query = rhnSQL.prepare(select_sql)
        query.execute()

        result = query.fetchall()

        all_subs_in_db = {}
        all_subs_sum = 0
        # copy database subscription data to a dict and
        # count all subscriptions over all org_id's
        for max_members, org_id, current_members in result:
            if org_id not in all_subs_in_db:
                all_subs_in_db[org_id] = {"max_members": 0,
                                          "current_members": current_members,
                                          "dirty": False}
            all_subs_in_db[org_id]["max_members"] += max_members
            all_subs_sum += max_members

        # generate test data
        # print "{ 'all_subs_in_db' : %s, 'all_subs_sum' : %s, 'prod' : '%s', 'data' : %s, 'cf_id' : %s }," % ( all_subs_in_db, all_subs_sum, prod, data, cf_id )
        all_subs_in_db = self.do_subscription_calculation(
            all_subs_in_db, all_subs_sum, prod, data, cf_id)

        for org in all_subs_in_db:
            if all_subs_in_db[org]["dirty"]:
                update_sql = """
                    UPDATE RHNPRIVATECHANNELFAMILY SET max_members = :max_m
                    WHERE channel_family_id = :cf_id and org_id = :org_id
                    """
                query = rhnSQL.prepare(update_sql)
                log_debug(2, "SQL: " % query )
                query.execute(
                    max_m = all_subs_in_db[org]["max_members"],
                    org_id = org,
                    cf_id = cf_id
                )
        rhnSQL.commit()

    def get_suse_product_id(self, product_id):
        """Return the suseproduct.id corresponding to an ncc/smt productid"""
        # this has the potential of getting uglier later if we get
        # overlapping ids
        query = rhnSQL.prepare("SELECT ID FROM SUSEPRODUCTS "
                               "WHERE PRODUCT_ID = :product_id")
        query.execute(product_id=product_id)
        try:
            return query.fetchone()[0]
        except TypeError:
            self.error_msg("Could not find the product associated with "
                           "this channel in the database. Please make sure "
                           "that your product list is up to date. "
                           "Run 'mgr-ncc-sync --refresh'.")
            sys.exit(1)

    def map_channel_to_products(self, channel, channel_id, product_id):
        """Map one channel to its products, registering it in the database

        :arg channel: channel XML Element
        :arg channel_id: the database id of the channel
        :arg product_id: id of the suse product that this channel belongs to

        Only non-optional channels are actually added.

        """
        # sometimes it's useful to know what arch this product has
        # (esp. in the case of i386,i486,i586,i686 having the same
        # channel but different products)
        query = rhnSQL.prepare(
            "SELECT a.name FROM rhnpackagearch a, suseproducts p "
            "WHERE a.id = p.arch_type_id AND p.product_id = :product_id")
        query.execute(product_id=product_id)
        arch_name = query.fetchone()
        arch_text = ' (%s)' % arch_name[0] if arch_name else ''
        channel_label = channel.get('label')
        parent_label = channel.get('parent')
        if parent_label == 'BASE':
            parent_label = None
        if channel.get('optional') == 'N':
            suse_id = self.get_suse_product_id(product_id)
            query = rhnSQL.prepare("""
                SELECT 1
                  FROM suseProductChannel
                 WHERE product_id = :product_id
                   AND channel_label = :channel_label""")
            query.execute(product_id=suse_id,
                          channel_label=channel_label)
            e = query.fetchone_dict() or None
            if e:
                # update
                query = rhnSQL.prepare(
                    "UPDATE suseProductChannel "
                    "   SET channel_id = :channel_id, "
                    "       parent_channel_label = :parent_label "
                    " WHERE product_id = :product_id "
                    "   AND channel_label = :channel_label")
                query.execute(product_id=suse_id,
                              channel_id=channel_id,
                              channel_label=channel_label,
                              parent_label=parent_label)
            else:
                # insert
                query = rhnSQL.prepare(
                    "INSERT INTO suseproductchannel (product_id, channel_id, channel_label, parent_channel_label) "
                    "VALUES (:product_id, :channel_id, :channel_label, :parent_label)")
                query.execute(product_id=suse_id,
                              channel_id=channel_id,
                              channel_label=channel_label,
                              parent_label=parent_label)
            self.log_msg("Registered channel %s to SuseProductChannel%s."
                         % (channel_label, arch_text))
        else:
            self.log_msg("Did NOT register optional channel %s to "
                         "SuseProductChannel%s." %
                         (channel_label, arch_text))

    def _is_vendor_channel_without_url(self, channel_id):
        """True if it is a vendor channel (org_id null) without urls"""
        query = rhnSQL.prepare("SELECT c.label "
                               "FROM rhnChannel c "
                               "WHERE c.org_id IS NULL "
                               "AND NOT EXISTS "
                               "(SELECT s.id FROM rhnContentSource s, "
                               "rhnCHannelContentSource cs "
                               "WHERE s.id=cs.source_id "
                               "AND cs.channel_id=c.id) "
                               "AND c.id=:channel_id")
        query.execute(channel_id=channel_id)
        if query.fetchone():
            return True
        return False

    def sync_channel(self, channel_label):
        """Schedule a repo sync for the specified database channel.

        """
        channel_id = self.get_channel_id(channel_label)
        if channel_id:
            if self._is_vendor_channel_without_url(channel_id):
                self.print_msg("Channel %s is a vendor channel without urls associated "
                                "to it. No need to sync." % channel_label)
                return
            try:
                scheduled = taskomatic.schedule_single_sat_repo_sync(channel_id)
            except socket.error, e:
                self.error_msg("Failed to connect to taskomatic. %s" % e)
            else:
                if scheduled:
                    self.print_msg("Scheduled repo sync for channel %s."
                                   % channel_label)
                else:
                    self.error_msg("Failed to schedule repo sync for channel %s."
                                   % channel_label)
        else:
            self.log_msg("Did not schedule a repo sync for channel %s as "
                         "it could not be found in the databse."
                         % channel_label)

    def sync_installed_channels(self):
        """Schedule a reposync of all SUSE Manager channels in the database.

        Only official channels (those with org_id == NULL) and
        having urls will be synced.
        Vendor channels without urls will be excluded, as they are supposed
        to be synced with a custom tool or act as parent channels.

        """
        if self.is_iss_slave:
            # sync must happen with mgr-inter-sync
            # return here without doing something
            self.print_msg("For syncing channels, please use the \"mgr-inter-sync\" command.")
            return

        self.print_msg("Scheduling repo sync for all installed channels...")

        query = rhnSQL.prepare("SELECT c.label "
                               "FROM rhnChannel c "
                               "JOIN rhnCHannelContentSource cs ON c.id = cs.channel_id "
                               "JOIN rhnContentSource s ON cs.source_id = s.id "
                               "WHERE c.org_id IS NULL")
        query.execute()
        try:
            channel_labels = [tup[0] for tup in query.fetchall()]
        except TypeError:
            self.print_msg(
                "No channels are installed in the database. \n"
                "Add new channels using: mgr-ncc-sync -c channel_label.")
        else:
            for label in channel_labels:
                self.sync_channel(label)

    def get_available_families(self):
        """Get the list of available channel family labels

        An available channel family is one that we have installed and
        whose privte_channel_family.max_members or fve_max_members is
        greater than zero. Which means that our client has active
        subscriptions for those channel families. Additionally, all official
        channel families have an ORG_ID of NULL.

        Returns a list of channel family labels.

        """
        query = rhnSQL.prepare(
            """SELECT DISTINCT fam.LABEL FROM RHNCHANNELFAMILY fam JOIN
                                              RHNPRIVATECHANNELFAMILY priv
                                              ON fam.ID = priv.CHANNEL_FAMILY_ID
               WHERE (priv.MAX_MEMBERS > 0 OR priv.FVE_MAX_MEMBERS > 0) AND
                     fam.ORG_ID IS NULL""")
        query.execute()
        families = [f[0] for f in query.fetchall()]
        return families

    def get_available_channels(self):
        """Get a list of all the channels the user has access to

        Returns a list of channel Element objects.

        """
        # get the channels from our config file
        channels_iter = etree.parse(CHANNELS).getroot()

        families = self.get_available_families()

        # filter out the channels which are not in the available families list
        channels = filter(lambda c: c.get('family') in families,
                          channels_iter)

        # filter out the channels whose parent isn't also in the channel list
        c_labels = [c.get('label') for c in channels]
        filtered = []
        for channel in channels:
            parent = channel.get('parent')
            if parent == 'BASE' or parent in c_labels:
                filtered.append(channel)

        # update_tag can be '', but empty string is not allowed in the DB
        # we have to set it to None
        for channel in filtered:
            udt = channel.get('update_tag')
            if udt == '':
                channel.set('update_tag', None)

        # make the repository urls point to our local path defined in 'fromdir'
        if self.fromdir:
            for channel in filtered:
                if channel.get('source_url'):
                    # pylint: disable=E1101
                    path = urlparse(channel.get('source_url')).path
                    channel.set('source_url', self.fromdir+path)
        return filtered

    def list_products(self):
        ret = dict()
        ncc_channels = sorted(self.get_available_channels(),
                              key=lambda channel: channel.get('label'))
        db_channels = rhnSQL.Table("RHNCHANNEL", "LABEL").keys()
        h = rhnSQL.prepare("""
            select p.product_id, pa.label as arch, p.friendly_name, p.version
              from suseproducts p
         left join rhnpackagearch pa ON p.arch_type_id = pa.id
        """)
        h.execute()
        products = {}
        for pr in h.fetchall_dict() or []:
            # special fixes
            if int(pr['product_id']) == 2320:
                pr['friendly_name'] = pr['friendly_name'] + " VMWare"
            products[int(pr['product_id'])] = pr

        channel_statuses = {}
        for channel in ncc_channels:
            label = channel.get('label')
            if label in db_channels:
                # channel is already in the database
                channel_statuses[label] = 'P'
            else:
                channel_statuses[label] = '.'
                #if self.is_mirrorable(channel):
                #    # channel is mirrorable, but is not in the database
                #    channel_statuses[label] = '.'
                #else:
                #    # channel is not mirrorable
                #    channel_statuses[label] = 'X'

        for channel in ncc_channels:
            channel_arch = channel.get('arch')
            label = channel.get('label')
            if channel.get('parent') != 'BASE':
                continue
            for product in channel.find('products'):
                product_id = int(product.text)
                if not products.has_key(product_id):
                    continue
                if (products[product_id]['arch'] is not None and
                    products[product_id]['arch'] != channel_arch):
                    continue

                productident = create_product_ident(product_id,
                                                    products[product_id]['friendly_name'],
                                                    channel_arch, label)
                if not ret.has_key(productident):
                    ret[productident] = SimpleProduct(productident, product_id,
                                                      products[product_id]['friendly_name'],
                                                      channel_arch, base_channel=label,
                                                      version=products[product_id]['version'])
                p = ret[productident]
                if channel.get('optional') == 'Y':
                    p.add_optional_channel(label, channel_statuses[label])
                else:
                    p.add_mandatory_channel(label, channel_statuses[label])

                for child in ncc_channels:
                    c_label = child.get('label')
                    if child.get('parent') != label:
                        continue

                    for cproduct in child.find('products'):
                        cproduct_id = int(cproduct.text)
                        if not products.has_key(cproduct_id):
                            continue
                        if (products[cproduct_id]['arch'] is not None and
                            products[cproduct_id]['arch'] != channel_arch):
                            continue

                        productidentchild = create_product_ident(cproduct_id,
                                                                 products[cproduct_id]['friendly_name'],
                                                                 channel_arch, child.get('parent'))

                        if not ret.has_key(productidentchild):
                            ret[productidentchild] = SimpleProduct(productidentchild, cproduct_id,
                                                                   products[cproduct_id]['friendly_name'],
                                                                   channel_arch, base_channel=child.get('parent'),
                                                                   version=products[cproduct_id]['version'])
                            pc = ret[productidentchild]
                        else:
                            pc = ret[productidentchild]
                            if child.get('parent') != pc.base_channel:
                                continue

                        if child.get('optional') == 'Y':
                            pc.add_optional_channel(c_label, channel_statuses[c_label])
                        else:
                            pc.add_mandatory_channel(c_label, channel_statuses[c_label])

        regex = re.compile('^.+\s(SP|11\.)(\d).*')
        for pk in sorted(ret.iterkeys()):
            p = ret[pk]
            if not p.is_base():
                continue
            parent_sp = None
            if (p.version == '11.1' or p.version == '11.2') and regex.match(p.name):
                d, parent_sp = regex.match(p.name).groups()
            for ck in sorted(ret.iterkeys()):
                c = ret[ck]
                if c.base_channel != p.base_channel:
                    continue
                if c.ident == p.ident:
                    continue
                if parent_sp:
                    # all this is only needed because of SLE 11 SP1 and SP2 uses the same base channel
                    if c.name == "Open Enterprise Server 11" and p.name != "SUSE Linux Enterprise Server 11 SP1":
                        continue
                    if c.name == "Open Enterprise Server 11.1" and p.name != "SUSE Linux Enterprise Server 11 SP2":
                        continue
                    elif c.name == "Open Enterprise Server 11.1" and p.name == "SUSE Linux Enterprise Server 11 SP2":
                        ret[ck].set_parent_product(p.ident)
                        continue
                    if c.name == "SUSE Linux Enterprise 11 Subscription Managment Tool" and p.name != "SUSE Linux Enterprise Server 11 SP1":
                        continue
                    if c.name == "SUSE Linux Enterprise Mono Extension 2.4" and p.name != "SUSE Linux Enterprise Server 11 SP1":
                        continue
                    child_sp = None
                    if regex.match(c.name):
                        d, child_sp = regex.match(c.name).groups()
                    if child_sp and parent_sp != child_sp:
                        # ugly hack: used to strip out products with of wrong Service Packs
                        continue
                ret[ck].set_parent_product(p.ident)

        return ret

    def list_channels(self):
        """List available channels on NCC and their status

        Status mean:
            - P - channel is in sync with the database (provided)
            - . - channel is not in the database, but is mirrorable
            - X - channel is in channels.xml, but is not mirrorable

        """
        ret = list()

        db_channels = rhnSQL.Table("RHNCHANNEL", "LABEL").keys()
        ncc_channels = sorted(self.get_available_channels(),
                              key=lambda channel: channel.get('label'))

        channel_statuses = {}
        for channel in ncc_channels:
            label = channel.get('label')
            if label in db_channels:
                # channel is already in the database
                channel_statuses[label] = 'P'
            else:
                if self.is_mirrorable(channel):
                    # channel is mirrorable, but is not in the database
                    channel_statuses[label] = '.'
                else:
                    # channel is not mirrorable
                    channel_statuses[label] = 'X'

        for channel in ncc_channels:
            label = channel.get('label')
            if channel.get('parent') != 'BASE':
                continue
            ret.append({'label': label, 'status': channel_statuses[label], 'parent': '', 'optional': (channel.get('optional') == 'Y')})

            for child in ncc_channels:
                c_label = child.get('label')
                if child.get('parent') != label:
                    continue
                ret.append({'label': c_label, 'status': channel_statuses[c_label], 'parent': label, 'optional': (child.get('optional') == 'Y')})
        return ret

    def get_mirrorable_repos(self):
        """Get a list of repository url parts directly from NCC

        Their presence in NCC means they are currently mirrorable and available
        to the user.

        Returns a dict of {repo_path: user_id}. Paths are split after '$RCE':
        NCC path="$RCE/SLED10-SP3-Pool/sled-10-i586"
        becomes: "/SLED10-SP3-Pool/sled-10-i586"

        Returns:
        {"/SLED10-SP3-Pool/sled-10-i586": "1",
         "/SLED10-SP3-Pool/sled-10-x86_64": "0", ...}

        """
        xmls = self._multi_get_ncc_xml(self.ncc_repoindex)

        repos = {}
        for (user_id, xml) in xmls:
            for repo in xml:
                repo_path = repo.get('path')
                # use the first set of valid credentials for each repo
                try:
                    repos[repo.get('path')]
                except KeyError:
                    repos[repo.get('path')] = user_id
        return repos

    get_mirrorable_repos = memoize(get_mirrorable_repos)

    def is_mirrorable(self, channel):
        """Check if the channel is mirrorable (we have access to it on NCC)

        A channel is mirrorable if:
         - it is a fake channel (path is empty)
         - its repo path is in the repoindex.xml from NCC or

        Returns:
         - user_id - string user_id of the credentials for NCC if it is
           mirrorable
         - True - if it is a local repository or a fake channel
         - False - if the repository does not exist or is not mirrorable

        """
        source_url = channel.get('source_url')
        if not source_url: # fake channel
            return True

        if self.fromdir and source_url.startswith('file://'): # local repository
            channel_path = source_url.split('file://')[1]
            return os.path.exists(channel_path)
        else: # remote repository
            channel_path = get_repo_path(source_url)
            try:
                return self.get_mirrorable_repos()[channel_path]
            except KeyError:
                if channel_path and channel_path.startswith('$RCE'):
                    # channel_path starting with $RCE have to be part of repoindex.xml
                    # if we get here, the repo is not mirrorable
                    # no need to do the accessible check
                    return False
                return (not channel_path
                        or suseLib.accessible(source_url + '/repodata/repomd.xml'))

    def get_ncc_channel(self, channel_label):
        """Try getting the NCC channel for this user

        :arg channel_label: the NCC label of the channel

        Returns a channel XML Element
        """
        for channel in self.get_available_channels():
            if channel.get('label') == channel_label:
                return channel

        # if we got this far, the channel is not available for this user
        sys.exit("Channel not available: %s." % channel_label)

    def get_parent_id(self, channel):
        """Returns the ID of the channel's parent from the database

        :arg channel: a Channel XML Element

        """
        parent_label = channel.get('parent')
        channel_label = channel.get('label')
        if parent_label == 'BASE':
            return None
        else:
            try:
                return rhnSQL.Row("RHNCHANNEL", "LABEL", parent_label)['id']
            except KeyError:
                sys.exit(
                    "The parent channel '%s' of channel '%s' "
                    "is not currently installed in the database. You need to "
                    "install it before adding this channel to the database."
                    % (parent_label, channel_label))

    def get_channel_id(self, channel_label):
        """Get a channel's id from the database

        :arg channel_label: a label of the channel to be queried

        Returns the channel's id if the channel was found or None otherwise.

        """
        query = rhnSQL.prepare(
            "SELECT ID FROM RHNCHANNEL WHERE LABEL = :label")
        query.execute(label=channel_label)
        try:
            return query.fetchone()[0]
        except TypeError:
            return None

    def insert_channel(self, channel, channel_id):
        """Insert an XML channel into the database

        One source_url identifies an rhnContentSource entry, a label
        identifies an rhnChannel entry and an rhnContentSource can
        belong to multiple rhnChannels

        :arg channel: XML ETree elem which contains repository information
        :arg channel_id: DB id of the channel the repo should belong to

        """
        self._channel_add_mirrcred(channel)

        channel_data = channel.attrib
        if not channel_data['source_url']:
            return # no URL, cannot create a content source

        # index the rhnContentSource by source_url, don't use the
        # channel label, because multiple channels should be able to
        # share the same repo and a repo is identified by its URL
        contentsource = rhnSQL.Row('RHNCONTENTSOURCE', 'source_url',
                                   channel_data['source_url'])
        if not contentsource.data: # create it
            type_id = rhnSQL.Row("RHNCONTENTSOURCETYPE", "LABEL", "yum")['id']
            query = rhnSQL.prepare(
                """INSERT INTO RHNCONTENTSOURCE
                       (ID, ORG_ID, TYPE_ID, SOURCE_URL, LABEL, METADATA_SIGNED)
                   VALUES (sequence_nextval('rhn_chan_content_src_id_seq'),
                           NULL, :type_id, :source_url, :label, :is_signed )""")
            query.execute(type_id=type_id, **channel_data)

        # create association between the new ContentSource and the Channel
        source_id = rhnSQL.Row(
            "RhnContentSource", "source_url", channel_data['source_url']
            )['id']
        channel_id = rhnSQL.Row(
            "RhnChannel", "label", channel_data["label"])['id']

        association = rhnSQL.prepare(
            """SELECT source_id, channel_id FROM RhnChannelContentSource
               WHERE source_id = :source_id and channel_id = :channel_id""")
        association.execute(source_id=source_id, channel_id=channel_id)

        if not association.fetchone():
            query = rhnSQL.prepare(
            """INSERT INTO RhnChannelContentSource (SOURCE_ID, CHANNEL_ID)
               VALUES (:source_id, :channel_id)""")
            query.execute(source_id=source_id, channel_id=channel_id)

    def get_channel_arch_id(self, channel):
        """Return the RHNCHANNELARCH.ID for this channel"""
        arch = channel.get('arch')

        # there are some special cases where the SUSE arch names are
        # different than the RedHat ones. We want to keep the channel
        # names with SUSE nomeclature, but use the existing RedHat names
        # in the RHNCHANNELARCH table
        if arch in ('i686', 'i586', 'i486', 'i386'):
            arch = 'ia32'
        elif arch == 'ppc64':
            arch = 'ppc'

        try:
            arch_id = rhnSQL.Row("RHNCHANNELARCH", "LABEL", "channel-%s" %
                                 arch)['id']
        except KeyError:
            raise Exception("This channel's arch could not be found in the "
                            "database: %s with arch: %s"
                            % (channel.get('name'), arch))
        return arch_id

    def get_channel_product_id(self, channel):
        """Return the RHNCHANNELPRODUCT.id for this channel

        If the corresponding row doesn't exist, insert it.

        :arg channel: XML Etree element

        """
        query = rhnSQL.prepare("SELECT id FROM rhnchannelproduct "
                               "WHERE product=:product and version=:version")
        query.execute(product=channel.get('product_name'),
                      version=channel.get('product_version'))
        try:
            channel_product_id = query.fetchone()[0]
        except TypeError:
            query = rhnSQL.prepare(
                """INSERT INTO rhnchannelproduct ( id, product, version, beta )
                   VALUES ( sequence_nextval('rhn_channelprod_id_seq'),
                            :product, :version, :beta )""")
            query.execute(product=channel.get('product_name'),
                          version=channel.get('product_version'),
                          beta='N')
        else:
            return channel_product_id

        query = rhnSQL.prepare("SELECT id FROM rhnchannelproduct "
                               "WHERE product=:product and version=:version")
        query.execute(product=channel.get('product_name'),
                      version=channel.get('product_version'))
        return query.fetchone()[0]

    def get_product_name_id(self, channel):
        """Return the RHNPRODUCTNAME.id for this channel

        If the corresponding row in RhnProductName does not exist, insert it.

        :arg channel: XML Etree element

        """
        query = rhnSQL.prepare("SELECT id FROM rhnproductname "
                               "WHERE name = :name")
        query.execute(name=channel.get('product_name'))
        try:
            product_name_id = query.fetchone()[0]
        except TypeError:
            query = rhnSQL.prepare(
                """INSERT INTO rhnproductname ( id, name, label )
                   VALUES ( sequence_nextval('rhn_productname_id_seq'),
                            :name, :label)""")
            query.execute(name=channel.get('product_name'),
                          label=channel.get('product_name'))
        else:
            return product_name_id
        query = rhnSQL.prepare("SELECT id FROM rhnproductname "
                               "WHERE name = :name")
        query.execute(name=channel.get('product_name'))
        return query.fetchone()[0]

    def add_channel(self, channel_label):
        """Add a new channel to the database

        :arg channel_label: the label of the channel that should be added

        """
        channel = self.get_ncc_channel(channel_label)

        if rhnSQL.Row('RHNCHANNEL', 'label', channel_label).data:
            self.print_msg("Channel %s is already in the database. "
                           % channel_label)
            return

        if not self.is_mirrorable(channel):
            self.print_msg("Channel %s is not mirrorable." % channel_label)
            return

        query = rhnSQL.prepare(
            """INSERT INTO RHNCHANNEL ( ID, BASEDIR, PARENT_CHANNEL,
                                        CHANNEL_ARCH_ID, LABEL, NAME,
                                        SUMMARY, DESCRIPTION,
                                        CHANNEL_PRODUCT_ID, PRODUCT_NAME_ID,
                                        CHECKSUM_TYPE_ID, UPDATE_TAG )
               VALUES ( sequence_nextval('rhn_channel_id_seq'), '/dev/null',
                       :parent_channel, :channel_arch_id, :label, :name,
                       :summary, :description, :channel_product_id,
                       :product_name_id,
                       (select id from RHNCHECKSUMTYPE
                        where label = 'sha1'), :update_tag )""")
        query.execute(
            channel_product_id = self.get_channel_product_id(channel),
            product_name_id = self.get_product_name_id(channel),
            parent_channel = self.get_parent_id(channel),
            channel_arch_id = self.get_channel_arch_id(channel),
            # XXX - org_id = ??
            **channel.attrib)

        # welcome to the family: create relation between the new channel
        # and corresponding channel family
        channel_id = rhnSQL.Row("RHNCHANNEL", "LABEL", channel_label)['id']
        channel_family_id = rhnSQL.Row(
            "RHNCHANNELFAMILY", "LABEL", channel.get('family'))['id']

        query = rhnSQL.prepare(
            """INSERT INTO RHNCHANNELFAMILYMEMBERS
                   (CHANNEL_ID, CHANNEL_FAMILY_ID)
               VALUES (:channel_id, :channel_family_id)""")
        query.execute(
            channel_id = channel_id,
            channel_family_id = channel_family_id)

        # add repos to the database
        self.insert_channel(channel, channel_id)

        # register this channel's products in the database
        for product in channel.find('products'):
            product_id = int(product.text)
            self.map_channel_to_products(channel, channel_id, product_id)

        for child in channel:
            if child.tag == 'dist':
                self.add_dist_channel_map(channel_id,
                                          self.get_channel_arch_id(channel),
                                          child)
        rhnSQL.commit()
        self.print_msg("Added channel '%s' to the database."
                       % channel_label)

    def update_channels(self):
        """Update channel information in the database"""
        if self.is_iss_slave:
            # we sync from another SUSE Manager. Nothing todo here
            return

        channels_xml = {}
        for c in etree.parse(CHANNELS).getroot():
            udt = c.get('update_tag')
            if udt == '':
                udt = None
            if self.fromdir:
                if c.get('source_url'):
                    # pylint: disable=E1101
                    path = urlparse(c.get('source_url')).path
                    c.set('source_url', self.fromdir+path)
            try:
                creds = self._channel_add_mirrcred(c)
            except ChannelNotMirrorable:
                # handle unmirrorable channels later
                continue
            channels_xml[c.get('label')] = {
                'name': c.get('name'),
                'summary': c.get('summary'),
                'description': c.get('description'),
                'source_url': c.get('source_url'),
                'update_tag': udt}

        updated_channels = []
        disabled_channels = []
        query = rhnSQL.prepare("SELECT label, name, summary, description, update_tag"
                               "  FROM rhnChannel "
                               " WHERE org_id IS NULL")
        query.execute()
        result = query.fetchall()
        for label, name, summary, description, update_tag in result:
            if label in channels_xml:
                if not (channels_xml[label]['name'] == name and
                        channels_xml[label]['summary'] == summary and
                        channels_xml[label]['description'] == description and
                        channels_xml[label]['update_tag'] == update_tag):
                    updated_channels.append(label)
                    query = rhnSQL.prepare(
                        """UPDATE RHNCHANNEL SET
                             NAME = :name,
                          SUMMARY = :summary,
                      DESCRIPTION = :description,
                       UPDATE_TAG = :update_tag
                            WHERE LABEL = :label""")
                    query.execute(label=label, **channels_xml[label])
            else:
                # we leave channels which are no longer mirrorable in
                # the database, but warn the user about it
                disabled_channels.append(label)
        rhnSQL.commit()

        if updated_channels:
            self.print_msg("The following channel(s) have updated metadata: ")
            for c in updated_channels:
                self.print_msg(c)
        if disabled_channels:
            self.print_msg("The following channel(s) are no longer mirrorable: ")
            for c in disabled_channels:
                self.print_msg(c)

        query = rhnSQL.prepare("SELECT label, source_url "
                               "FROM rhnContentSource")
        query.execute()
        result = query.fetchall()
        updated_creds = []
        for label, source_url in result:
            if label in channels_xml:
                channel = channels_xml[label]
                if channel['source_url'] != source_url:
                    # remove the credentials from the URL
                    query = rhnSQL.prepare("UPDATE RHNCONTENTSOURCE "
                                           "SET source_url = :new_url "
                                           "WHERE source_url LIKE :old_url")
                    query.execute(new_url=channel['source_url'],
                                  old_url=source_url)
                    updated_creds.append(label)
        rhnSQL.commit()

        if updated_creds:
            self.print_msg(
                "Updated mirror credentials / channel source URL for: ")
            for c in updated_creds:
                self.print_msg(c)

    def sync_suseproductchannel(self):
        """Sync the suseProductChannel relationships from the config file

        Add or remove channel/product relationships according to the
        channel's current optional status.

        """
        q = rhnSQL.prepare("SELECT spc.product_id, spc.channel_label "
                           "FROM suseproductchannel spc "
                           "JOIN suseproducts sp ON spc.product_id = sp.id")
        q.execute()
        existing_product_channels = [("%s-%s" % (tup[0], tup[1])) for tup in q.fetchall()]

        for channel in self.get_available_channels():
            if channel.get('optional') == 'Y':
                # we store only not optional channels
                continue
            installed_channels = rhnSQL.Table("RHNCHANNEL", "LABEL")
            channel_label = channel.get('label')
            parent_label = channel.get('parent')
            if parent_label == 'BASE':
                parent_label = None
            for product in channel.find('products'):
                product_id = self.get_suse_product_id(int(product.text))
                channel_id = None
                if installed_channels.has_key(channel_label):
                    channel_id = rhnSQL.Row("rhnchannel", "label", channel_label)['id']
                key = "%s-%s" % (product_id, channel_label)
                if key in existing_product_channels:
                    # update
                    q = rhnSQL.prepare("UPDATE suseproductchannel "
                                       "SET channel_id = :channel_id, "
                                       "    parent_channel_label = :parent_label "
                                       "WHERE product_id=:product_id AND channel_label=:channel_label")
                    q.execute(channel_id=channel_id, product_id=product_id,
                              channel_label=channel_label, parent_label=parent_label)
                    existing_product_channels.remove(key)
                else:
                    # insert
                    q = rhnSQL.prepare("INSERT INTO suseproductchannel "
                                       "(channel_id, product_id, channel_label, parent_channel_label) "
                                       "VALUES (:channel_id, :product_id, :channel_label, :parent_label)")
                    q.execute(channel_id=channel_id, product_id=product_id,
                              channel_label=channel_label, parent_label=parent_label)

        for pc in existing_product_channels:
            # drop
            (product_id, channel_label) = pc.split('-', 1)
            q = rhnSQL.prepare("DELETE FROM suseproductchannel "
                               "WHERE product_id=:product_id AND channel_label=:channel_label")
            q.execute(product_id=product_id, channel_label=channel_label)


        rhnSQL.commit()

    def add_dist_channel_map(self, channel_id, channel_arch_id, dist):
        query = rhnSQL.prepare(
            """INSERT INTO RHNDISTCHANNELMAP
               (OS, RELEASE, CHANNEL_ARCH_ID, CHANNEL_ID)
               VALUES (:os, :release, :channel_arch_id, :channel_id)""")
        query.execute(os=dist.get('os'), release=dist.get('release'),
        channel_arch_id=channel_arch_id, channel_id=channel_id)

    def update_subscriptions(self):
        """Sync subscriptions from NCC to the database"""
        all_subs = self.get_subscriptions_from_ncc()
        cons_subs = self.consolidate_subscriptions(all_subs)
        self.reset_entitlements_in_table()
        for s in cons_subs:
            if self.is_entitlement(s):
                self.edit_entitlement_in_table(s, cons_subs[s])
            else:
                self.edit_subscription_in_table(s, cons_subs[s])

    def is_entitlement(self, product):
        return product in self.ncc_rhn_ent_mapping

    def _channel_add_mirrcred(self, channel):
        """Add the mirrorcred query string to the url in channel['source_url']"""
        channel_url = channel.get('source_url')

        # if we don't have a source_url, then we don't need to do
        # anything since we won't put anything in the database anyway
        if not channel_url:
            return

        if self.fromdir is not None:
            if channel_url.startswith('file://'):
                return
            # pylint: disable=E1101
            path = urlparse(channel_url).path
            channel.set('source_url', self.fromdir+path)
            if self.is_mirrorable(channel):
                return
            else:
                # seems to be no local url.
                # Set it back and try to find a mirror credential
                channel.set('source_url', channel_url)

        url = suseLib.URL(channel_url)

        # nu.novell.com needs authentication using the mirror credentials
        if url.host == "nu.novell.com":
            # add the user to the credentials query parameter if it's non-zero
            user_id = self.is_mirrorable(channel)
            if not user_id:
                raise ChannelNotMirrorable
            credstring = "credentials=mirrcred"
            if user_id != '0':
                credstring += "_" + user_id

            if url.query:
                url.query += "&"+credstring
            else:
                url.query = credstring
            channel.set('source_url', url.getURL())

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

    def migrate_res(self):
        """Migrate channel families from rhel to RES subscriptions"""

        cf_id = rhnSQL.Row("rhnChannelFamily", 'label', 'RES')['id']
        if not cf_id:
            self.error_msg("ID of SUSE Linux Enterprise RedHat Expanded Support not found")
            sys.exit(1)

        q = rhnSQL.prepare("""UPDATE rhnChannelFamilyMembers
                                 SET channel_family_id=:cf_id
                               WHERE channel_family_id IN (
                                     SELECT rcfin.id
                                       FROM rhnChannelFamily rcfin
                                      WHERE rcfin.label IN
                                            ('rhel-server', 'rhel-server-6',
                                             'rhel-cluster',
                                             'rhel-server-cluster',
                                             'rhel-server-cluster-storage',
                                             'rhel-server-vt'))
                           """)
        q.execute(cf_id=cf_id)

def sql_list(alist):
    """Transforms a python list into an SQL string of a list

    ['foo', 'bar'] --> "('foo', 'bar')"
    ['1', '2', '3'] --> "(1, 2, 3)"

    """
    try:
        # if they are integers, then make a list of integers
        alist = map(int, alist)
    except ValueError:
        # otherwise leave it as it is
        pass
    l = str(tuple(alist))
    l = l.replace(",)", ")") # "('foo',)" should be "('foo')"
    return l

def get_repo_path(repourl):
    """
    https://nu.novell.com/repo/$RCE/SLE11-SP1-Debuginfo-Updates/sle-11-ppc64/?credentials=mirrcred_1
    becomes:
    $RCE/SLE11-SP1-Debuginfo-Updates/sle-11-ppc64

    """
    return repourl.split('repo/')[-1].split('/?credentials')[0].rstrip('/')



