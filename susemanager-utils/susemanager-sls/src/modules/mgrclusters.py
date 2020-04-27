# -*- coding: utf-8 -*-
'''
SUSE Manager Clusters Management module for Salt

'''
from __future__ import absolute_import

from salt.exceptions import CommandExecutionError
import logging

log = logging.getLogger(__name__)

__virtualname__ = 'mgrclusters'


def __virtual__():
    '''
    This module is always enabled while 'cmd.run' is available.
    '''
    return __virtualname__ if 'cmd.run' in __salt__ else (False, 'cmd.run is not available')


def _get_provider_fun(provider_module, fun):
    fun_key = "{}.{}".format(provider_module, fun)
    if not provider_module:
        raise CommandExecutionError("You must specify a valid cluster provider module: {}".format(provider_module))
    elif fun_key in __salt__:
        return __salt__[fun_key]
    else:
        raise CommandExecutionError("The selected cluster provider cannot be found: {}".format(provider_module))


def list_nodes(provider_module, params):
    fun = _get_provider_fun(provider_module, 'list_nodes')
    return fun(**params)


def add_node(provider_module, params):
    fun = _get_provider_fun(provider_module, 'add_node')
    return fun(**params)


def remove_node(provider_module, params):
    fun = _get_provider_fun(provider_module, 'remove_node')
    return fun(**params)


def upgrade_cluster(provider_module, params):
    fun = _get_provider_fun(provider_module, 'upgrade_cluster')
    return fun(**params)


def create_cluster(provider_module, params):
    fun = _get_provider_fun(provider_module, 'create_cluster')
    return fun(**params)
