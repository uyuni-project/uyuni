# -*- coding: utf-8 -*-
'''
Export udev database

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
    Only work when udevadm is installed.
    '''
    return salt.utils.which_bin(['udevadm']) is not None


def exportdb():
    '''
    Extract all info delivered by udevadm

    CLI Example:

    .. code-block:: bash

        salt '*' udev.info /dev/sda
        salt '*' udev.info /sys/class/net/eth0
    '''

    cmd = 'udevadm info --export-db'
    udev_result = __salt__['cmd.run_all'](cmd, output_loglevel='quiet')

    if udev_result['retcode'] != 0:
        raise CommandExecutionError(udev_result['stderr'])

    devices = []
    dev = {}
    for line in (line.strip() for line in udev_result['stdout'].splitlines()):
        if line:
            line = line.split(':', 1)
            if len(line) != 2:
                continue
            query, data = line
            if query == 'E':
                if query not in dev:
                    dev[query] = {}
                key, val = data.strip().split('=', 1)

                try:
                    val = int(val)
                except ValueError:
                    try:
                        val = float(val)
                    except ValueError:
                        pass  # Quiet, this is not a number.

                dev[query][key] = val
            else:
                if query not in dev:
                    dev[query] = []
                dev[query].append(data.strip())
        else:
            if dev:
                normalize(dev)
                add_scsi_info(dev)
                devices.append(dev)
                dev = {}
    if dev:
        normalize(dev)
        add_scsi_info(dev)
        devices.append(dev)

    return devices


def normalize(dev):
    '''
    Replace list with only one element to the value of the element.

    :param dev:
    :return:
    '''
    for sect, val in list(dev.items()):
        if isinstance(val, list) and len(val) == 1:
            dev[sect] = val[0]

    return dev


def add_scsi_info(dev):
    '''
    Add SCSI info from sysfs
    '''
    if dev.get('E') and dev.get('E').get('SUBSYSTEM') == 'scsi' and dev.get('E').get('DEVTYPE') == 'scsi_device':
        sysfs_path = dev['P']
        scsi_type = __salt__['cmd.run_all']('cat /sys/{0}/type'.format(sysfs_path), output_loglevel='quiet')

        if scsi_type['retcode'] != 0:
            raise CommandExecutionError(scsi_type['stderr'])

        dev['X-Mgr'] = {}
        dev['X-Mgr']['SCSI_SYS_TYPE'] = scsi_type['stdout']
