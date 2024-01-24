#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2016 Red Hat, Inc.
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
#

from spacewalk.common.rhnLog import log_debug
from spacewalk.server import rhnSQL
from spacewalk.server.rhnLib import InvalidAction

# the "exposed" functions
__rhnexport__ = ["update"]

# returns a list of errata scheduled for this action


# pylint: disable-next=invalid-name,unused-argument
def update(serverId, actionId, dry_run=0):
    log_debug(3)
    statement = """
        select r1.errata_id, r2.allow_vendor_change, e.advisory_status
        from rhnactionerrataupdate r1
        join rhnErrata e on r1.errata_id = e.id
        left join rhnactionpackagedetails r2 on r1.action_id = r2.action_id
        where r1.action_id = :action_id
    """
    h = rhnSQL.prepare(statement)
    h.execute(action_id=actionId)
    ret = h.fetchall_dict()
    if not ret:
        # No errata for this action
        raise InvalidAction(
            # pylint: disable-next=consider-using-f-string
            "errata.update: Unknown action id "
            "%s for server %s" % (actionId, serverId)
        )

    retracted = [x["errata_id"] for x in ret if x["advisory_status"] == "retracted"]
    if retracted:
        # Do not install retracted patches
        raise InvalidAction(
            # pylint: disable-next=consider-using-f-string
            "errata.update: Action contains retracted errata %s"
            % retracted
        )
    if ret[0]["allow_vendor_change"] is None or ret[0]["allow_vendor_change"] is False:
        return [x["errata_id"] for x in ret]

    return {
        "errata_ids": [x["errata_id"] for x in ret],
        "allow_vendor_change": (ret[0]["allow_vendor_change"] == "Y"),
    }
