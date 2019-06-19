import logging
import salt.modules.cmdmod
import salt.utils
import os
import re

__salt__ = {
    'cmd.run_all': salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


def _lscpu(feedback):
    '''
    Use lscpu method

    :return:
    '''
    lscpu = salt.utils.path.which_bin(['lscpu'])
    if lscpu is not None:
        try:
            log.debug("Trying lscpu to get CPU socket count")
            ret = __salt__['cmd.run_all']('{0} -p'.format(lscpu), output_loglevel='quiet')
            if ret['retcode'] == 0:
                max_socket_index = -1
                for line in ret['stdout'].strip().splitlines():
                    if line.startswith('#'):
                        continue
                    socket_index = int(line.split(',')[2])
                    if socket_index > max_socket_index:
                        max_socket_index = socket_index
                if max_socket_index > -1:
                    return {'cpusockets': (1 + max_socket_index)}
        except Exception as error:
            feedback.append("lscpu: {0}".format(str(error)))
            log.debug(str(error))


def _parse_cpuinfo(feedback):
    '''
    Use parsing /proc/cpuinfo method.

    :return:
    '''
    physids = set()
    if os.access("/proc/cpuinfo", os.R_OK):
        try:
            log.debug("Trying /proc/cpuinfo to get CPU socket count")
            with open('/proc/cpuinfo') as handle:
                for line in handle.readlines():
                    if line.strip().startswith('physical id'):
                        comps = line.split(':')
                        if len(comps) < 2 or len(comps[1]) < 2:
                            continue
                        physids.add(comps[1].strip())
            if physids:
                return {'cpusockets': len(physids)}
        except Exception as error:
            log.debug(str(error))
            feedback.append("/proc/cpuinfo: {0}".format(str(error)))
        else:
            feedback.append('/proc/cpuinfo: format is not applicable')


def _dmidecode(feedback):
    '''
    Use dmidecode method.

    :return:
    '''
    dmidecode = salt.utils.path.which_bin(['dmidecode'])
    if dmidecode is not None:
        try:
            log.debug("Trying dmidecode to get CPU socket count")
            ret = __salt__['cmd.run_all']("{0} -t processor".format(dmidecode), output_loglevel='quiet')
            if ret['retcode'] == 0:
                count = 0
                for line in ret['stdout'].strip().splitlines():
                    if 'Processor Information' in line:
                        count += 1
                if count:
                    return {'cpusockets': count}
        except Exception as error:
            log.debug(str(error))
            feedback.append("dmidecode: {0}".format(str(error)))
    else:
        feedback.append("dmidecode: executable not found")


def cpusockets():
    """
    Returns the number of CPU sockets.
    """
    feedback = list()
    grains = _lscpu(feedback) or _parse_cpuinfo(feedback) or _dmidecode(feedback)
    if not grains:
        log.warn("Could not determine CPU socket count: {0}".format(' '.join(feedback)))

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
