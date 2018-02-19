# -*- coding: utf-8 -*-
'''
SUSE Manager Action Chains module for Salt

'''
from __future__ import absolute_import

import logging
from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = 'mgractionchains'

SALT_ACTIONCHAINS_BASE = 'actionchains'


def __virtual__():
    '''
    This module is always enabled while 'state.sls' is available.
    '''
    return __virtualname__ if 'state.sls' in __salt__ else (False, 'state.sls is not available')


def _calculate_sls(actionchain_id, minion_id, chunk):
    return '{0}.actionchain_{1}_{2}_{3}'.format(SALT_ACTIONCHAINS_BASE,
                                                actionchain_id,
                                                minion_id,
                                                chunk)

def start(actionchain_id):
    '''
    Start the execution of the given SUSE Manager Action Chain

    actionchain_id
        The SUSE Manager Actionchain ID to execute on this minion.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.start 123
    '''
    target_sls = _calculate_sls(actionchain_id, __grains__['id'], 1)
    return __salt__['state.sls'](target_sls, metadata={"mgractionchain": True})
