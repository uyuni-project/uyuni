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

# pylint: disable=invalid-name

from spacewalk.server import rhnSQL
from uyuni.common.rhnConfig import CFG
import os


def delete_package(package_id):
    """
    Performs the following operations:
        * remove pacakge from all the associated channels.
        * remove the package from the database.
        * remove rpm from the file system.
    """

    # Remove the package from all the channels
    h = rhnSQL.prepare(
        """
        DELETE FROM rhnChannelPackage
         WHERE package_id = :package_id
    """
    )
    h.execute(package_id=package_id)

    # Retrieve rpm path
    h = rhnSQL.prepare(
        """
        SELECT path FROM rhnPackage
         WHERE id = :package_id
    """
    )
    h.execute(package_id=package_id)
    row = h.fetchone_dict()

    if row["path"]:
        rpm = os.path.join(CFG.mount_point, row["path"])
        if os.path.isfile(rpm):
            os.remove(rpm)

    # Finally remove the package from the database
    h = rhnSQL.prepare(
        """
        DELETE FROM rhnPackage
         WHERE id = :package_id
    """
    )
    h.execute(package_id=package_id)
