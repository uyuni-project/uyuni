#!/usr/bin/env python

import logging
import salt.modules.cmdmod
import salt.utils
import os
import re

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


def cpusockets():
    """
    Returns the number of CPU sockets.
    """
    grains = {}
    physids = {}

    # First try lscpu command if available
    lscpu = salt.utils.which_bin(['lscpu'])
    if lscpu is not None:
        try:
            log.debug("Trying lscpu to get CPU socket count")
            cmd = lscpu + " -p"
            ret = __salt__['cmd.run_all'](cmd, output_loglevel='quiet')
            if ret['retcode'] == 0:
                lines = ret['stdout']
                max_socket_index = -1
                for line in lines.splitlines():
                    if line.startswith('#'):
                        continue
                    # get the socket index from the output
                    socket_index = int(line.split(',')[2])
                    if socket_index > max_socket_index:
                        max_socket_index = socket_index
                if max_socket_index > -1:
                    grains['cpusockets'] = 1 + max_socket_index
                    return grains
        except:
            pass

    # Next try parsing /proc/cpuinfo
    if os.access("/proc/cpuinfo", os.R_OK):
        try:
            log.debug("Trying /proc/cpuinfo to get CPU socket count")
            with open('/proc/cpuinfo') as f:
                for line in f:
                    if line.strip().startswith('physical id'):
                        comps = line.split(':')
                        if not len(comps) > 1:
                            continue
                        if not len(comps[1]) > 1:
                            continue
                        val = comps[1].strip()
                        physids[val] = True
            if physids and len(physids) > 0:
                grains['cpusockets'] = len(physids)
                return grains
        except:
            pass

    # Next try dmidecode
    dmidecode = salt.utils.which_bin(['dmidecode'])
    if dmidecode is not None:
        try:
            log.debug("Trying dmidecode to get CPU socket count")
            cmd = dmidecode + " -t processor"
            ret = __salt__['cmd.run_all'](cmd, output_loglevel='quiet')
            if ret['retcode'] == 0:
                lines = ret['stdout']
                count = 0
                for line in lines.splitlines():
                    if 'Processor Information' in line:
                        count += 1
                if count > 0:
                    grains['cpusockets'] = count
                    return grains
        except:
            pass

    log.warn("Could not determine CPU socket count")
    return grains


def total_num_cpus():
    """ returns the total number of CPU in system.
    /proc/cpuinfo shows the number of active CPUs
    On s390x this can be different from the number of present CPUs in a system
    See IBM redbook: "Using z/VM for Test and Development Environments: A Roundup" chapter 3.5
    """
    re_cpu = re.compile(r"^cpu[0-9]+$")
    sysdev = '/sys/devices/system/cpu/'
    return {'total_num_cpus': len([cpud for cpud in (os.path.exists(sysdev) and os.listdir(sysdev) or list())
                                   if re_cpu.match(cpud)])}
