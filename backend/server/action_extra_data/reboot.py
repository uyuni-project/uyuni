#
# Copyright (c) 2008--2011 Red Hat, Inc.
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

from spacewalk.common import rhnFlags
from spacewalk.common.rhnLog import log_debug
from spacewalk.server.rhnServer import server_kickstart
from spacewalk.server import rhnSQL

# the "exposed" functions
__rhnexport__ = ['reboot']

_query_queue_actions_soon = rhnSQL.Statement("""
   select sa.action_id id
     from rhnServerAction sa,
          rhnAction a
    where sa.server_id = :server_id
      and sa.action_id = a.id
      and sa.status in (0, 1) -- Queued or picked up
      and a.earliest_action <= current_timestamp + numtodsinterval(6 * 60, 'second') -- Check earliest_action
      and not exists (
          select 1
            from rhnServerAction sap
           where sap.server_id = :server_id
             and sap.action_id = a.prerequisite
             and sap.status != 2 -- completed
          )
""")

_update_earliest_action = rhnSQL.Statement("""
    update rhnAction
       set earliest_action = current_timestamp + numtodsinterval(6 * 60, 'second')
     where id = :action_id
""")

def reboot(server_id, action_id, data={}):
    log_debug(3, action_id)

    # after a reboot re-schedule following actions to be executed
    # not sooner than 6 minutes after reboot action is finished
    # 3 minutes to let the reboot really begin
    # and 3 more minutes to be sure the server is really down
    # or up again
    h = rhnSQL.prepare(_query_queue_actions_soon)
    h.execute(server_id=server_id)
    action = h.fetchone_dict()
    s = rhnSQL.prepare(_update_earliest_action)
    while action:
        log_debug(5, action)
        s.execute(action_id=action['id'])
        action = h.fetchone_dict()

    action_status = rhnFlags.get('action_status')
    server_kickstart.update_kickstart_session(server_id, action_id,
        action_status, kickstart_state='restarted',
        next_action_type=None)

