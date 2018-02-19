# -*- coding: utf-8 -*-
'''
SUSE Manager Action Chains module for Salt

'''
from __future__ import absolute_import

import logging
import os
import sys
import salt.config
import salt.syspaths
import salt.utils
import yaml

from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = 'mgractionchains'

SALT_ACTIONCHAIN_BASE = 'actionchains'

def __virtual__():
    '''
    This module is always enabled while 'state.sls' is available.
    '''
    return __virtualname__ if 'state.sls' in __salt__ else (False, 'state.sls is not available')


def _calculate_sls(actionchain_id, minion_id, chunk):
    return '{0}.actionchain_{1}_{2}_{3}'.format(SALT_ACTIONCHAIN_BASE,
                                                actionchain_id,
                                                minion_id,
                                                chunk)

def _get_ac_storage_filenamepath():
    config_dir = __opts__.get('conf_dir', None)
    if config_dir is None and 'conf_file' in self.opts:
        config_dir = os.path.dirname(__opts__['conf_file'])
    if config_dir is None:
        config_dir = salt.syspaths.CONFIG_DIR

    minion_d_dir = os.path.join(
        config_dir,
        os.path.dirname(__opts__.get('default_include',
                                      salt.config.DEFAULT_MINION_OPTS['default_include'])))

    if not os.path.isdir(minion_d_dir):
        os.makedirs(minion_d_dir)

    schedule_conf = os.path.join(minion_d_dir, '_mgractionchains.conf')

def _read_next_ac_chunk():
    try:
        f_storage_filename = _get_ac_storage_filenamepath()
        with salt.utils.fopen(f_storage_filename, "rw") as f_storage:
            ret = yaml.load(f_storage.read())
            f_storage.write()
            return ret
    except (IOError, yaml.scanner.ScannerError) as exc:
        log.error("Error processing YAML from '{0}': {1}".format(f_storage_filename, exc))

def _save_nextactionchains(next_chunk):
    try:
        f_storage_filename = _get_ac_storage_filenamepath()
        with salt.utils.fopen(f_storage_filename, "w") as f_storage:
            f_storage.write(yaml.dump(next_chunk))
    except (IOError, yaml.scanner.ScannerError) as exc:
        log.error("Error writing YAML from '{0}': {1}".format(f_storage_filename, exc))

def start(actionchain_id):
    '''
    Start the execution of the given SUSE Manager Action Chain

    actionchain_id
        The SUSE Manager Actionchain ID to execute on this minion.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.start 123
    '''
    #TODO: We should kill previously stored action before starting a new one?
    target_sls = _calculate_sls(actionchain_id, __grains__['id'], 1)
    log.debug("Starting execution of SUSE Manager Action Chains ID "
              "'{0}' -> Target SLS: {1}".format(actionchain_id, target_sls))
    return __salt__['state.sls'](target_sls, metadata={"mgractionchain": True})

def resume():
    '''
    Continue the execution of a SUSE Manager Action Chain.
    This will trigger the execution of the next chunk SLS file stored on '_mgractionchains.conf'

    This method is called by the Salt Reactor as a response to the 'minion/start/event'.
    '''
    next_chunk = _read_next_ac_chunk().get('next_chunk')
    log.debug("Resuming execution of SUSE Manager Action Chain -> Target SLS: "
              "{0}".format(next_chunk))
    return __salt__['state.sls'](target_sls, metadata={"mgractionchain": True})
