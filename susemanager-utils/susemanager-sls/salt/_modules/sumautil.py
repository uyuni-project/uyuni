# -*- coding: utf-8 -*-
'''
Utility module for Suse Manager

'''
from __future__ import absolute_import

import logging
import salt.utils
import salt.modules.cmdmod
import salt.modules.network
import socket
import os
from salt.exceptions import CommandExecutionError

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
    'network.get_route': salt.modules.network.get_route
}

log = logging.getLogger(__name__)

__virtualname__ = 'sumautil'


def __virtual__():
    '''
    Only run on Linux systems
    '''
    if __grains__['kernel'] != 'Linux':
        return (False, 'The sumautil module only works on Linux systems.')
    return __virtualname__


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
    sysfspath = '/sys/class/net/{0}/device/driver'.format(iface)

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
    for e in os.listdir('/sys/class/net/'):
        try:
            drivers[e] = get_net_module(e)
        except OSError as e:
            log.warn("An error occurred getting net driver for {0}".format(e), exc_info=True)
    return drivers or None
