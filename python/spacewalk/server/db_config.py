# -*- coding: utf-8 -*-
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only
#

"""A module that reads values from the database"""


from typing import Optional, Union
from spacewalk.server import rhnSQL


def sanitize_value(key: str, val: str) -> Optional[Union[str, float, int]]:
    """
    attempt to convert a string value to the proper type
    """
    convert_table = {
        "PSW_CHECK_SPECIAL_CHARACTERS": str,
    }
    val = val.strip()

    if key in convert_table:
        try:
            val = convert_table[key](val)
        except ValueError:
            pass
    else:
        try:
            val = int(val)  # make int if can.
        except ValueError:
            try:
                val = float(val)  # make float if can.
            except ValueError:
                pass
    if val == "":  # Empty strings treated as None
        val = None
    return val


def value(key: str) -> Optional[Union[str, float, int]]:
    """
    Return the value of the given key. If the value is not defined, return the default value
    When the key is not found, an AttributeError is raised
    """
    rhnSQL.initDB()

    h = rhnSQL.prepare(
        """
            SELECT value, default_value
            FROM rhnConfiguration
            WHERE key = :key
            """
    )
    h.execute(key=key)
    data = h.fetchone_dict()
    if not data:
        raise AttributeError(key)

    result = data["value"] if data["value"] else data["default_value"]
    return sanitize_value(key, result)
