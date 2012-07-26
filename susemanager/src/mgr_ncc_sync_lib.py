# -*- coding: utf-8 -*-
#
# Copyright (C) 2009, 2010, 2011 Novell, Inc.
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
import xml.etree.ElementTree as etree
from datetime import date
from urlparse import urlparse, urljoin
from xml.parsers.expat import ExpatError

from spacewalk.server import rhnSQL, taskomatic
from spacewalk.common import rhnLog, suseLib
from spacewalk.common.rhnConfig import initCFG, CFG
from spacewalk.common.rhnLog import log_debug, log_error

CHANNELS = '/usr/share/susemanager/channels.xml'
CHANNEL_FAMILIES = '/usr/share/susemanager/channel_families.xml'

DEFAULT_LOG_LOCATION = '/var/log/rhn/'

INFINITE = 200000 # a very big number that we use for unlimited subscriptions

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

class NCCSync(object):
    """This class is used to sync SUSE Manager Channels and NCC repositories"""

    def __init__(self, quiet=False, debug=-1, fromdir=None):
        """Setup configuration"""
        self.quiet = quiet
        self.debug = debug
        self.reset_ent_value = 10

        if fromdir is not None:
            fromdir = urljoin('file://', os.path.abspath(fromdir))
        self.fromdir = fromdir

        self.ncc_rhn_ent_mapping = {
            "SM_ENT_MON_S"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_S"      : [ "provisioning_entitled" ],
            "SM_ENT_MGM_S"       : [ "enterprise_entitled" ],
            "SM_ENT_MGM_V"       : [ "virtualization_host_platform", "enterprise_entitled" ],
            "SM_ENT_MON_V"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_V"      : [ "provisioning_entitled" ],
            "SM_ENT_MON_Z"       : [ "monitoring_entitled" ],
            "SM_ENT_PROV_Z"      : [ "provisioning_entitled" ],
            "SM_ENT_MGM_Z"       : [ "enterprise_entitled" ]
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

        self.namespace = "http://www.novell.com/xml/center/regsvc-1_0"

        # FIXME:
        # self.ncc_url_prods = CFG.reg_url + "/?command=regdata&lang=en-US&version=1.0"
        # self.ncc_url_subs  = CFG.reg_url + "/?command=listsubscriptions&lang=en-US&version=1.0"
        if self.fromdir:
            self.ncc_url_prods = self.fromdir + "/productdata.xml"
            self.ncc_url_subs  = self.fromdir + "/listsubscriptions.xml"
            self.ncc_repoindex = self.fromdir + "/repo/repoindex.xml"
        else:
            self.ncc_url_prods = "https://secure-www.novell.com/center/regsvc/?command=regdata&lang=en-US&version=1.0"
            self.ncc_url_subs  = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions&lang=en-US&version=1.0"
            self.ncc_repoindex = "https://%(authuser)s:%(authpass)s@nu.novell.com/repo/repoindex.xml"

        self.connect_retries = 10

        try:
            rhnSQL.initDB()
        except rhnSQL.SQLConnectError, e:
            self.error_msg("Could not connect to the database. %s" % e)
            sys.exit(1)

    def dump_to(self, path):
        """Dump NCC xml data to path

        :arg path: the destination path
        """
        if not os.path.exists(path):
            os.makedirs(path)
        if not os.path.isdir(path):
            self.error_msg("'%s' is not a directory." % path)
            sys.exit(1)

        if self.fromdir:
            send = None
        else:
            send = ('<?xml version="1.0" encoding="UTF-8"?>'
                    '<listsubscriptions xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                    '<authuser>%(authuser)s</authuser>'
                    '<authpass>%(authpass)s</authpass>'
                    '<smtguid>%(smtguid)s</smtguid>'
                    '</listsubscriptions>\n' % self.__dict__)

        self.print_msg("Downloading Subscription information...")
        try:
            subs = io.FileIO(path + '/listsubscriptions.xml', 'w')
            response = suseLib.send(self.ncc_url_subs, send)
            subs.write(response.read())
            subs.close()
        except Exception, e:
            self.error_msg("NCC connection failed: %s" % e)
            sys.exit(1)

        if self.fromdir:
            send = None
        else:
            send = ('<?xml version="1.0" encoding="UTF-8"?>'
                    '<productdata xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                    '<authuser>%(authuser)s</authuser>'
                    '<authpass>%(authpass)s</authpass>'
                    '<smtguid>%(smtguid)s</smtguid>'
                    '</productdata>\n' % self.__dict__)

        self.print_msg("Downloading Product information...")
        try:
            prod = io.FileIO(path + '/productdata.xml', 'w')
            response = suseLib.send(self.ncc_url_prods, send)
            prod.write(response.read())
            prod.close()
        except:
            self.error_msg("NCC connection failed")
            sys.exit(1)

        try:
            idx = io.FileIO(path + '/repoindex.xml', 'w')
            response = suseLib.send(self.ncc_repoindex % self.__dict__)
            idx.write(response.read())
            idx.close()
        except:
            self.error_msg("NCC connection failed")
            sys.exit(1)

    def _get_ncc_xml(self, url, send=None):
        """Connect to ncc and return the parsed XML document

        :arg url: the url where the request will be sent
        :kwarg send: do a post-request when "send" is given.

        Returns the root XML Element of the parsed document.

        """
        try:
            response = suseLib.send(url, send)
        except:
            self.error_msg("NCC connection failed")
            sys.exit(1)

        try:
            tree = etree.parse(response)
        except ExpatError:
            self.error_msg("Could not parse XML from %s. The remote document "
                           "does not appear to be a valid XML document. "
                           "This document was written to the logfile: %s." %
                           (url, rhnLog.LOG.file))
            response.seek(0)
            log_error("Invalid XML document (got ExpatError) from %s: %s" %
                      (url, response.read()))
            sys.exit(1)

        return tree.getroot()

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
        if self.fromdir:
            send = None
        else:
            send = ('<?xml version="1.0" encoding="UTF-8"?>'
                    '<listsubscriptions xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                    '<authuser>%(authuser)s</authuser>'
                    '<authpass>%(authpass)s</authpass>'
                    '<smtguid>%(smtguid)s</smtguid>'
                    '</listsubscriptions>\n' % self.__dict__)

        self.print_msg("Downloading Subscription information...")
        subscriptionlist = self._get_ncc_xml(self.ncc_url_subs, send)
        subscriptions = []
        for row in subscriptionlist.findall('{%s}subscription' % self.namespace):
            subscription = {}
            for col in row.getchildren():
                dummy = col.tag.split( '}' )
                key = dummy[1]
                subscription[key] = col.text
            subscriptions.append(subscription)
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
                if label not in subscription_count:
                    subscription_count[label] = {'consumed': 0,
                                                 'nodecount': INFINITE}
                else:
                    subscription_count[label]['nodecount'] = INFINITE

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
        if self.fromdir:
            send = None
        else:
            send = ('<?xml version="1.0" encoding="UTF-8"?>'
                    '<productdata xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                    '<authuser>%(authuser)s</authuser>'
                    '<authpass>%(authpass)s</authpass>'
                    '<smtguid>%(smtguid)s</smtguid>'
                    '</productdata>\n' % self.__dict__)

        self.print_msg("Downloading Product information...")
        productdata = self._get_ncc_xml(self.ncc_url_prods, send)

        suse_products = []
        for row in productdata:
            if row.tag == ("{%s}row" % self.namespace):
                suseProduct = {}
                for col in row.findall("{%s}col" % self.namespace):
                    key = col.get("name")
                    if key in ["start-date", "end-date"]:
                        suseProduct[key] = float(col.text)
                    else:
                        suseProduct[key] = col.text
                if suseProduct["PRODUCT_CLASS"]:
                    # FIXME: skip buggy NCC entries. Some have no
                    # product_class, which is invalid data
                    suse_products.append(suseProduct)
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
            select_sql = ("SELECT max_members, org_id, current_members from RHNPRIVATECHANNELFAMILY "
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
        return query.fetchone()[0]

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
        if channel.get('optional') == 'N':
            suse_id = self.get_suse_product_id(product_id)
            query = rhnSQL.prepare(
                "INSERT INTO suseproductchannel (product_id, channel_id) "
                "VALUES (:product_id, :channel_id)")
            query.execute(product_id=suse_id,
                          channel_id=channel_id)
            self.log_msg("Registered channel %s to SuseProductChannel%s."
                         % (channel.get('label'), arch_text))
        else:
            self.log_msg("Did NOT register optional channel %s to "
                         "SuseProductChannel%s." %
                         (channel.get('label'), arch_text))

    def sync_channel(self, channel_label):
        """Schedule a repo sync for the specified database channel.

        """
        channel_id = self.get_channel_id(channel_label)
        if channel_id:
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

        Only official channels (those with org_id == NULL) will be synced.

        """
        self.print_msg("Scheduling repo sync for all installed channels...")

        query = rhnSQL.prepare("SELECT label FROM rhnChannel "
                               "WHERE org_id IS NULL")
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

        # make the repository urls point to our local path defined in 'fromdir'
        if self.fromdir:
            for channel in filtered:
                if channel.get('source_url'):
                    # pylint: disable=E1101
                    path = urlparse(channel.get('source_url')).path
                    channel.set('source_url', self.fromdir+path)
        return filtered

    def list_channels(self):
        """List available channels on NCC and their status

        Statuses mean:
            - P - channel is in sync with the database (provided)
            - . - channel is not in the database, but is mirrorable
            - X - channel is in channels.xml, but is not mirrorable

        """
        self.print_msg("Listing all channels you are subscribed to...\n\n"
                       "Statuses mean:\n"
                       "- P - channel is in sync with the database (provided)\n"
                       "- . - channel is not installed, but is available\n"
                       "- X - channel is not available\n")

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
            print "[%s] %s" % (channel_statuses[label], label)

            for child in ncc_channels:
                c_label = child.get('label')
                if child.get('parent') != label:
                    continue
                print "    [%s] %s" % (channel_statuses[c_label], c_label)

    def get_mirrorable_repos(self):
        """Get a list of repository url parts directly from NCC

        Their presence in NCC means they are currently mirrorable and available
        to the user.

        Returns a list of repo paths split after '$RCE':
        NCC path="$RCE/SLED10-SP3-Pool/sled-10-i586"
        becomes: "/SLED10-SP3-Pool/sled-10-i586"

        """
        root = self._get_ncc_xml(self.ncc_repoindex % self.__dict__)

        return [repo.get('path') for repo in root]
    get_mirrorable_repos = memoize(get_mirrorable_repos)

    def is_mirrorable(self, channel):
        """Return a boolean if the etree Element channel is mirrorable or not

        A channel is mirrorable if:
         - its repo path is in the repoindex.xml from NCC or
         - it is a fake channel (path is empty)

        """
        if self.fromdir:
            channel_path = channel.get('source_url')
            return not channel_path or os.path.exists(channel_path.split('file://')[1])
        else:
            channel_path = get_repo_path(channel.get('source_url'))
            return (channel_path in self.get_mirrorable_repos() or not channel_path or
                    suseLib.accessible(channel.get('source_url') + '/repodata/repomd.xml'))

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
        channel_data = channel.attrib
        if not channel_data['source_url']:
            return # no URL, cannot create a content source

        _channel_add_mirrcred(channel_data)

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
        # first look in the db to see if it's already there
        if self.get_channel_id(channel_label):
            self.print_msg("Channel %s is already in the database. "
                            % channel_label)
            return

        channel = self.get_ncc_channel(channel_label)
        # then look if it's mirrorable
        if not self.is_mirrorable(channel):
            self.print_msg("Channel %s is not mirrorable." % channel_label)
        else:
            query = rhnSQL.prepare(
                """INSERT INTO RHNCHANNEL ( ID, BASEDIR, PARENT_CHANNEL,
                                            CHANNEL_ARCH_ID, LABEL, NAME,
                                            SUMMARY, DESCRIPTION,
                                            CHANNEL_PRODUCT_ID, PRODUCT_NAME_ID,
                                            CHECKSUM_TYPE_ID )
                   VALUES ( sequence_nextval('rhn_channel_id_seq'), '/dev/null',
                           :parent_channel, :channel_arch_id, :label, :name,
                           :summary, :description, :channel_product_id,
                           :product_name_id,
                           (select id from RHNCHECKSUMTYPE
                            where label = 'sha1') )""")
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

    def sync_suseproductchannel(self):
        """Sync the suseProductChannel relationships from the config file

        Add or remove channel/product relationships according to the
        channel's current optional status.

        """
        q = rhnSQL.prepare("SELECT c.label "
                           "FROM rhnchannel c, suseproductchannel s "
                           "WHERE c.id = s.channel_id")
        q.execute()
        channel_labels = [tup[0] for tup in q.fetchall()]
        delete_channel_labels = []
        add_channels = []

        installed_channels = rhnSQL.Table("RHNCHANNEL", "LABEL")
        for channel in self.get_available_channels():
            # we only care about the ones that we already have installed
            if installed_channels.has_key(channel.get('label')):
                if channel.get('label') in channel_labels:
                    if channel.get('optional') == 'Y':
                        delete_channel_labels.append(channel.get('label'))
                else:
                    if channel.get('optional') == 'N':
                        add_channels.append(channel)

        # add channel-product relationships
        for channel in add_channels:
            channel_label = channel.get('label')
            channel_id = rhnSQL.Row("rhnchannel", "label", channel_label)['id']
            for product in channel.find('products'):
                product_id = rhnSQL.Row("suseproducts", 'product_id',
                                        int(product.text))['id']
                q = rhnSQL.prepare("INSERT INTO suseproductchannel "
                                   "(channel_id, product_id) "
                                   "VALUES (:channel_id, :product_id)")
                q.execute(channel_id=channel_id, product_id=product_id)

        # delete channel-product relationships we don't want anymore
        if delete_channel_labels:
            q = rhnSQL.prepare("SELECT id FROM rhnchannel "
                               "WHERE label in %s" %
                               sql_list(delete_channel_labels))
            q.execute()
            delete_channel_ids = [i[0] for i in q.fetchall()]

            q = rhnSQL.prepare("DELETE FROM suseproductchannel "
                               "WHERE channel_id IN %s" %
                               sql_list(delete_channel_ids))
            q.execute()
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

def _channel_add_mirrcred(channel):
    """Add the mirrorcred query string to the url in channel['source_url']"""
    url = suseLib.URL(channel['source_url'])
    # nu.novell.com needs authentication using the mirror credentials
    if url.host == "nu.novell.com":
        if url.query:
            url.query += "&credentials=mirrcred"
        else:
            url.query = "credentials=mirrcred"
        channel['source_url'] = url.getURL()

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
    https://nu.novell.com/repo/$RCE/SLE11-SP1-Debuginfo-Updates/sle-11-ppc64/
    becomes:
    $RCE/SLE11-SP1-Debuginfo-Updates/sle-11-ppc64

    """
    return repourl.split('repo/')[-1].rstrip('/')


