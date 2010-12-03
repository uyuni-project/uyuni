#!/usr/bin/python
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
import xml.etree.ElementTree as etree
from optparse import OptionParser
from datetime import date

from spacewalk.server import rhnSQL
from spacewalk.common import CFG, initCFG

from spacewalk.susemanager import suseLib

NCC_CHANNELS = 'channels.xml'

class ChannelNotAvailableError(Exception):
    def __init__(self, channel_label):
        self.channel = channel_label
    def __str__(self):
        return "You do not have access to the channel: %s" % self.channel


class NCCSync(object):
    """This class is used to sync SUSE Manager Channels and NCC repositories"""

    def __init__(self):
        """Setup configuration"""
        initCFG("server.satellite")

        # FIXME: move static values to config file
        self.authuser = ""
        self.authpass = ""
        self.smtguid  = ""
        #self.authuser = CFG.mirrcred_user
        #self.authpass = CFG.mirrcred_pass


        self.namespace = "http://www.novell.com/xml/center/regsvc-1_0"

        # FIXME:
        # self.ncc_url_prods = CFG.reg_url + "/?command=regdata&lang=en-US&version=1.0"
        # self.ncc_url_subs  = CFG.reg_url + "/?command=listsubscriptions&lang=en-US&version=1.0"
        self.ncc_url_prods = "https://secure-www.novell.com/center/regsvc/?command=regdata&lang=en-US&version=1.0"
        self.ncc_url_subs  = "https://secure-www.novell.com/center/regsvc/?command=listsubscriptions&lang=en-US&version=1.0"
        self.connect_retries = 10
        # FIXME: the path needs to be fixed
        self.channel_family_config = '/tmp/channel_families.xml'

        rhnSQL.initDB()

    def _connect_ncc( self, url_in, send=None ):
        """Connect the ncc with the given URL. 
           Do a post-request when "send" is given. 
           Return a filedescriptor"""
        new_url = url_in
        try_counter = self.connect_retries
        while new_url != "" and try_counter > 0:
            try_counter -= 1
            o = urllib.URLopener()
            try:
                f = o.open( new_url, send)
                new_url = ""
            except IOError, e:
                # 302 is a redirect
                if e[1] == 302:
                    new_url = e[3].dict["location"]
                elif e[1] == 504:
                    # gateway timeout - try again
                    pass
                else:
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
        send = '<?xml version="1.0" encoding="UTF-8"?>'
        send = send + ("<productdata xmlns=\"%s\" client_version=\"1.2.3\" lang=\"en\">" % self.namespace)
        send = send + "<authuser>%s</authuser>" % self.authuser
        send = send + "<authpass>%s</authpass>" % self.authpass
        send = send + "<smtguid>%s</smtguid>" % self.smtguid
        send = send + "</productdata>\n"

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
    def consolidate_subscriptions( self, subs ):
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
                if s["nodecount"] == "-1":
                    # FIXME: -1 is unlimited
                    subscription_count[ p ] = { "consumed" : int(s["consumed"]), "nodecount" : 200000 }
                elif today >= start_date and (end == 0 or today <= end_date) and s["type"] != "PROVISIONAL":
                    if subscription_count.has_key( p ):
                        subscription_count[ p ]["nodecount"] += int(s["nodecount"])
                    else:
                        subscription_count[ p ] = { "consumed" : int(s["consumed"]), "nodecount" : int(s["nodecount"]) }

        return subscription_count

