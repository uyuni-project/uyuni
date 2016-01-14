# -*- coding: utf-8 -*-
'''
s390 utility for Suse Manager

'''
from __future__ import absolute_import

import logging
import salt.utils
import salt.modules.cmdmod
from salt.exceptions import CommandExecutionError
import os

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


def __virtual__():
    '''
    Only works if /usr/bin/read_values is accessible
    '''
    return os.access("/usr/bin/read_values", os.X_OK)


def read_values():
    '''
    Cat the specified file.

    CLI Example:

    .. code-block:: bash

        salt '*' mainframesysinfo.read_values
    '''
    cmd = '/usr/bin/read_values -s'
    result = __salt__['cmd.run_all'](cmd, output_loglevel='quiet')

    if result['retcode'] != 0:
        raise CommandExecutionError(result['stderr'])

    return result['stdout']