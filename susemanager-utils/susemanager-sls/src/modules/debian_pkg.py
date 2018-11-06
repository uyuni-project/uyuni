# -*- coding: utf-8 -*-
'''
Overriding stock functions for Debian pkg

'''
from __future__ import absolute_import

import logging
import salt.utils
import salt.modules.cmdmod
from datetime import datetime
from salt.exceptions import CommandExecutionError

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)

__virtualname__ = 'pkg'

def __virtual__():
    '''
    Only load on Debian systems.
    '''
    return __virtualname__ if __grains__['os_family'] == 'Debian' else False


def info_installed(*names, **kwargs):
    '''
    Return the information of the named package(s) installed on the system.

    This is a copy of aptpkg.info_installed which transforms
    Debian 'version' into 'epoch', 'version' and 'release'
    Debian 'architecture' gets transformed to 'arch' and
    'install_date_time_t' is generated from 'install_date'

    names
        The names of the packages for which to return information.

    failhard
        Whether to throw an exception if none of the packages are installed.
        Defaults to True.

    attr
        Comma seperated list of attributes to return.
        Missing attribute will be silently dropped.
        Supported values are the ones from Debian +
        'epoch', 'release', 'arch' and 'install_date_time_t'

    CLI example:

    .. code-block:: bash
        salt '*' pkg.info_installed <package1>
        salt '*' pkg.info_installed <package1> <package2> <package3> ...
        salt '*' pkg.info_installed <package1> failhard=false
        salt '*' pkg.info_installed <package1> failhard=false attr=package,version,architecture
    '''
    kwargs = salt.utils.args.clean_kwargs(**kwargs)
    failhard = kwargs.pop('failhard', True)
    attrs = kwargs.pop('attr', None)

    if not attrs is None:
        attrs_arr = attrs.split(',')

    if kwargs:
        salt.utils.args.invalid_kwargs(kwargs)

    ret = dict()
    for pkg_name, pkg_nfo in __salt__['lowpkg.info'](*names, failhard=failhard).items():
        t_nfo = dict()
        # susemanager needs 'epoch', 'version', 'release', 'arch'
        # and 'install_date_time-t' attributes
        # install_date_time_t is calculated from 'install_date' and
        # 'epoch', 'version' and 'release' are created from 'version'
        for key, value in pkg_nfo.items():
            if key == 'package':
                t_nfo['name'] = value
            elif key == 'origin':
                t_nfo['vendor'] = value
            elif key == 'section':
                t_nfo['group'] = value
            elif key == 'maintainer':
                t_nfo['packager'] = value
            elif key == 'homepage':
                t_nfo['url'] = value
            elif key == 'architecture':
                t_nfo['arch'] = value + '-deb'
            elif key == 'install_date':
                t_nfo[key] = value
                t_nfo['install_date_time_t'] = int((datetime.strptime(value, '%Y-%m-%dT%H:%M:%SZ') - datetime(1970, 1, 1)).total_seconds())
            elif key == 'version':
                # special care
                tmp_v = value
                if ':' in tmp_v:
                    # found epoch
                    t_nfo['epoch'], tmp_v = tmp_v.split(':', 1)
                if '-' in tmp_v:
                    t_nfo['version'], t_nfo['release'] = tmp_v.split('-', 1)
                else:
                    t_nfo['version'] = tmp_v
                    t_nfo['release'] = 'X'
            else:
                t_nfo[key] = value

        # if attributes have been given, only return these
        if not attrs is None:
            t_nfo2 = dict()
            for attr in attrs_arr:
                if t_nfo.has_key(attr):
                    t_nfo2[attr] = t_nfo[attr]

            ret[pkg_name] = t_nfo2
        else:
            ret[pkg_name] = t_nfo

    return ret

