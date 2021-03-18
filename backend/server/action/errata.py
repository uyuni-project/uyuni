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
__rhnexport__ = ['update']

# returns a list of errata scheduled for this action


def update(serverId, actionId, dry_run=0):
    log_debug(3)
    statement = """
        select r1.errata_id, r2.allow_vendor_change
        from rhnactionerrataupdate r1
        left join rhnactionpackagedetails r2 on r1.action_id = r2.action_id
        where r1.action_id = :action_id
    """
    h = rhnSQL.prepare(statement)
    h.execute(action_id=actionId)
    ret = h.fetchall_dict()
    if not ret:
        # No errata for this action
        raise InvalidAction("errata.update: Unknown action id "
                            "%s for server %s" % (actionId, serverId))

    if ret[0]['allow_vendor_change'] is null or ret[0]['allow_vendor_change'] is False:
        return [x['errata_id'] for x in ret]

    return {
        "errata_ids" : [x['errata_id'] for x in ret],
        "allow_vendor_change" : (ret[0]['allow_vendor_change'] == 'Y')
    }
