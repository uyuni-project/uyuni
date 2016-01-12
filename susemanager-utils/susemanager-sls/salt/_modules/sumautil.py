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


# def __virtual__():
#     '''
#     Only works when cat is installed.
#     '''
#     # cat should always be available
#     return salt.utils.which_bin(['cat']) is not None


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

    def get_master_ip(family, host):
        try:
            s = socket.socket(family)

            sockfamily, socktype, proto, canonname, sockaddr = socket.getaddrinfo(
                    host, 0, family, socket.SOCK_STREAM)[0]

            if family == socket.AF_INET:
                ip, port = sockaddr
            elif family == socket.AF_INET6:
                ip, port, flow_info, scope_id = sockaddr

            return ip

        except Exception:
            return None
        finally:
            if s:
                s.close()

    master = __opts__.get('master', '')
    log.debug('Using master: {0}'.format(str(master)))

    ipv4 = get_master_ip(socket.AF_INET, master)
    ipv6 = get_master_ip(socket.AF_INET6, master)

    srcipv4 = None
    srcipv6 = None

    # 'ip route get $ip' should be as good as opening a socket to the master
    if ipv4:
        route = __salt__['network.get_route'](ipv4)
        log.debug("network.get_route({0}): ".format(ipv4, str(route)))
        srcipv4 = route.get('source', None)

    if ipv6:
        route = __salt__['network.get_route'](ipv6)
        log.debug("network.get_route({0}): ".format(ipv6, str(route)))
        srcipv6 = route.get('source', None)

    return srcipv4, srcipv6


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
    log.debug("Looking for '{0}'".format(sysfspath))
    if os.path.exists(sysfspath):
        try:
            driverpath = os.readlink(sysfspath)
            driver = driverpath.rsplit('/', 1)[1]
            return driver
        except:
            log.exception('')
    return None


def get_net_modules():
    '''
    Returns a dictionary of all network interfaces and their
    corresponding kernel module (if it could be determined).

    CLI Example:

    .. code-block:: bash

        salt '*' sumautil.get_net_modules
    '''
    drivers = {}
    for e in os.listdir('/sys/class/net/'):
        drivers[e] = get_net_module(e)

    return drivers
