# -*- coding: utf-8 -*-
'''
Some description
'''
from __future__ import absolute_import

# Don't "fix" the above docstring to put it on two lines, as the sphinx
# autosummary pulls only the first line for its description.

# Import python libs
import logging
try:
    from spacewalk.server import rhnSQL
    RHN_SQL_LOADED = True
except ImportError:
    RHN_SQL_LOADED = False

# Set up logging
log = logging.getLogger(__name__)

# This external pillar will be known as `something_else`
__virtualname__ = 'suma_groups'


def __virtual__():
    if RHN_SQL_LOADED:
        return __virtualname__
    return False


def ext_pillar(minion_id,  # pylint: disable=W0613
               pillar,  # pylint: disable=W0613
               command):
    '''
    Docstring
    '''

    groups = {}
    rhnSQL.initDB()  # TODO salt user must have read access to /etc/rhn/rhn.conf

    rows = rhnSQL.fetchall_dict("""select g.id as id
    from rhnservergroup g, rhnservergroupmembers gm, suseminioninfo m
    where g.id=gm.server_group_id and gm.server_id=m.server_id
    and m.minion_id=:minion_id""",
                         minion_id=minion_id)

    if rows:
        groups['suma_group_ids'] = [row['id'] for row in rows]
    else:
        log.warn("No groups found for minion_id=" + minion_id)

    return groups
