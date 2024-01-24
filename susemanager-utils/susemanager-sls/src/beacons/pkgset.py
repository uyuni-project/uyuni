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

SALT_CONFIG_DIR = os.environ.get("SALT_CONFIG_DIR", "/etc/salt")

__opts__ = salt.config.minion_config(os.path.join(SALT_CONFIG_DIR, "minion"))

CACHE = salt.cache.Cache(__opts__)

PKGSET_COOKIES = (
    os.path.join(__opts__["cachedir"], "rpmdb.cookie"),
    os.path.join(__opts__["cachedir"], "dpkg.cookie"),
)


# pylint: disable-next=invalid-name
def __virtual__():
    return __virtualname__


# pylint: disable-next=unused-argument
def validate(config):
    """
    The absence of this function could cause noisy logging,
    when logging level set to DEBUG or TRACE.
    So we need to have it with no any validation inside.
    """
    return True, "There is nothing to validate"


# pylint: disable-next=unused-argument
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
        # pylint: disable-next=unspecified-encoding
        with open(cookie_path) as ck_file:
            ck_data = ck_file.read().strip()
            # pylint: disable-next=undefined-variable
            if __virtualname__ not in __context__:
                # After a minion restart, when this is running for first time, there is nothing in context yet
                # So, if there is any data in the cache, we put it in the context, if not we put the new data.
                # and update the data in the cache.
                cache_data = CACHE.fetch("beacon/pkgset", "cookie").get("data", None)
                if cache_data:
                    # pylint: disable-next=undefined-variable
                    __context__[__virtualname__] = cache_data
                else:
                    # pylint: disable-next=undefined-variable
                    __context__[__virtualname__] = ck_data
                    CACHE.store("beacon/pkgset", "cookie", {"data": ck_data})
            # pylint: disable-next=undefined-variable
            if __context__[__virtualname__] != ck_data:
                # Now it's time to fire beacon event only if the new data is not yet
                # inside the context (meaning not proceesed), and then stop iterating
                ret.append({"tag": "changed"})
                CACHE.store("beacon/pkgset", "cookie", {"data": ck_data})
                # pylint: disable-next=undefined-variable
                __context__[__virtualname__] = ck_data
                break

    return ret
