#  pylint: disable=missing-module-docstring
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
from spacewalk.susemanager import package_helper

# pylint: disable=invalid-name


def deleteChannelErrata(errata_id, channel_id):
    """Remove errata from channel"""
    h = rhnSQL.prepare(
        """
        DELETE FROM rhnChannelErrata
         WHERE errata_id = :errata_id
           AND channel_id = :channel_id
    """
    )
    return h.execute(errata_id=errata_id, channel_id=channel_id)


def errataHasChannels(errata_id):
    """Looks if errata is referenced by some channel"""
    h = rhnSQL.prepare(
        """
        SELECT channel_id
          FROM rhnChannelErrata
         WHERE errata_id = :errata_id
    """
    )
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
      * delete all entries from rhnErrataCloned related with the errata
      * finally delete errata from rhnErrata.
    """

    # delete all packages from errata
    h = rhnSQL.prepare(
        """
        SELECT rhnPackage.id FROM rhnPackage
         LEFT OUTER JOIN rhnErrataPackage ep on ep.package_id = rhnPackage.id
         WHERE ep.errata_id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)

    package_ids = [x["id"] for x in h.fetchall_dict() or []]
    for package_id in package_ids:
        package_helper.delete_package(package_id)

    h = rhnSQL.prepare(
        """
        DELETE FROM rhnErrataPackage ep
         WHERE ep.errata_id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)

    # delete files from errata
    h = rhnSQL.prepare(
        """
        DELETE FROM rhnErrataFile
         WHERE errata_id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)

    # delete errata
    # removed also references from rhnErrataCloned
    # and rhnServerNeededCache
    h = rhnSQL.prepare(
        """
        DELETE FROM rhnErrata
         WHERE id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)


def findErrataByAdvisory(advisory):
    """
    Search the errata using the given advisory.
    Returns None if the errata is not found, otherwise returns the ID of the errata.
    """
    h = rhnSQL.prepare(
        """
        SELECT id
          FROM rhnErrata
         WHERE advisory = :advisory
    """
    )
    h.execute(advisory=advisory)
    res = h.fetchone_dict() or None
    if res:
        return res["id"]
    else:
        return None


def channelContainsErrata(channel_id, errata_id):
    """Returns True if the errata is contained by the specified channel, false otherwise."""

    h = rhnSQL.prepare(
        """
        SELECT channel_id
          FROM rhnChannelErrata
         WHERE errata_id = :errata_id AND channel_id = :channel_id
    """
    )
    h.execute(channel_id=channel_id, errata_id=errata_id)
    res = h.fetchone_dict() or None

    return res is not None


def channelsWithErrata(errata_id):
    """Return a List containing the IDs of the channels containing the errata."""

    h = rhnSQL.prepare(
        """
        SELECT channel_id
          FROM rhnChannelErrata
         WHERE errata_id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)

    return [x["channel_id"] for x in h.fetchall_dict() or []]


def findErrataClones(errata_id):
    """Find all the clones of this errata.
    Returns a list containing the IDs of the clones.
    """

    h = rhnSQL.prepare(
        """
        SELECT id from rhnErrataCloned
         WHERE original_id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)

    clones = [x["id"] for x in h.fetchall_dict() or []]
    ret = clones[:]

    for clone in clones:
        ret += findErrataClones(clone)

    return ret


def errataParent(errata_id):
    """
    When the errata has been cloned from another one this function returns
    the ID of the original errata.
    If the errata is **not** a clone, the given errata_id is returned.
    """

    h = rhnSQL.prepare(
        """
        SELECT original_id
          FROM rhnErrataCloned
         WHERE id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)
    res = h.fetchone_dict()

    if res:
        return errataParent(res["original_id"])
    else:
        return errata_id


def getAdvisory(errata_id):
    """Return the advisory of the errata."""

    h = rhnSQL.prepare(
        """
        SELECT advisory
          FROM rhnErrata
         WHERE id = :errata_id
    """
    )
    h.execute(errata_id=errata_id)
    return h.fetchone_dict()["advisory"]
