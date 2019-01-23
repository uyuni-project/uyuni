# -*- coding: utf-8 -*-
'''
Watch libzypp/RPM database via cookies and fire
an event to the SUSE Manager if that has been changed.

Author: Bo Maryniuk <bo@suse.de>
'''

from __future__ import absolute_import
import os
import logging
log = logging.getLogger(__name__)


__virtualname__ = 'pkgset'


def __virtual__():
    return (
        os.path.exists("/usr/lib/zypp/plugins/commit/susemanager") or  # Remove this once 2015.8.7 not in use
        os.path.exists("/usr/lib/zypp/plugins/commit/zyppnotify") or
        os.path.exists("/usr/share/yum-plugins/susemanagerplugin.py") or  # Remove this once 2015.8.7 not in use
        os.path.exists("/usr/share/yum-plugins/yumnotify.py")
    ) and __virtualname__ or False


def validate(config):
    '''
    Validate the beacon configuration. A "cookie" file path is mandatory.
    '''

    if not config.get('cookie'):
        return False, 'Cookie path has not been set.'

    return True, 'Configuration validated'


def beacon(config):
    '''
    Watch the cookie file from libzypp's plugin. If its content changes, fire an event to the Master.

    Example Config

    .. code-block:: yaml

        beacons:
          pkgset:
            cookie: /path/to/cookie/file
            interval: 5

    '''

    ret = []
    if os.path.exists(config.get('cookie', '')):
        with open(config.get('cookie')) as ck_file:
            ck_data = ck_file.read().strip()
            if __virtualname__ not in __context__:
                __context__[__virtualname__] = ck_data
            if __context__[__virtualname__] != ck_data:
                ret.append({
                    'tag': 'changed'
                })
                __context__[__virtualname__] = ck_data

    return ret
