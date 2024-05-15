#  pylint: disable=missing-module-docstring
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
from uyuni.common.rhnConfig import CFG, initCFG
from spacewalk.server import rhnSQL
from spacewalk.susemanager import errata_helper

# pylint: disable=invalid-name

DEFAULT_LOG_LOCATION = "/var/log/rhn/"


# pylint: disable-next=missing-class-docstring
class Cleaner(object):  # pylint: disable=too-few-public-methods
    def __init__(
        self, all=False, channel=None, debug=0
    ):  # pylint: disable=redefined-builtin
        self.all = all
        self.channel = channel
        self.debug = debug

        if not all and not channel:
            print("You need to specify either --all or --channel")
            sys.exit(1)

        initCFG("server.susemanager")
        if self.debug == 0:
            self.debug = CFG.DEBUG

        rhnLog.initLOG(
            DEFAULT_LOG_LOCATION + "mgr-clean-old-patchnames.log", self.debug
        )

        try:
            rhnSQL.initDB()
        except rhnSQL.SQLConnectError as e:
            # pylint: disable-next=consider-using-f-string
            log_error("Could not connect to the database. %s" % e)
            # pylint: disable-next=raise-missing-from,broad-exception-raised,consider-using-f-string
            raise Exception("Could not connect to the database. %s" % e)

    def run(self):
        channels = []
        if self.all:
            channels = list(rhnSQL.Table("RHNCHANNEL", "LABEL").keys())
        else:
            channels = [self.channel]

        for c in channels:
            # pylint: disable-next=consider-using-f-string
            _printLog("Remove old patches in channel '%s'" % c)
            # search errata which ends with channel-* in this channel
            h = rhnSQL.prepare(
                """
                SELECT e.id as errata_id,
                       e.advisory,
                       e.advisory_rel,
                       c.id as channel_id,
                       ca.label channel_arch_label
                  FROM rhnErrata e
                  JOIN rhnChannelErrata ce ON e.id = ce.errata_id
                  JOIN rhnChannel c ON ce.channel_id = c.id
                  JOIN rhnChannelArch ca ON c.channel_arch_id = ca.id
                 WHERE c.label = :channel
            """
            )
            h.execute(channel=c)
            patches = h.fetchall_dict() or []
            channel_id = None
            for patch in patches:
                # pylint: disable-next=consider-using-f-string
                pattern = "-%s-%s-?[0-9]*$" % (
                    patch["advisory_rel"],
                    patch["channel_arch_label"],
                )
                if not re.search(pattern, patch["advisory"]):
                    # pylint: disable-next=consider-using-f-string
                    log_debug(2, "Found new style patch '%s'. Skip" % patch["advisory"])
                    # This is not an old style patch. Skip
                    continue
                errata_id = patch["errata_id"]
                channel_id = patch["channel_id"]
                log_debug(
                    1,
                    # pylint: disable-next=consider-using-f-string
                    "Remove patch '%s(%d)' from channel '%s(%d)'"
                    % (patch["advisory"], errata_id, c, channel_id),
                )

                # delete channel from errata
                errata_helper.deleteChannelErrata(errata_id, channel_id)

                # search if the errata still has channels
                if errata_helper.errataHasChannels(errata_id):
                    # if yes, work on this patch is finished
                    log_debug(2, "Patch exists in other channels too")
                    continue

                # else we can remove the errta completly
                log_debug(2, "Delete Patch completly")
                errata_helper.deleteErrata(errata_id)

            # if channel_id is still None, no patches were deleted
            # Then is no need to run update_needed_cache for this channel
            if channel_id:
                # Update the errata/package cache for the servers
                #        use procedure rhn_channel.update_needed_cache(channel_id)
                # pylint: disable-next=consider-using-f-string
                log_debug(2, "Update Server Cache for channel '%s'" % c)
                rhnSQL.commit()
                update_needed_cache = rhnSQL.Procedure(
                    "rhn_channel.update_needed_cache"
                )
                update_needed_cache(channel_id)
                rhnSQL.commit()
            else:
                # pylint: disable-next=consider-using-f-string
                log_debug(1, "No old style patches found in '%s'" % c)

        _printLog("Finished")


def _printLog(msg):
    log_debug(0, msg)
    print(msg)
