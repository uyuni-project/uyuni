# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 Novell, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

from spacewalk.server import rhnSQL

def deleteChannelErrata(errata_id, channel_id):
    """ Remove errata from channel """
    h = rhnSQL.prepare("""
        DELETE FROM rhnChannelErrata
         WHERE errata_id = :errata_id
           AND channel_id = :channel_id
    """)
    return h.execute(errata_id=errata_id, channel_id=channel_id)

def errataHasChannels(errata_id):
    """ Looks if errata is referenced by some channel """
    h = rhnSQL.prepare("""
        SELECT channel_id
          FROM rhnChannelErrata
         WHERE errata_id = :errata_id
    """)
    h.execute(errata_id=errata_id)
    res = h.fetchone_dict() or None
    if res:
        return True
    else:
        return False

def deleteErrata(errata_id):
    """
    Remove errata from database

    This takes care of the following operations:
      * delete all the packages associated with the errata from rhnErrataPackage
      * delete all the files associated with the errata from rhnErrataFile
      * delete all entries from rhnErrataTmp related with the errata
      * delete all entries from rhnErrataCloned related with the errata
      * finally delete errata from rhnErrata.
    """

    # delete all packages from errata
    h = rhnSQL.prepare("""
        DELETE FROM rhnErrataPackage ep
         WHERE ep.errata_id = :errata_id
    """)
    h.execute(errata_id=errata_id)

    # delete files from errata
    h = rhnSQL.prepare("""
        DELETE FROM rhnErrataFile
         WHERE errata_id = :errata_id
    """)
    h.execute(errata_id=errata_id)

    # delete erratatmp
    h = rhnSQL.prepare("""
        DELETE FROM rhnErrataTmp
         WHERE id = :errata_id
    """)
    h.execute(errata_id=errata_id)

    # delete errata
    # removed also references from rhnErrataCloned
    # and rhnServerNeededCache
    h = rhnSQL.prepare("""
        DELETE FROM rhnErrata
         WHERE id = :errata_id
    """)
    h.execute(errata_id=errata_id)

