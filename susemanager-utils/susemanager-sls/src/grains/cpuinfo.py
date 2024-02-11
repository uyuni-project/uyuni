#  pylint: disable=missing-module-docstring,unused-import
import json
import logging
import salt.modules.cmdmod
import salt.utils
import os
import re

try:
    from salt.utils.path import which_bin as _which_bin
except ImportError:
    from salt.utils import which_bin as _which_bin

from salt.exceptions import CommandExecutionError

__salt__ = {
    "cmd.run_all": salt.modules.cmdmod.run_all,
}

log = logging.getLogger(__name__)


def _lscpu_count_sockets(feedback):
    """
    Use lscpu method

    :return:
    """
    lscpu = _which_bin(["lscpu"])
    if lscpu is not None:
        try:
            log.debug("Trying lscpu to get CPU socket count")
            ret = __salt__["cmd.run_all"](
                # pylint: disable-next=consider-using-f-string
                "{0} -p".format(lscpu),
                output_loglevel="quiet",
            )
            if ret["retcode"] == 0:
                max_socket_index = -1
                for line in ret["stdout"].strip().splitlines():
                    if line.startswith("#"):
                        continue
                    socket_index = int(line.split(",")[2])
                    if socket_index > max_socket_index:
                        max_socket_index = socket_index
                if max_socket_index > -1:
                    return {"cpusockets": (1 + max_socket_index)}
        # pylint: disable-next=broad-exception-caught
        except Exception as error:
            # pylint: disable-next=consider-using-f-string
            feedback.append("lscpu: {0}".format(str(error)))
            log.debug(str(error))


def _cpuinfo_count_sockets(feedback):
    """
    Use parsing /proc/cpuinfo method.

    :return:
    """
    physids = set()
    if os.access("/proc/cpuinfo", os.R_OK):
        try:
            log.debug("Trying /proc/cpuinfo to get CPU socket count")
            # pylint: disable-next=unspecified-encoding
            with open("/proc/cpuinfo") as handle:
                for line in handle.readlines():
                    if line.strip().startswith("physical id"):
                        comps = line.split(":")
                        if len(comps) < 2 or len(comps[1]) < 2:
                            continue
                        physids.add(comps[1].strip())
            if physids:
                return {"cpusockets": len(physids)}
        # pylint: disable-next=broad-exception-caught
        except Exception as error:
            log.debug(str(error))
            # pylint: disable-next=consider-using-f-string
            feedback.append("/proc/cpuinfo: {0}".format(str(error)))
        else:
            feedback.append("/proc/cpuinfo: format is not applicable")


def _dmidecode_count_sockets(feedback):
    """
    Use dmidecode method.

    :return:
    """
    dmidecode = _which_bin(["dmidecode"])
    if dmidecode is not None:
        try:
            log.debug("Trying dmidecode to get CPU socket count")
            ret = __salt__["cmd.run_all"](
                # pylint: disable-next=consider-using-f-string
                "{0} -t processor".format(dmidecode),
                output_loglevel="quiet",
            )
            if ret["retcode"] == 0:
                count = 0
                for line in ret["stdout"].strip().splitlines():
                    if "Processor Information" in line:
                        count += 1
                if count:
                    return {"cpusockets": count}
        # pylint: disable-next=broad-exception-caught
        except Exception as error:
            log.debug(str(error))
            # pylint: disable-next=consider-using-f-string
            feedback.append("dmidecode: {0}".format(str(error)))
    else:
        feedback.append("dmidecode: executable not found")


def cpusockets():
    """
    Returns the number of CPU sockets.
    """
    feedback = list()
    grains = (
        _lscpu_count_sockets(feedback)
        or _cpuinfo_count_sockets(feedback)
        or _dmidecode_count_sockets(feedback)
    )
    if not grains:
        log.warning(
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            "Could not determine CPU socket count: {0}".format(" ".join(feedback))
        )

    return grains


def total_num_cpus():
    """returns the total number of CPU in system.
    /proc/cpuinfo shows the number of active CPUs
    On s390x this can be different from the number of present CPUs in a system
    See IBM redbook: "Using z/VM for Test and Development Environments: A Roundup" chapter 3.5
    """
    re_cpu = re.compile(r"^cpu[0-9]+$")
    sysdev = "/sys/devices/system/cpu/"
    return {
        "total_num_cpus": len(
            [
                cpud
                for cpud in (os.path.exists(sysdev) and os.listdir(sysdev) or list())
                if re_cpu.match(cpud)
            ]
        )
    }


def cpu_data():
    """
    Returns the cpu model, vendor ID and other data that may not be in the cpuinfo
    """
    lscpu = _which_bin(["lscpu"])
    if lscpu is not None:
        try:
            log.debug("Trying lscpu to get CPU data")
            ret = __salt__["cmd.run_all"](
                # pylint: disable-next=consider-using-f-string
                "{0}".format(lscpu),
                env={"LC_ALL": "C"},
                output_loglevel="quiet",
            )
            if ret["retcode"] == 0:
                lines = ret["stdout"].splitlines()
                name_map = {
                    "Model name": "cpu_model",
                    "Vendor ID": "cpu_vendor",
                    "NUMA node(s)": "cpu_numanodes",
                    "Stepping": "cpu_stepping",
                    "Core(s) per socket": "cpu_cores",
                    "Socket(s)": "cpu_sockets",
                    "Thread(s) per core": "cpu_threads",
                    "CPU(s)": "cpu_sum",
                }
                values = {}
                for line in lines:
                    parts = [l.strip() for l in line.split(":", 1)]
                    if len(parts) == 2 and parts[0] in name_map:
                        values[name_map[parts[0]]] = parts[1]
                log.debug(values)
                return values
            else:
                log.warning("lscpu does not support -J option")
        except (CommandExecutionError, ValueError) as error:
            # pylint: disable-next=logging-format-interpolation,consider-using-f-string
            log.warning("lscpu: {0}".format(str(error)))
