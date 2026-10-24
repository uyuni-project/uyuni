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

"""A module that reads configuration values from the database"""

from typing import List, Optional, Tuple, Union
from spacewalk.common.rhnConfig import is_true
from spacewalk.server import rhnSQL

ConfigValue = Optional[Union[str, float, int]]

# keys whose values are never converted to numbers
STRING_KEYS = (
    "PSW_CHECK_SPECIAL_CHARACTERS",
    "HTTP_PROXY",
    "HTTP_PROXY_USERNAME",
    "HTTP_PROXY_PASSWORD",
    "NO_PROXY",
)


def sanitize_value(key: str, val: Optional[str]) -> ConfigValue:
    """
    attempt to convert a string value to the proper type
    """
    if val is None:
        return None
    val = val.strip()
    if val == "":  # Empty strings treated as None
        return None

    if key in STRING_KEYS:
        return val
    try:
        return int(val)  # make int if can.
    except ValueError:
        try:
            return float(val)  # make float if can.
        except ValueError:
            return val


class DBConfig:
    """
    Read-only access to the configuration values stored in the rhnConfiguration table.

    When a key has no value, the default value is returned.
    Values are changed only by the Java code.

    Values can be read like the file configuration: DB_CFG.SYSTEM_CHECKIN_THRESHOLD
    When the key is not found, an AttributeError is raised.
    """

    def __getattr__(self, key: str) -> ConfigValue:
        if key.startswith("_"):
            raise AttributeError(key)
        return self.get(key.upper())

    def get(self, key: str) -> ConfigValue:
        """
        Return the value of the given key or the default value when no value is set
        """
        return sanitize_value(key, self._raw_value(key))

    def get_default(self, key: str) -> ConfigValue:
        """
        Return the default value of the given key
        """
        return sanitize_value(key, self._fetch(key)[1])

    def get_bool(self, key: str) -> bool:
        """
        Return the value of the given key as boolean
        """
        return is_true(self._raw_value(key))

    def get_list(self, key: str) -> List[str]:
        """
        Return the comma separated value of the given key as list
        """
        raw = self._raw_value(key) or ""
        return [v.strip() for v in raw.split(",") if v.strip()]

    def _raw_value(self, key: str) -> Optional[str]:
        value, default_value = self._fetch(key)
        return value if value is not None else default_value

    def _fetch(self, key: str) -> Tuple[Optional[str], Optional[str]]:
        rhnSQL.initDB()

        h = rhnSQL.prepare("""
                SELECT value, default_value
                FROM rhnConfiguration
                WHERE key = :key
                """)
        h.execute(key=key)
        data = h.fetchone_dict()
        if not data:
            raise AttributeError(key)
        return data["value"], data["default_value"]


DB_CFG = DBConfig()
