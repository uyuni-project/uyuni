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

def _calculate_sls(actionchain_id, machine_id, chunk):
    return '{0}.actionchain_{1}_{2}_{3}'.format(SALT_ACTIONCHAIN_BASE,
                                                actionchain_id,
                                                machine_id,
                                                chunk)

def _get_ac_storage_filenamepath():
    '''
    Calculate the filepath to the '_mgractionchains.conf' which is placed
    by default in /etc/salt/minion.d/
    '''
    config_dir = __opts__.get('conf_dir', None)
    if config_dir is None and 'conf_file' in __opts__:
        config_dir = os.path.dirname(__opts__['conf_file'])
    if config_dir is None:
        config_dir = salt.syspaths.CONFIG_DIR

    minion_d_dir = os.path.join(
        config_dir,
        os.path.dirname(__opts__.get('default_include',
                                      salt.config.DEFAULT_MINION_OPTS['default_include'])))

    return os.path.join(minion_d_dir, '_mgractionchains.conf')

def _read_next_ac_chunk(clear=True):
    '''
    Read and remove the content of '_mgractionchains.conf' file. Return the parsed YAML.
    '''
    f_storage_filename = _get_ac_storage_filenamepath()
    if not os.path.isfile(f_storage_filename):
        return None
    ret = None
    try:
        with salt.utils.fopen(f_storage_filename, "r") as f_storage:
            ret = yaml.load(f_storage.read())
        if clear:
            os.remove(f_storage_filename)
        return ret
    except (IOError, yaml.scanner.ScannerError) as exc:
        err_str = "Error processing YAML from '{0}': {1}".format(f_storage_filename, exc)
        log.error(err_str)
        raise CommandExecutionError(err_str)

def _add_boot_time(next_chunk, prefix):
    '''
    Add the current boot time to the next_chunk dict
    '''
    uptime = __salt__["status.uptime"]()
    next_chunk["{0}_boot_time".format(prefix)] = uptime["since_iso"]

def _persist_next_ac_chunk(next_chunk):
    '''
    Persist next_chunk to execute as YAML in '_mgractionchains.conf'
    '''
    _add_boot_time(next_chunk, "persist")
    f_storage_filename = _get_ac_storage_filenamepath()
    try:
        f_storage_dir = os.path.dirname(f_storage_filename);
        if not os.path.exists(f_storage_dir):
            os.makedirs(f_storage_dir)
        with salt.utils.fopen(f_storage_filename, "w") as f_storage:
            f_storage.write(yaml.dump(next_chunk))
    except (IOError, yaml.scanner.ScannerError) as exc:
        err_str = "Error writing YAML from '{0}': {1}".format(f_storage_filename, exc)
        log.error(err_str)
        raise CommandExecutionError(err_str)

def start(actionchain_id):
    '''
    Start the execution of the given SUSE Manager Action Chain

    actionchain_id
        The SUSE Manager Actionchain ID to execute on this minion.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.start 123
    '''
    if os.path.isfile(_get_ac_storage_filenamepath()):
        msg = "Action Chain '{0}' cannot be started. There is already another " \
              "Action Chain being executed. Please check file '{1}'".format(
                actionchain_id, _get_ac_storage_filenamepath())
        log.error(msg)
        raise CommandExecutionError(msg)
    target_sls = _calculate_sls(actionchain_id, __grains__['machine_id'], 1)
    log.debug("Starting execution of SUSE Manager Action Chains ID "
              "'{0}' -> Target SLS: {1}".format(actionchain_id, target_sls))
    ret = __salt__['state.sls'](target_sls, queue=True)
    if isinstance(ret, list):
        raise CommandExecutionError(ret)
    return ret

def next(actionchain_id, chunk, next_action_id=None, ssh_extra_filerefs=None):
    '''
    Persist the next Action Chain chunk to be executed by the 'resume' method.

    next_chunk
        The next target SLS to be executed.

    CLI Example:

    .. code-block:: bash

        salt '*' mgractionchains.next actionchains.actionchain_123_machineid_2
    '''
    yaml_dict = {
        'next_chunk': _calculate_sls(actionchain_id, __grains__['machine_id'], chunk)
    }
    if next_action_id:
        yaml_dict['next_action_id'] = next_action_id
    if ssh_extra_filerefs:
        yaml_dict['ssh_extra_filerefs'] = ssh_extra_filerefs
    _persist_next_ac_chunk(yaml_dict)

def get_pending_resume():
    '''
    Get information about any pending action chain chunk execution.
    '''
    next_chunk = _read_next_ac_chunk(False)
    if next_chunk:
        _add_boot_time(next_chunk, "current")
    return next_chunk or {}



def resume():
    '''
    Continue the execution of a SUSE Manager Action Chain.
    This will trigger the execution of the next chunk SLS file stored on '_mgractionchains.conf'

    This method is called by the Salt Reactor as a response to the 'minion/start/event'.
    '''
    next_chunk = _read_next_ac_chunk()
    if not next_chunk:
        return {}
    if type(next_chunk) != dict:
        err_str = "Not able to resume Action Chain execution! Malformed " \
                  "'_mgractionchains.conf' found: {0}".format(next_chunk)
        log.error(err_str)
        raise CommandExecutionError(err_str)
    next_chunk = next_chunk.get('next_chunk')
    log.debug("Resuming execution of SUSE Manager Action Chain -> Target SLS: "
              "{0}".format(next_chunk))
    return __salt__['state.sls'](next_chunk, queue=True)

def clean():
    '''
    Clean execution of an Action Chain by removing '_mgractionchains.conf'.
    '''
    _read_next_ac_chunk()
    return {"success": True}


