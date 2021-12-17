# -*- coding: utf-8 -*-
"""
Watch RPM or DPkg database via cookies and fire
an event to the SUSE Manager if that has been changed.

Author: Bo Maryniuk <bo@suse.de>
"""

from __future__ import absolute_import
import os

import salt.cache
import salt.config


__virtualname__ = "pkgset"

__opts__ = salt.config.minion_config('/etc/salt/minion')

CACHE = salt.cache.Cache(__opts__)

PKGSET_COOKIES = (
    "/var/cache/venv-salt-minion/rpmdb.cookie",
    "/var/cache/venv-salt-minion/dpkg.cookie",
    "/var/cache/salt/minion/rpmdb.cookie",
    "/var/cache/salt/minion/dpkg.cookie",
)


def __virtual__():
    return __virtualname__


def validate(config):
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


def beacon(config):
    """
    Watch the cookie file from package manager plugin.
    If its content changes, fire an event to the Master.

    Example Config

    .. code-block:: yaml

        beacons:
          pkgset:
            interval: 5

    """

    ret = []
    for cookie_path in PKGSET_COOKIES:
        if not os.path.exists(cookie_path):
            continue
        with open(cookie_path) as ck_file:
            ck_data = ck_file.read().strip()
            if __virtualname__ not in __context__:
                cache_data = CACHE.fetch("beacon/pkgset", "cookie").get("data", None)
                if cache_data:
                    __context__[__virtualname__] = cache_data
                else:
                    __context__[__virtualname__] = ck_data
                    CACHE.store("beacon/pkgset", "cookie", {"data": ck_data})
            if __context__[__virtualname__] != ck_data:
                ret.append({"tag": "changed"})
                CACHE.store("beacon/pkgset", "cookie", {"data": ck_data})
                __context__[__virtualname__] = ck_data
                break

    return ret
