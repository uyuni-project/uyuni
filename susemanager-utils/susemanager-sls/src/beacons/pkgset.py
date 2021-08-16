# -*- coding: utf-8 -*-
'''
Watch RPM or DPkg database via cookies and fire
an event to the SUSE Manager if that has been changed.

Author: Bo Maryniuk <bo@suse.de>
'''

from __future__ import absolute_import
from salt.utils.odict import OrderedDict
import os
import logging
log = logging.getLogger(__name__)


__virtualname__ = 'pkgset'

PKG_PLUGINS = OrderedDict([
        ("/usr/lib/zypp/plugins/commit/venv-zyppnotify", "/var/cache/venv-salt-minion/rpmdb.cookie"),
        ("/usr/share/yum-plugins/venv-yumnotify.py",     "/var/cache/venv-salt-minion/rpmdb.cookie"),
        ("/usr/bin/venv-dpkgnotify",                     "/var/cache/venv-salt-minion/dpkg.cookie"),
        ("/usr/lib/zypp/plugins/commit/zyppnotify",      "/var/cache/salt/minion/rpmdb.cookie"),
        ("/usr/share/yum-plugins/yumnotify.py",          "/var/cache/salt/minion/rpmdb.cookie"),
        ("/usr/bin/dpkgnotify",                          "/var/cache/salt/minion/dpkg.cookie")
    ])
COOKIE_PATH = None


def __virtual__():
    return any(
               os.path.exists(plug) for plug in PKG_PLUGINS
           ) and __virtualname__ or False


def validate(config):
    '''
    Validate the beacon configuration. A "cookie" file path is mandatory.
    '''

    global COOKIE_PATH

    for plug in PKG_PLUGINS:
        if os.path.exists(plug):
            COOKIE_PATH = PKG_PLUGINS.get(plug)
            return True, 'Configuration validated'

    return False, 'Cookie path has not been set.'


def beacon(config):
    '''
    Watch the cookie file from libzypp's plugin. If its content changes, fire an event to the Master.

    Example Config

    .. code-block:: yaml

        beacons:
          pkgset:
            interval: 5

    '''

    global COOKIE_PATH

    ret = []
    if os.path.exists(COOKIE_PATH):
        with open(COOKIE_PATH) as ck_file:
            ck_data = ck_file.read().strip()
            if __virtualname__ not in __context__:
                __context__[__virtualname__] = ck_data
            if __context__[__virtualname__] != ck_data:
                ret.append({
                    'tag': 'changed'
                })
                __context__[__virtualname__] = ck_data

    return ret
