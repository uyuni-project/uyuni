#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2009, 2010 Novell, Inc.
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

import sys
import urllib
import time
import xml.etree.ElementTree as etree
from optparse import OptionParser
from datetime import date

from spacewalk.server import rhnSQL
from spacewalk.common import initCFG, CFG, rhnLog
from spacewalk.susemanager import suseLib
from spacewalk.common import log_debug, log_error, rhnFault

NCC_CHANNELS = '/usr/share/susemanager/channels.xml'
DEFAULT_LOG_LOCATION = '/var/log/rhn/'

class ChannelNotAvailableError(Exception):
    def __init__(self, channel_label):
        self.channel = channel_label
    def __str__(self):
        return "You do not have access to the channel: %s" % self.channel


class NCCSync(object):
    """This class is used to sync SUSE Manager Channels and NCC repositories"""

    def __init__(self, quiet=False):
        """Setup configuration"""
        self.quiet = quiet

        initCFG("server.susemanager")
        if CFG.DEBUG > 1:
            rhnLog.initLOG(DEFAULT_LOG_LOCATION + 'sm-ncc-sync.log', CFG.DEBUG)
        else:
          rhnLog.initLOG(DEFAULT_LOG_LOCATION + 'sm-ncc-sync.log')

        self.log_msg("\nStarted: %s" % (time.asctime(time.localtime())))

        self.smtguid  = suseLib.getProductProfile()['guid']
        self.authuser = CFG.mirrcred_user
        self.authpass = CFG.mirrcred_pass

        self.namespace = "http://www.novell.com/xml/center/regsvc-1_0"

        # FIXME:
        # self.ncc_url_prods = CFG.reg_url + "/?command=regdata&lang=en-US&version=1.0"
        # self.ncc_url_subs  = CFG.reg_url + "/?command=listsubscriptions&lang=en-US&version=1.0"
        self.ncc_url_prods = "https://secure-www.novell.com/center/regsvc/?command=regdata&lang=en-US&version=1.0"
        self.ncc_url_subs  = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions&lang=en-US&version=1.0"
        self.connect_retries = 10
        self.channel_family_config = '/usr/share/susemanager/channel_families.xml'

        rhnSQL.initDB()

    def _connect_ncc( self, url, send=None ):
        """Connect the ncc with the given URL.
        
        :arg url: the url where the request will be sent
        :kwarg send: do a post-request when "send" is given.

        Returns a filedescriptor.

        """
        new_url = url
        try_counter = self.connect_retries
        while new_url != "" and try_counter > 0:
            try_counter -= 1
            o = urllib.URLopener()
            try:
                log_debug(1, "try connecting %s" % new_url)
                f = o.open( new_url, send)
                new_url = ""
            except IOError, e:
                # 302 is a redirect
                if e[1] == 302:
                    log_debug(1, "got redirect")
                    new_url = e[3].dict["location"]
                elif e[1] == 504:
                    # gateway timeout - try again
                    log_debug(1, "got gateway timeout")
                    pass
                else:
                    self.error_msg("connecting %s failed with HTTP error code %s" % (new_url, e[1]))
                    raise e
        return f

    # OUT: [ {'consumed-virtual': '0',
    #         'productlist': '2380,2502',
    #         'start-date': '1167782194',
    #         'end-date': '0',
    #         'consumed': '1',
    #         'substatus': 'ACTIVE',
    #         'subid': '66cb82cdb3e6f82961cdc056118dabd1',
    #         'nodecount': '0',
    #         'subname': 'SUSE Linux Enterprise Desktop 10',
    #         'duration': '0',
    #         'regcode': '86119@NYC-SLED-dad2c3d7c',
    #         'type': 'FULL',
    #         'server-class': 'OS',
    #         'product-class': '7260'
    #         }, { ... }, ... ]
    def get_subscriptions_from_ncc(self):
        """Returns all subscripts a customer has
           that data can be used for consolidate_subscriptions()."""
        send = ('<?xml version="1.0" encoding="UTF-8"?>'
                '<productdata xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                '<authuser>%(authuser)s</authuser>'
                '<authpass>%(authpass)s</authpass>'
                '<smtguid>%(smtguid)s</smtguid>'
                '</productdata>\n' % self.__dict__)

        self.print_msg("Downloading Subscription information")
        f = self._connect_ncc( self.ncc_url_subs, send )
        tree = etree.parse(f)
        subscriptions = []
        for row in tree.getroot():
            if row.tag == ("{%s}subscription" % self.namespace):
                subscription = {}
                for col in row.getchildren():
                    dummy = col.tag.split( '}' )
                    key = dummy[1]
                    subscription[key] = col.text
                subscriptions.append(subscription)
        return subscriptions

    #   OUT: {'SLE-HAE-PPC':{ 'consumed':0, 'nodecount':10 }, '10040':{ 'consumed':0, 'nodecount':200000}, ... }
    #   nodecount is the number of subscriptions the customer has today for a product
    #   FIXME: not sure about 'consumed' yet
    def consolidate_subscriptions(self, subs):
        """Takes the get_subscriptions_from_ncc() data and sums the subscriptions of a product"""
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
                    # "nodecount = None" means unlimited
                    if s["nodecount"] == "-1":
                        subscription_count[ p ] = { "consumed" : int(s["consumed"]), "nodecount" : 200000 }
                    elif subscription_count.has_key( p ):
                        subscription_count[ p ]["nodecount"] += int(s["nodecount"])
                    else:
                        subscription_count[ p ] = { "consumed" : int(s["consumed"]), "nodecount" : int(s["nodecount"]) }

        return subscription_count

    def get_suse_products_from_ncc(self):
        """Get all the products known by NCC

        Returns a list of dicts with all the keys the NCC has for a product

        """
        send = ('<?xml version="1.0" encoding="UTF-8"?>'
                '<productdata xmlns="%(namespace)s" client_version="1.2.3" lang="en">'
                '<authuser>%(authuser)s</authuser>'
                '<authpass>%(authpass)s</authpass>'
                '<smtguid>%(smtguid)s</smtguid>'
                '</productdata>\n' % self.__dict__)

        self.print_msg("Downloading Product information")
        f = self._connect_ncc(self.ncc_url_prods, send)

        tree = etree.parse(f)
        suseProducts = []
        for row in tree.getroot():
            if row.tag == ("{%s}row" % self.namespace):
                suseProduct = {}
                for col in row.findall("{%s}col" % self.namespace):
                    key = col.get("name")
                    if key == "start-date" or key == "end-date":
                        suseProduct[key] = float(col.text)
                    else:
                        suseProduct[key] = col.text
                if suseProduct["PRODUCT_CLASS"] != None:
                    # FIXME: skip buggy NCC entries. Some have no product_class, which is invalid data
                    suseProducts.append(suseProduct)
        return suseProducts

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
        """Updates the channel_family table on base of the XML config file"""
        self.print_msg("Updating Channel Family Information")
        tree = etree.parse(self.channel_family_config)
        for family in tree.getroot():
            name = family.get("name")
            label = family.get("label")
            self.edit_channel_family_table(label, name)

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
        if name == None:
            name = label

        if channel_family_id == None:
            channel_family_id = self.add_channel_family_row(
                label, name, org_id, url)
        else:
            self.update_channel_family_table(label, name, org_id, url)
        return channel_family_id

    def update_suse_products_table(self, suseProducts):
        """Creates/updates entries in the DB Products table

        Expects a get_suse_products_from_ncc() datastructure

        """
        for p in suseProducts:
            arch_type_id = self.get_arch_id( p["ARCH"] )
            params = {}

            channel_family_id = self.get_channel_family_id(
                p["PRODUCT_CLASS"])

            if not channel_family_id:
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
                         ARCH_TYPE_ID, RELEASE, CHANNEL_FAMILY_ID, PRODUCT_LIST)
                    VALUES
                        (sequence_nextval('suse_products_id_seq'),
                         :name, :version, :friendly_name, :arch_type_id,
                         :release, :channel_family_id, :product_list)
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
                              product_list = p["PRODUCT_LIST"]
                )
        rhnSQL.commit()

    def edit_subscription_in_table( self, prod, data ):
        """Updates/inserts a subscription in the DB.

        Expects a product like from consolidate_subscriptions()

        """
        cf_id = (self.get_channel_family_id(prod) or
                 self.add_channel_family_row(prod))

        select_sql = ("SELECT max_members, org_id, current_members from RHNPRIVATECHANNELFAMILY "
                      "WHERE channel_family_id = %s order by org_id" % cf_id)
        query = rhnSQL.prepare(select_sql)
        query.execute()

        all_subs_in_db = {}
        all_subs_sum   = 0
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
                max_m = data["nodecount"],
                current_m = 0
            )
            rhnSQL.commit()

        # copy database subscription data to a dict and
        # count all subscriptions over all org_id's
        for f in result:
            # all_subs_in_db[ org_id ] = { "max_members" : NUM, "current_members" : NUM, "dirty" : 0 }
            if not all_subs_in_db.has_key( f[1] ):
                all_subs_in_db[ f[1] ] = { "max_members" : 0, "current_members" : f[2], "dirty" : 0 }
            all_subs_in_db[ f[1] ]["max_members"] += f[0]
            all_subs_sum += f[0]

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
        log_debug(1, "NCC says: %s, DB says: %s for channel family %s" % (data["nodecount"], all_subs_sum, cf_id) )
        for org in sorted(all_subs_in_db.keys()):
            log_debug(1, "working on org_id %s" % org)
            free = all_subs_in_db[ org ]["max_members"] - all_subs_in_db[ org ]["current_members"]
            if (free > 0 and needed_subscriptions <= free) or (free < 0 and needed_subscriptions < 0):
                log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], needed_subscriptions) )
                all_subs_in_db[ org ]["max_members"] -= needed_subscriptions
                all_subs_in_db[ org ]["dirty"] = 1
                needed_subscriptions = 0
                break
            elif free > 0 and needed_subscriptions > free:
                log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], free) )
                needed_subscriptions -= free
                all_subs_in_db[ org ]["max_members"] -= free
                all_subs_in_db[ org ]["dirty"] = 1
        if needed_subscriptions > 0:
            # we reduced all max_members but still don't have enough
            # That means, we use more subscriptions than we have in NCC
            self.error_msg("More subscriptions used than registered in NCC. Left subscriptions: %s for family %s" % (needed_subscriptions, cf_id) )
            for org in all_subs_in_db.keys():
                free = all_subs_in_db[ org ]["max_members"]
                if free > 0 and needed_subscriptions <= free:
                    log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], needed_subscriptions) )
                    all_subs_in_db[ org ]["max_members"] -= needed_subscriptions
                    all_subs_in_db[ org ]["dirty"] = 1
                    needed_subscriptions = 0
                    break
                elif free > 0 and needed_subscriptions > free:
                    log_debug(1, "max_members (%s) -= %s" % (all_subs_in_db[ org ]["max_members"], free) )
                    needed_subscriptions -= free
                    all_subs_in_db[ org ]["max_members"] -= free
                    all_subs_in_db[ org ]["dirty"] = 1
        if needed_subscriptions > 0:
            self.error_msg("still too many subscripts are in use. No solution found: left subscriptions: %s" % needed_subscriptions)

        for org in all_subs_in_db.keys():
            if all_subs_in_db[ org ]["dirty"] == 1:
                update_sql = """
                    UPDATE RHNPRIVATECHANNELFAMILY SET
                    max_members = :max_m
                    WHERE channel_family_id = :cf_id and org_id = :org_id
                    """
                query = rhnSQL.prepare(update_sql)
                log_debug(2, "SQL: " % query )
                query.execute(
                    max_m = all_subs_in_db[ org ]["max_members"],
                    org_id = org,
                    cf_id = cf_id
                )
        rhnSQL.commit()



    def repo_sync(self):
        """Trigger a reposync of all the channels in the database

        """
        self.print_msg("Triggering reposync of all database channels...")

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
        with open(NCC_CHANNELS, 'r') as f:
            tree = etree.parse(f)
        channels_iter = tree.getroot()

        families = self.get_available_families()

        # filter out the channels which are not in the available families list
        channels = filter(lambda c: c.get('family') in families,
                          channels_iter)

        # filter out the channels whose parent isn't also in the channels list
        c_labels = [c.get('label') for c in channels]
        parents = []
        for channel in channels:
            parent = channel.get('parent')
            if parent == 'BASE' or parent in c_labels:
                parents.append(parent)

        return parents

    def list_channels(self):
        """List available channels on NCC and if they are in sync with the db

        Statuses mean:
            - P - channel is in sync (provided)
            - . - channel is not in sync

        """
        print "Listing all mirrorable channels..."
        db_channels = rhnSQL.Table("RHNCHANNEL", "LABEL").keys()

        ncc_channels = [c.get('label') for c in
                             self.get_available_channels()]
        for channel in ncc_channels:
            if channel in db_channels:
                print "[P] %s" % channel
            else:
                print "[.] %s" % channel

    def get_ncc_channel(self, channel_label):
        """Try getting the NCC channel for this user"""
        for channel in self.get_available_channels():
            if channel.get('label') == channel_label:
                return channel

        # if we got this far, the channel is not available for this user
        raise ChannelNotAvailableError(channel_label)

    def get_parent_id(self, channel_label):
        if channel_label == 'BASE':
            return None
        else:
            return rhnSQL.Row("RHNCHANNEL", "LABEL", channel_label)['id']

    def insert_repo(self, repo, channel_id):
        """Insert an XML repo into the database as a ContentSource

        :arg repo:       repository information from channels.xml
        :arg channel_id: DB id of the channel the repo should belong to

        """
        query = rhnSQL.prepare(
            "SELECT LABEL FROM RHNCONTENTSOURCE WHERE LABEL = :label")
        query.execute(label=repo.get('label'))
        if not query.fetchone():
            # FIXME make a TYPE_ID for zypper?
            query = rhnSQL.prepare(
                """INSERT INTO RHNCONTENTSOURCE
                       ( ID, ORG_ID, TYPE_ID, SOURCE_URL, LABEL)
                   VALUES ( sequence_nextval('rhn_chan_content_src_id_seq'),
                            NULL, 500, :source_url, :label )""")
            query.execute(**repo.attrib)

            # create relation between the new repo and the channel
            repo_id = rhnSQL.Row(
                "RHNCONTENTSOURCE", "LABEL", repo.get('label'))['id']
            query = rhnSQL.prepare(
            """INSERT INTO RHNCHANNELCONTENTSOURCE (SOURCE_ID, CHANNEL_ID)
               VALUES (:source_id, :channel_id)""")
            query.execute(
                source_id = repo_id,
                channel_id = channel_id)

    def add_channel(self, channel_label):
        """Add a new channel to the database

        :arg channel_label: the label of the channel that should be added

        """
        # first look in the db to see if it's already there
        query = rhnSQL.prepare(
            "SELECT LABEL FROM RHNCHANNEL WHERE LABEL = :label")
        query.execute(label=channel_label)

        if query.fetchone():
            self.print_msg( "Channel %s is already in the database." % channel_label )
        else:
            channel = self.get_ncc_channel(channel_label)
            query = rhnSQL.prepare(
                """INSERT INTO RHNCHANNEL ( ID, BASEDIR, PARENT_CHANNEL,
                                            CHANNEL_ARCH_ID, LABEL, NAME,
                                            SUMMARY, DESCRIPTION )
                   VALUES ( sequence_nextval('rhn_channel_id_seq'), '/dev/null',
                           :parent_channel, :channel_arch_id, :label, :name,
                           :summary, :description )""")
            query.execute(
                parent_channel = self.get_parent_id(channel.get('parent')),
                channel_arch_id = rhnSQL.Row(
                    "RHNCHANNELARCH", "LABEL", "channel-%s" %
                    channel.get('arch'))['id'],
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

            for repo in channel:
                self.insert_repo(repo, channel_id)
                
            rhnSQL.commit()
            self.print_msg( "Added channel '%s' to the database." % channel_label )

        # TODO do the syncing
        self.print_msg( "Trigger sync for channel '%s'" % channel_label )

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

def main():
    parser = OptionParser(version="%prog 0.1",
                          description="Sync SUSE Manager repositories from NCC")

    parser.add_option("-l", "--list-channels", action="store_true", dest="list",
                      help="list all the channels which are available for you")
    parser.add_option("-c", "--channel", action="store",
                      help="add a new channel and trigger a reposync")
    parser.add_option("-p", "--products", action="store_true",
                      help="fetch all known products from NCC")
    parser.add_option("-f", "--update_cf", action="store_true",
                      help="update channel family by XML config")
    parser.add_option("-s", "--update_subscriptions", action="store_true",
                      help="update subscriptions by NCC data")
    parser.add_option('-q', '--quiet', action='store_true', dest='quiet', 
                      default=False, help="Print no output, still logs output")

    (options, args) = parser.parse_args()

    syncer = NCCSync(quiet=options.quiet)
    if options.list:
        syncer.list_channels()
    elif options.channel:
        syncer.add_channel(options.channel)
    elif options.products:
        suseProducts = syncer.get_suse_products_from_ncc()
        syncer.update_suse_products_table( suseProducts )
    elif options.update_cf:
        syncer.update_channel_family_table_by_config()
    elif options.update_subscriptions:
        all_subs = syncer.get_subscriptions_from_ncc()
        cons_subs = syncer.consolidate_subscriptions( all_subs )
        for s in cons_subs.keys():
            syncer.edit_subscription_in_table( s, cons_subs[s] )
    else:
        syncer.update_channel_family_table_by_config()
        suseProducts = syncer.get_suse_products_from_ncc()
        syncer.update_suse_products_table( suseProducts )
        all_subs = syncer.get_subscriptions_from_ncc()
        cons_subs = syncer.consolidate_subscriptions( all_subs )
        for s in cons_subs.keys():
            syncer.edit_subscription_in_table( s, cons_subs[s] )
        syncer.repo_sync()

if __name__ == "__main__":
    try:
        main()
    except IOError, e:
        print "ERROR: %s" % e


