#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2012 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
#

import sys

# pylint: disable-next=unused-import
from spacewalk.common import rhnFlags
from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.server import rhnSQL
from spacewalk.server.rhnChannel import subscribe_channels, unsubscribe_channels

__rhnexport__ = ["upgrade"]

_query_dup_data = rhnSQL.Statement(
    """
    SELECT id, dry_run
      FROM rhnActionDup
     WHERE action_id = :action_id
"""
)

_query_channel_changes = rhnSQL.Statement(
    """
    SELECT c.id, c.label, c.parent_channel, adc.task
      FROM rhnActionDupChannel adc
      JOIN rhnChannel c ON adc.channel_id = c.id
     WHERE adc.action_dup_id = :action_dup_id
"""
)


# pylint: disable-next=invalid-name,dangerous-default-value,unused-argument
def upgrade(serverId, actionId, data={}):
    log_debug(3)

    h = rhnSQL.prepare(_query_dup_data)
    h.execute(action_id=actionId)
    row = h.fetchone_dict() or None
    if not row:
        log_error("Unable to find action data")
        return

    if row["dry_run"] == "Y":
        _restore_channels(serverId, row["id"])


# pylint: disable-next=invalid-name
def _restore_channels(serverId, action_dup_id):
    log_debug(3)
    h = rhnSQL.prepare(_query_channel_changes)
    h.execute(action_dup_id=action_dup_id)
    channel_changes = h.fetchall_dict() or None

    if not channel_changes:
        # something goes wrong
        log_error("nothing to rollback for channels")
        return

    # we need to rollback the changes from action
    # therefore unsubscribe task 'S' and
    # subscribe task 'U'
    to_unsubscribe = [x for x in channel_changes if x["task"] == "S"]
    to_subscribe = [x for x in channel_changes if x["task"] == "U"]

    try:
        unsubscribe_channels(serverId, to_unsubscribe)
        subscribe_channels(serverId, to_subscribe)
    # pylint: disable-next=broad-exception-caught
    except Exception as e:
        log_error(str(e), sys.exc_info()[2])

    return
