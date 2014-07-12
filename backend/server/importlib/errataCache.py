#
# Copyright (c) 2008--2013 Red Hat, Inc.
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
# Adds tasks to be executed by the errata cache daemon
#

from spacewalk.server import rhnSQL

def schedule_errata_cache_update(channels):
    # If no channels were supplied, exit here to shortcut parsing the query
    if not channels:
        return
    h = rhnSQL.prepare("""
        insert into rhnTaskQueue
       (org_id, task_name, task_data, priority, earliest)
       select coalesce(c.org_id, 1), 'update_errata_cache_by_channel', c.id, 0, current_timestamp
       from rhnChannel c
       where c.label = :label
    """)
    h.executemany(label=channels)
    rhnSQL.commit()
