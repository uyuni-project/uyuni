# -*- coding: utf-8 -*-
'''
Utility module for Suse Manager

'''
from __future__ import absolute_import

import logging
import socket
import os
from salt.exceptions import CommandExecutionError

log = logging.getLogger(__name__)

__virtualname__ = 'sumautil'

SYSFS_NET_PATH = '/sys/class/net'


def __virtual__():
    '''
    Only run on Linux systems
    '''
    return __grains__['kernel'] == 'Linux' and __virtualname__ or False


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
       return {'retcode': 1, 'stderr': result['stderr']}

    return {'retcode': 0, 'stdout': result['stdout']}


def primary_ips():
    '''
    Get the source IPs that the minion uses to connect to the master.
    Returns the IPv4 and IPv6 address (if available).

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.primary_ip
    '''

    get_master_ip = lambda family, host: socket.getaddrinfo(host, 0, family)[0][-1][0]

    master = __opts__.get('master', '')
    log.debug('Using master: {0}'.format(str(master)))

    ret = dict()
    for sock_family, sock_descr in {socket.AF_INET: 'IPv4', socket.AF_INET6: 'IPv6'}.iteritems():
        try:
            ret['{0}'.format(sock_descr)] = __salt__['network.get_route'](get_master_ip(sock_family, master))
            log.debug("network.get_route({0}): ".format(ret['{0} source'.format(sock_descr)]))
        except Exception as err:
            log.debug('{0} is not available? {1}'.format(sock_descr, err))

    return ret


def get_net_module(iface):
    '''
    Returns the kernel module used for the give interface
    or None if the module could not be determined of if the
    interface name is wrong.
    Uses '/sys/class/net' to find out the module.

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_net_module eth0
    '''
    sysfspath = os.path.join(SYSFS_NET_PATH, iface, 'device/driver')

    return os.path.exists(sysfspath) and os.path.split(os.readlink(sysfspath))[-1] or None


def get_net_modules():
    '''
    Returns a dictionary of all network interfaces and their
    corresponding kernel module (if it could be determined).

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_net_modules
    '''
    drivers = dict()
    for devdir in os.listdir(SYSFS_NET_PATH):
        try:
            drivers[devdir] = get_net_module(devdir)
        except OSError as devdir:
            log.warn("An error occurred getting net driver for {0}".format(devdir), exc_info=True)

    return drivers or None
