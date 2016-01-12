# -*- coding: utf-8 -*-
'''
Utility commands for Suse Manager

'''
from __future__ import absolute_import

import logging
import salt.utils
import salt.modules.cmdmod
from salt.exceptions import CommandExecutionError

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


def __virtual__():
    '''
    Only work when cat is installed.
    '''
    # cat should always be available
    return salt.utils.which_bin(['cat']) is not None


def cat(path):
    '''
    Cat the specified file.

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.cat /tmp/file
    '''
    cmd = 'cat %s' % path
    result = __salt__['cmd.run_all'](cmd, output_loglevel='quiet')

    if result['retcode'] != 0:
        raise CommandExecutionError(result['stderr'])

    return result['stdout']