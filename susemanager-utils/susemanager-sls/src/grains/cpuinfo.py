#  pylint: disable=missing-module-docstring,unused-import

# SPDX-FileCopyrightText: 2016-2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import logging
import salt.modules.cmdmod
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


# -----------------------------------------------------------------------------
# Grain for Architecture-Specific CPU Data
# -----------------------------------------------------------------------------


def _read_file(path):
    """
    Helper to read a file and return its content. Returns empty string if not found.
    """
    try:
        # pylint: disable-next=unspecified-encoding
        with open(path, "r", errors="replace") as f:
            return f.read()
    except FileNotFoundError:
        return ""


def _exact_string_match(key, text):
    """
    Extract a value based on a key in the text using regex.
    """
    # pylint: disable-next=consider-using-f-string
    match = re.search(r"{}\s*:\s*(.*)".format(re.escape(key)), text)
    return match.group(1).strip() if match else ""


def _add_device_tree(specs):
    """
    Attempts to read the device tree from predefined paths and adds it to the specs dict.
    """
    device_tree_paths = [
        "/sys/firmware/devicetree/base/compatible",
        "/sys/firmware/devicetree/base/hypervisor/compatible",
    ]
    for path in device_tree_paths:
        content = _read_file(path)
        if content:
            compatible_strings = [s for s in content.split("\x00") if s]
            specs["device_tree"] = ",".join(compatible_strings)
            break


def _add_ppc64_extras(specs):
    """
    Adds PowerPC specific details.
    """
    _add_device_tree(specs)

    lparcfg_content = _read_file("/proc/ppc64/lparcfg")
    if lparcfg_content:
        match = re.search(r"shared_processor_mode\s*=\s*(\d+)", lparcfg_content)
        if match:
            specs["lpar_mode"] = "shared" if match.group(1) == "1" else "dedicated"


def _add_arm64_extras(specs):
    """
    Adds ARM64-specific details. It first checks for Device Tree information.
    If not found, it falls back to dmidecode for ACPI-based systems.
    """
    _add_device_tree(specs)

    if "device_tree" in specs:
        return

    dmidecode = _which_bin(["dmidecode"])
    if not dmidecode:
        log.debug("dmidecode executable not found, skipping for ARM64 extras.")
        return

    try:
        ret = __salt__["cmd.run_all"](
            # pylint: disable-next=consider-using-f-string
            "{0} -t processor".format(dmidecode),
            output_loglevel="quiet",
        )

        if ret["retcode"] == 0:
            output = ret["stdout"]
            family = _exact_string_match("Family", output)
            manufacturer = _exact_string_match("Manufacturer", output)
            signature = _exact_string_match("Signature", output)

            if family or manufacturer or signature:
                specs["family"] = family
                specs["manufacturer"] = manufacturer
                specs["signature"] = signature
        else:
            log.warning("dmidecode failed for ARM64 extras: %s", ret["stderr"])

    except (CommandExecutionError, OSError) as e:
        log.warning("Failed to retrieve arm64 CPU details via dmidecode: %s", str(e))


def _add_z_systems_extras(specs):
    """
    Collects extended metadata for z Systems based on `read_values -s`.
    """
    read_values = _which_bin(["read_values"])
    if not read_values:
        log.warning("read_values executable not found, skipping for z Systems extras.")
        return

    try:
        ret = __salt__["cmd.run_all"](
            # pylint: disable-next=consider-using-f-string
            "{0} -s".format(read_values),
            output_loglevel="quiet",
        )
        if ret["retcode"] == 0:
            output = ret["stdout"]

            # Identify z architecture layer
            for candidate in ("VM00", "LPAR"):
                if candidate in output:
                    layer_id = candidate
                    break
            else:
                return

            fields = {
                "type": "Type",
                "type_name": "Type Name",
                "layer_type": f"{layer_id} Name",
            }

            for key, label in fields.items():
                value = _exact_string_match(label, output)
                if value:
                    specs[key] = value
        else:
            log.warning("read_values failed for z Systems extras: %s", ret["stderr"])

    except (CommandExecutionError, OSError):
        log.warning("Failed to retrieve z System CPU details.", exc_info=True)


def _get_architecture():
    """
    Returns the system architecture.
    """
    try:
        ret = __salt__["cmd.run_all"]("uname -m", output_loglevel="quiet")
        return ret["stdout"].strip() if ret.get("retcode") == 0 else "unknown"
    except (CommandExecutionError, OSError):
        log.warning("Failed to determine system architecture.", exc_info=True)
        return "unknown"


def arch_specs():
    """
    Returns extended CPU architecture-specific metadata.
    This function is designed to be called to generate a Salt grain.
    """
    specs = {}
    arch = _get_architecture()

    if arch in ["ppc64", "ppc64le"]:
        _add_ppc64_extras(specs)
    elif arch in ["arm64", "aarch64"]:
        _add_arm64_extras(specs)
    elif arch.startswith("s390"):
        _add_z_systems_extras(specs)

    return {"cpu_arch_specs": specs}
