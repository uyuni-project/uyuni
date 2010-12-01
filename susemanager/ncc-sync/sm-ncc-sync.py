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

from spacewalk.server import rhnSQL
from spacewalk.common import CFG, initCFG

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

        rhnSQL.initDB()

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

    def get_channel_arch_id(self, arch):
        """Return the ARCH_ID of the arch with the :arch: name"""
        return 503

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
                channel_arch_id = self.get_channel_arch_id(channel.get('arch')),
                # XXX - org_id = ??
                **channel.attrib)
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

    (options, args) = parser.parse_args()

    syncer = NCCSync()
    if options.list:
        syncer.list_channels()
    elif options.channel:
        syncer.add_channel(options.channel)
    else:
        syncer.repo_sync()

if __name__ == "__main__":
    main()