#   OUT: array of dicts with all keys the NCC has for a product
    def get_suse_products_from_ncc(self):
        """returns all products known by NCC"""
        send = '<?xml version="1.0" encoding="UTF-8"?>'
        send = send + ("<productdata xmlns=\"%s\" client_version=\"1.2.3\" lang=\"en\">" % self.namespace)
        send = send + "<authuser>%s</authuser>" % self.authuser
        send = send + "<authpass>%s</authpass>" % self.authpass
        send = send + "<smtguid>%s</smtguid>" % self.smtguid
        send = send + "</productdata>\n"

        f = self._connect_ncc( self.ncc_url_prods, send )

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

    def get_arch_id( self, arch_in ):
        """returns the database id of an arch"""
        arch_type_id = None

        if arch_in != None:
            select_sql = "SELECT id from RHNPACKAGEARCH where LABEL = :arch"
            query = rhnSQL.prepare(select_sql)
            query.execute( arch = arch_in )
            row = query.fetchone_dict() or {}
            if row:
                arch_type_id = row["id"]
        return arch_type_id

    def get_channel_family_id( self, label_in ):
        """returns the id of a channel_family or None if not existend"""
        select_sql = "SELECT id from RHNCHANNELFAMILY where LABEL = :label"
        query = rhnSQL.prepare(select_sql)
        query.execute( label = label_in )
        row = query.fetchone_dict() or {}
        if row:
            return row["id"]
        return None

    def update_channel_family_table_by_config( self ):
        """updates the channel_family table on base of the XML config file"""
        tree = etree.parse( self.channel_family_config )
        for family in tree.getroot():
            name = family.get("name")
            label = family.get("label")
            self.edit_channel_family_table( label, name )

    def add_channel_family_row( self, label_in, name_in=None, org_id_in=1, url_in="some url" ):
        """insert a new channel_family row"""
        channel_family_id = self.get_channel_family_id( label_in )
        if name_in == None:
            name_in = label_in
        if channel_family_id == None:
            insert_sql = """
            INSERT INTO RHNCHANNELFAMILY
                ( id, org_id, name, label, product_url )
            VALUES
                ( sequence_nextval('rhn_channel_family_id_seq'), :org_id, :name, :label, :product_url )
            """
            query = rhnSQL.prepare(insert_sql)
            query.execute(
                org_id = org_id_in,
                name = name_in,
                label = label_in,
                product_url = url_in
            )
            select_sql = "SELECT id from RHNCHANNELFAMILY where LABEL = :label"
            query = rhnSQL.prepare(select_sql)
            query.execute( label = label_in )
            row = query.fetchone_dict() or {}
            channel_family_id = row["id"]
            rhnSQL.commit()
        return channel_family_id

    # FIXME: only given values should be updated in DB
    def update_channel_family_table( self, label_in, name_in=None, org_id_in=1, url_in="some url" ):
        """update an existing channel_family row"""
        channel_family_id = self.get_channel_family_id( label_in )
        if name_in == None:
            name_in = label_in
        if channel_family_id != None:
            update_sql = """UPDATE RHNCHANNELFAMILY 
                            SET name        = :name,
                                label       = :label,
                                org_id      = :org_id,
                                product_url = :product_url
                            WHERE id = :id"""
            query = rhnSQL.prepare(update_sql)
            query.execute( id = channel_family_id,
                           name = name_in,
                           label = label_in,
                           org_id = org_id_in,
                           product_url = url_in
            )
            rhnSQL.commit()
        return channel_family_id

    def edit_channel_family_table( self, label_in, name_in=None, org_id_in=1, url_in="some url" ):
        """returns the id of a channel_family.
           If not existing, a new one is created
           If existing, it's getting updated"""
        channel_family_id = self.get_channel_family_id( label_in )
        if name_in == None:
            name_in = label_in

        if channel_family_id == None:
            channel_family_id = self.add_channel_family_row( label_in, name_in, org_id_in, url_in )
        else:
            self.update_channel_family_table( label_in, name_in, org_id_in, url_in )
        return channel_family_id

    def update_suse_products_table( self, suseProducts ):
        """expects a get_suse_products_from_ncc() datastructure and creates/updates entries in DB"""
        for p in suseProducts:
            arch_type_id = self.get_arch_id( p["ARCH"] )

            # FIXME: maybe better get_channel_family_id()
            channel_family_id = self.edit_channel_family_table( p["PRODUCT_CLASS"] )

            # NAME+VERSION+RELEASE+ARCH are uniq
            select_sql = """SELECT id from SUSEPRODUCTS
                            where name       = :name
                            and version      = :version
                            and arch_type_id = :arch
                            and release      = :release"""
            query = rhnSQL.prepare(select_sql)
            query.execute(
                name = p["PRODUCT"],
                version = p["VERSION"],
                arch = arch_type_id,
                release = p["REL"]
            )
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
                                (sequence_nextval('suse_products_id_seq'), :name, :version, 
                                 :friendly_name, :arch_type_id, :release, 
                                 :channel_family_id, :product_list)
                """

                query = rhnSQL.prepare(insert_sql)
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
        """updates/inserts a subscription in the DB. 
           Expects a product like from consolidate_subscriptions()"""

        # FIXME: maybe better get_channel_family_id()
        cf_id = self.edit_channel_family_table( prod )

        select_sql = "SELECT 1 from RHNPRIVATECHANNELFAMILY where channel_family_id = %s" % cf_id
        query = rhnSQL.prepare(select_sql)
        query.execute()
        row = query.fetchone_dict() or {}
        if row:
            update_sql = """
                UPDATE RHNPRIVATECHANNELFAMILY SET
                max_members = :max_m, current_members = :c_m
                WHERE channel_family_id = :cf_id
            """
            query = rhnSQL.prepare(update_sql)
            query.execute(
                max_m = data["nodecount"],
                c_m = 0
            )

        else:
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



    def get_db_channel_labels(self):
        """Return all the current channel names from SUSE Manager"""
        h = rhnSQL.prepare("SELECT LABEL FROM RHNCHANNEL")
        h.execute()
        channels = h.fetchall()

        # return just a list of labels, not a list of one-value tuples
        return [tup[0] for tup in channels]

    def repo_sync(self):
        """Trigger a reposync of all the channels in the database

        """
        print "Triggering reposync of all database channels..."

    def get_available_channels(self):
        """Get a list of all the channels the user has access to

        Returns a list of channel Element objects.

        """
        # get the channels from our config file
        with open(NCC_CHANNELS, 'r') as f:
            tree = etree.parse(f)
        channels_iter = tree.getroot()

        # get all the available channel families from the DB
        query = rhnSQL.prepare("SELECT LABEL FROM RHNCHANNELFAMILY")
        query.execute()
        families = [f[0] for f in query.fetchall()]
        families = ['7262']

        # only retrieve the channels that are in our families
        return filter(lambda channel: channel.get('family') in families,
                      channels_iter)

    def list_channels(self):
        """List available channels on NCC and if they are in sync with the db

        Statuses mean:
            - P - channel is in sync (provided)
            - . - channel is not in sync

        """
        print "Listing all mirrorable channels..."
        db_channels = self.get_db_channel_labels()

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

        raise ChannelNotAvailableError(channel_label)

    def get_channel_id(self, channel_label):
        """Return the ID of the RHNCHANNEL identified by channel_label"""
        query = rhnSQL.prepare("""SELECT ID FROM RHNCHANNEL
                                  WHERE LABEL=:label""")
        query.execute(label=channel_label)
        try:
            return query.fetchone()[0]
        except TypeError:
            raise Exception, ("Channel with label %s was not found in the DB." %
                              channel_label)

    def get_parent_id(self, channel_label):
        if channel_label == 'BASE':
            return None
        else:
            return self.get_channel_id(channel_label)

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
            print "Channel %s is already in the database." % channel_label
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
                    "RHNCHANNELARCH", "LABEL", "channel-%s" % channel.get('arch'))['id'],
                # XXX - org_id = ??
                **channel.attrib)

            # welcome to the family: create relation between the new channel
            # and corresponding channel family

            channel_id = rhnSQL.Row("RHNCHANNEL", "LABEL", channel_label)['id']
            channel_family_id = rhnSQL.Row(
                "RHNCHANNELFAMILY", "LABEL", channel.get('family'))['id']

            query = rhnSQL.prepare(
                """INSERT INTO RHNCHANNELFAMILYMEMBERS (CHANNEL_ID, CHANNEL_FAMILY_ID)
                   VALUES (:channel_id, :channel_family_id)""")
            query.execute(
                channel_id = channel_id,
                channel_family_id = channel_family_id)

            for repo in channel:
                self.insert_repo(repo, channel_id)
                
            rhnSQL.commit()
            print "Added channel '%s' to the database." % channel_label

        # TODO do the syncing
        print "Triggered a database sync for channel '%s'" % channel_label


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

    (options, args) = parser.parse_args()

    syncer = NCCSync()
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
        syncer.repo_sync()

if __name__ == "__main__":
    main()
