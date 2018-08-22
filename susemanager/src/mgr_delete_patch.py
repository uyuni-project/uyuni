# -*- coding: utf-8 -*-
#
# Copyright (c) 2011 Novell, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
from __future__ import print_function

import re
import sys

from spacewalk.common import rhnLog
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnConfig import CFG
from spacewalk.server import rhnSQL
from spacewalk.susemanager import errata_helper

DEFAULT_LOG_LOCATION = '/var/log/rhn/'

try:
   input = raw_input
except NameError:
   pass

class Cleaner:
    def __init__(self, debug):
        self.debug = debug
        rhnLog.initLOG(DEFAULT_LOG_LOCATION + 'mgr-delete-patch.log', self.debug)

        try:
            rhnSQL.initDB()
        except rhnSQL.SQLConnectError as e:
            log_error("Could not connect to the database. %s" % e)
            raise Exception("Could not connect to the database. %s" % e)

    def remove(self, errata):
        """ Remove an errata and all its clones """

        clones           = []
        errata_to_remove = {}
        errata_id        = errata_helper.findErrataByAdvisory(errata)

        if not errata_id:
            log_error("Cannot find patch with advisory {0}".format(errata))
            return
        else:
            log_debug(0, "Patch {0} found".format(errata))

        parent_errata_id = errata_helper.errataParent(errata_id)
        parent_advisory  = None
        if parent_errata_id != errata_id:
            parent_advisory  = errata_helper.getAdvisory(parent_errata_id)
            clones           = errata_helper.findErrataClones(parent_errata_id)

            _printLog("{0} is a clone of {1}".format(errata, parent_advisory))
            print("The tool is going to remove '{0}' and all its clones:".format(parent_advisory))
        else:
            clones = errata_helper.findErrataClones(errata_id)
            print("The tool is going to remove '{0}' and all its clones:".format(errata))

        for id in clones:
            clone_advisory       = errata_helper.getAdvisory(id)
            errata_to_remove[id] = clone_advisory
            print("  -", clone_advisory)

        reply = None
        while not reply in ('y', 'n'):
            reply = input("Do you want to continue? (Y/n) ")
            if not reply:
                reply = "y"
            reply = reply.lower()
        if reply == 'n':
            _printLog('User decided to quit.')
            sys.exit(0)

        if parent_errata_id:
            errata_to_remove[parent_errata_id] = parent_advisory
        else:
            errata_to_remove[errata_id] = errata

        for id in errata_to_remove:
            self.__remove_errata(id, errata_to_remove[id])

        rhnSQL.commit()
        _printLog("Finished")

    def __remove_errata(self, errata_id, advisory):
        """ Remove an errata. """

        channel_ids = errata_helper.channelsWithErrata(errata_id)

        for channel_id in channel_ids:
            _printLog("Removing '{0}' patch from channel '{1}'".format(advisory, channel_id))

            # delete errata from channel
            errata_helper.deleteChannelErrata(errata_id, channel_id)

            # Update the errata/package cache for the servers
            # use procedure rhn_channel.update_needed_cache(channel_id)
            log_debug(2, "Update Server Cache for channel '{0}'".format(channel_id))
            rhnSQL.commit()
            update_needed_cache = rhnSQL.Procedure("rhn_channel.update_needed_cache")
            update_needed_cache(channel_id)
            rhnSQL.commit()

        errata_helper.deleteErrata(errata_id)

    def __findChannel(self, channel):
        """
        Search the channel using the given label.
        Returns None if the channel is not found, otherwise returns the ID of the channel.
        """
        h = rhnSQL.prepare("""
            SELECT id
              FROM rhnChannel
             WHERE label = :channel
        """)
        h.execute(channel=channel)
        res = h.fetchone_dict() or None
        if res:
            return res['id']
        else:
            return None

def _printLog(msg):
    log_debug(0, msg)
    print(msg)

